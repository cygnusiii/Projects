package com.iii.smarthome.conference;

import java.util.Locale;

import fm.*;
import fm.Serializer;
import fm.icelink.*;
import fm.icelink.websync.*;
import fm.websync.*;

// Peers have to exchange information when setting up P2P links,
// like descriptions of the streams (called the offer or answer)
// and information about network addresses (called candidates).
// IceLink generates this information for you automatically.
// Your responsibility is to pass messages back and forth between
// peers as quickly as possible. This is called "signalling".
public class Signalling
{
    // We're going to use WebSync for this example, but any real-time
    // messaging system will do (like SIP or XMPP). We use WebSync
    // since it works well with JavaScript and uses HTTP, which is
    // widely allowed. To use something else, simply replace the calls
    // to WebSync with calls to your library.
    private String websyncServerUrl = null;
    private Client websyncClient = null;
    // IceLink includes a WebSync client extension that will
    // automatically manage signalling for you. If you are not
    // using WebSync, set this to false to see how the event
    // system works. Use it as a template for your own code.
    private Conference conference = null;
    private String sessionId = null;

    
    public Signalling(String webSyncServerUrl,Conference conference,Client websyncClient){
    	this.websyncServerUrl = webSyncServerUrl;
    	this.conference = conference;
    	this.websyncClient = websyncClient;
    }
    public Signalling(String websyncServerUrl)
    {
        this.websyncServerUrl = websyncServerUrl;

    }
    public void start(final SingleAction<String> callback) throws Exception
    {
        // Create a WebSync client.
        websyncClient = new Client(websyncServerUrl);

        // Create a persistent connection to the server.
        websyncClient.connect(new ConnectArgs()
        {{
            setOnFailure(new SingleAction<ConnectFailureArgs>()
            {
                public void invoke(ConnectFailureArgs e)
                {
                    callback.invoke(String.format(Locale.getDefault(), "Could not connect to WebSync. %s", e.getException().getMessage()));
                    e.setRetry(false);
                }
            });
            setOnSuccess(new SingleAction<ConnectSuccessArgs>()
            {
                public void invoke(ConnectSuccessArgs e)
                {
                    callback.invoke(null);
                }
            });
        }});
    }

    public void stop(final SingleAction<String> callback) throws Exception
    {
        // Tear down the persistent connection.
        websyncClient.disconnect(new DisconnectArgs()
        {{
            setOnComplete(new SingleAction<DisconnectCompleteArgs>()
            {
                public void invoke(DisconnectCompleteArgs e)
                {
                    callback.invoke(null);
                }
            });
        }});

        websyncClient = null;
    }

    public void bindName(String name) {
		try {
			websyncClient.bind(new BindArgs("name", Serializer.serializeString(name)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void bindIMEI(String imei) {
		try {
			websyncClient.bind(new BindArgs("imei", Serializer.serializeString(imei)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public void attach(final Conference conference, final String sessionId, final SingleAction<String> callback) throws Exception
    {
        this.conference = conference;
        this.sessionId = sessionId;

            // Manage the conference automatically using a WebSync
            // channel. P2P links will be created automatically to
            // peers that join the same channel.
            ClientExtensions.joinConference(websyncClient, new JoinConferenceArgs("/" + sessionId, conference)
            {{
                setOnFailure(new SingleAction<JoinConferenceFailureArgs>()
                {
                    public void invoke(JoinConferenceFailureArgs e)
                    {
                        callback.invoke(String.format(Locale.getDefault(), "Could not attach signalling to conference %s. %s", sessionId, e.getException().getMessage()));
                    }
                });
                setOnSuccess(new SingleAction<JoinConferenceSuccessArgs>()
                {
                    public void invoke(JoinConferenceSuccessArgs e)
                    {
                        callback.invoke(null);
                    }
                });
            }});
  
        
    }


    public void detach(final SingleAction<String> callback) throws Exception
    {
            // Leave the managed WebSync channel.
            ClientExtensions.leaveConference(websyncClient, new LeaveConferenceArgs("/" + sessionId)
            {{
                setOnSuccess(new SingleAction<LeaveConferenceSuccessArgs>()
                {
                    public void invoke(LeaveConferenceSuccessArgs e)
                    {
                        conference = null;
                        sessionId = null;
                        
                        callback.invoke(null);
                    }
                });
                setOnFailure(new SingleAction<LeaveConferenceFailureArgs>()
                {
                    public void invoke(LeaveConferenceFailureArgs e)
                    {
                        callback.invoke(String.format(Locale.getDefault(), "Could not detach signalling from conference %s. %s", sessionId, e.getException().getMessage()));
                    }
                });
            }});
      
    }
}
