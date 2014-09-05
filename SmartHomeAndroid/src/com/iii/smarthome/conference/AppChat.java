package com.iii.smarthome.conference;

import java.util.HashMap;
import java.util.Locale;

import com.iii.smarthome.webservice.ConfigServer;



import fm.*;
import fm.Serializer;
import fm.icelink.*;
import fm.icelink.websync.BaseLinkArgsExtensions;
import fm.websync.Record;

public class AppChat
{
    private String icelinkServerAddress = ConfigServer.icelinkServerAddress;
    private String websyncServerUrl = ConfigServer.websyncRequestUrl;

    private Signalling signalling;
    private Conference conference;
    private Stream textStream;
    public  Stream getTextStream()           { return this.textStream; }
    public  void   setTextStream(Stream textStream) { this.textStream = textStream;  }
    private String sessionId;
    public  String getSessionId()           { return this.sessionId; }
    public  void   setSessionId(String sid) { this.sessionId = sid;  }

    private Certificate certificate;

    private SingleAction<LinkInitArgs> logLinkInitEvent;
    private SingleAction<LinkUpArgs>   logLinkUpEvent;
    private SingleAction<LinkDownArgs> logLinkDownEvent;
    private boolean useWebSyncExtension = true;
    
    private AppChat() throws Exception
    {
        logLinkInitEvent = new SingleAction<LinkInitArgs>() { public void invoke(LinkInitArgs e) { logLinkInit(e); } };
        logLinkUpEvent   = new SingleAction<LinkUpArgs>()   { public void invoke(LinkUpArgs e)   { logLinkUp(e);   } };
        logLinkDownEvent = new SingleAction<LinkDownArgs>() { public void invoke(LinkDownArgs e) { logLinkDown(e); } };

        // Log to the Android console.
        Log.setProvider(new AndroidLogProvider(LogLevel.Info));

        // To save time, generate a DTLS certificate when the
        // app starts and reuse it for multiple conferences.
        certificate = Certificate.generateCertificate();
    }

    private static AppChat app;
    public static synchronized AppChat getInstance() throws Exception
    {
        if (app == null)
        {
            app = new AppChat();
        }
        return app;
    }
	public void connect(SingleAction<String> callback,String myName,String myIMEI) throws Exception {
		startSignalling(callback, myName, myIMEI);
		startConference(callback);
	}
	public void disconnect(SingleAction<String> callback) throws Exception{
		stopConference(callback);
		stopSignalling(callback);
	}
    public void startSignalling(SingleAction<String> callback,String myName,String myIMEI) throws Exception
    {
        signalling = new Signalling(websyncServerUrl);
        signalling.start(callback);
        signalling.bindName(myName);
		signalling.bindIMEI(myIMEI);
    }

    public void stopSignalling(SingleAction<String> callback) throws Exception
    {
        signalling.stop(callback);
        signalling = null;

    }

    public void startConference(final SingleAction<String> callback) throws Exception
    {
        // Create a new IceLink conference.
        conference = new Conference(icelinkServerAddress, new Stream[] { textStream});

        // Use our pre-generated DTLS certificate.
        conference.setDtlsCertificate(certificate);

        // Supply TURN relay credentials in case we are behind a
        // highly restrictive firewall. These credentials will be
        // verified by the TURN server.
        conference.setRelayUsername("test");
        conference.setRelayPassword("pa55w0rd!");

        // Add a few event handlers to the conference so we can see
        // when a new P2P link is created or changes state.
        conference.addOnLinkInit(logLinkInitEvent);
        conference.addOnLinkUp(new SingleAction<LinkUpArgs>(){
        	public void invoke(LinkUpArgs e){
        		//attach room number to list
        		if(!getPeerName(e).equals("Guest"))callback.invoke("attach-room"+getPeerName(e));
        	}
        });
        conference.addOnLinkDown(new SingleAction<LinkDownArgs>(){
        	public void invoke(LinkDownArgs e){
        		//detach room number to list
        		callback.invoke("detach-room"+getPeerName(e));
        	}
        });

        // Attach signalling to the conference.
        signalling.attach(conference, sessionId, callback);
    }

    private void logLinkInit(LinkInitArgs e)
    {
        Log.info("Link to "+getPeerName(e)+" initializing...");
    }

    private void logLinkUp(LinkUpArgs e)
    {
        Log.info("Link to "+getPeerName(e)+" is UP.");
    }

    private void logLinkDown(LinkDownArgs e)
    {
        Log.info(String.format(Locale.getDefault(), "Link to "+getPeerName(e)+" is DOWN. %s", e.getException().getMessage()));
    }

    public void stopConference(SingleAction<String> callback) throws Exception
    {
        // Detach signalling from the conference.
        signalling.detach(callback);

        conference.removeOnLinkInit(logLinkInitEvent);
        conference.removeOnLinkUp(logLinkUpEvent);
        conference.removeOnLinkDown(logLinkDownEvent);
        conference = null;
    }
	public void sendMessage(String message){
		try {
			// Send message to the conference links.
			conference.sendRTP(textStream, textStream.getFormat(),new RTPPacket(Encoding.getUTF8().getBytes(message)));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
    private String getPeerName(BaseLinkArgs e)
    {
        @SuppressWarnings("unchecked")
		HashMap<String, Record> peerBindings = (useWebSyncExtension ? BaseLinkArgsExtensions.getPeerClient(e).getBoundRecords() : (HashMap<String, Record>)e.getPeerState());
        try
        {
            return Serializer.deserializeString(peerBindings.get("name").getValueJson());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
	private String getPeerImei(BaseLinkArgs e) {
		@SuppressWarnings("unchecked")
		HashMap<String, Record> peerBindings = (useWebSyncExtension ? BaseLinkArgsExtensions.getPeerClient(e).getBoundRecords(): (HashMap<String, Record>) e.getPeerState());
		try {
			return Serializer.deserializeString(peerBindings.get("imei")
					.getValueJson());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	public Signalling getSignalling(){
		return this.signalling;
	}

}
