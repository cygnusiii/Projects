package com.iii.smarthome.conference;

import java.util.HashMap;
import java.util.Locale;

import com.iii.smarthome.webservice.ConfigServer;

import android.view.*;
import fm.*;
import fm.Serializer;
import fm.icelink.*;
import fm.icelink.webrtc.*;
import fm.icelink.websync.BaseLinkArgsExtensions;
import fm.websync.Record;
import aopus.*;
import avp8.*;

public class AppVideo {
	private String icelinkServerAddress = ConfigServer.icelinkServerAddress;
	private String websyncServerUrl = ConfigServer.websyncRequestUrl;

	private Signalling signalling;
	private LocalMedia localMedia;

	private AudioStream audioStream;
	private VideoStream videoStream;
	private Conference conference;

	private String sessionId;

	public String getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(String sid) {
		this.sessionId = sid;
	}

	private Certificate certificate;

	private SingleAction<LinkInitArgs> logLinkInitEvent;
	private SingleAction<LinkUpArgs> logLinkUpEvent;
	private SingleAction<LinkDownArgs> logLinkDownEvent;
	private SingleAction<StreamLinkInitArgs> addRemoteVideoControlEvent;
	private SingleAction<StreamLinkDownArgs> removeRemoteVideoControlEvent;
	private boolean useWebSyncExtension = true;

	private AppVideo() throws Exception {
		logLinkInitEvent = new SingleAction<LinkInitArgs>() {
			public void invoke(LinkInitArgs e) {
				logLinkInit(e);
			}
		};
		logLinkUpEvent = new SingleAction<LinkUpArgs>() {
			public void invoke(LinkUpArgs e) {
				logLinkUp(e);
			}
		};
		logLinkDownEvent = new SingleAction<LinkDownArgs>() {
			public void invoke(LinkDownArgs e) {
				logLinkDown(e);
			}
		};
		addRemoteVideoControlEvent = new SingleAction<StreamLinkInitArgs>() {
			public void invoke(StreamLinkInitArgs e) {
				addRemoteVideoControl(e);
			}
		};
		removeRemoteVideoControlEvent = new SingleAction<StreamLinkDownArgs>() {
			public void invoke(StreamLinkDownArgs e) {
				removeRemoteVideoControl(e);
			}
		};

		// Log to the Android console.
		Log.setProvider(new AndroidLogProvider(LogLevel.Info));

		// WebRTC has chosen VP8 as its mandatory video codec.
		// Since video encoding is best done using native code,
		// reference the video codec at the application-level.
		// This is required when using a WebRTC video stream.
		VideoStream.registerCodec("VP8", new EmptyFunction<VideoCodec>() {
			public VideoCodec invoke() {
				return new Vp8Codec();
			}
		}, true);

		// For improved audio quality, we can use Opus. By
		// setting it as the preferred audio codec, it will
		// override the default PCMU/PCMA codecs.
		AudioStream.registerCodec("opus", 48000, 2,new EmptyFunction<AudioCodec>() {
				public AudioCodec invoke() {
					return new OpusCodec();
				}
		}, true);

		// To save time, generate a DTLS certificate when the
		// app starts and reuse it for multiple conferences.
		certificate = Certificate.generateCertificate();
	}

	
	private static AppVideo app;

	public static synchronized AppVideo getInstance() throws Exception {
		if (app == null) {
			app = new AppVideo();
		}
		return app;
	}

	public void connect(SingleAction<String> callback,ViewGroup viewContainer) throws Exception {
		if(!localMediaStarted()){
			startLocalMedia(viewContainer, callback);
		}
		startSignalling(callback);
		startConference(callback);
	}
	public void disconnect(SingleAction<String> callback) throws Exception{
		stopConference(callback);
		stopSignalling(callback);
		stopLocalMedia(callback);
	}
	// //////////////////////////
	public boolean signallingStarted() {
		return (signalling != null);
	}

	public void startSignalling(SingleAction<String> callback) throws Exception {
		signalling = new Signalling(websyncServerUrl);
		signalling.start(callback);

	}

	public void stopSignalling(SingleAction<String> callback) throws Exception {
		signalling.stop(callback);
		signalling = null;

	}

	public boolean localMediaStarted() {
		return (localMedia != null);
	}

	public void startLocalMedia(ViewGroup videoContainer,
			SingleAction<String> callback) throws Exception {
		localMedia = new LocalMedia();
		localMedia.start(videoContainer, callback);
	}

	public void stopLocalMedia(SingleAction<String> callback) throws Exception {
		localMedia.stop(callback);
		localMedia = null;
	}

	public void startConference(SingleAction<String> callback) throws Exception {
		if (!signallingStarted()) {
			callback.invoke("Signnalling not start");
		} else if (!localMediaStarted()) {
			callback.invoke("Local not start");
		} else if (conference != null) {
			callback.invoke("Conference is exist");
		} else {
			// Create a WebRTC audio stream description (requires a
			// reference to the local audio feed).
			audioStream = new AudioStream(localMedia.getLocalMediaStream());

			// Create a WebRTC video stream description (requires a
			// reference to the local video feed). Whenever a P2P link
			// initializes using this description, position and display
			// the remote video control on-screen by passing it to the
			// layout manager created above. Whenever a P2P link goes
			// down, remove it.
			videoStream = new VideoStream(localMedia.getLocalMediaStream());
			videoStream.addOnLinkInit(addRemoteVideoControlEvent);
			videoStream.addOnLinkDown(removeRemoteVideoControlEvent);

			// Create a new IceLink conference.
			conference = new Conference(icelinkServerAddress, new Stream[] {
					audioStream, videoStream });
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
			conference.addOnLinkUp(logLinkUpEvent);
			conference.addOnLinkDown(logLinkDownEvent);
			conference.setMaxLinks(1);
			// Attach signalling to the conference.
			signalling.attach(conference, sessionId, callback);
		}
	}

	private void addRemoteVideoControl(StreamLinkInitArgs e) {
		try {
			View remoteVideoControl = (View) LinkExtensions
					.getRemoteVideoControl(e.getLink());
			localMedia.getLayoutManager().addRemoteVideoControl(e.getPeerId(),
					remoteVideoControl);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void removeRemoteVideoControl(StreamLinkDownArgs e) {
		try {
			localMedia.getLayoutManager().removeRemoteVideoControl(
					e.getPeerId());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void logLinkInit(LinkInitArgs e) {
		Log.info("Link to peer initializing...");
	}

	private void logLinkUp(LinkUpArgs e) {
		Log.info("Link to peer is UP.");
	}

	private void logLinkDown(LinkDownArgs e) {
		Log.info(String.format(Locale.getDefault(), "Link to peer is DOWN. %s",
				e.getException().getMessage()));
	}

	public void stopConference(SingleAction<String> callback) throws Exception {
		// Detach signalling from the conference.
		signalling.detach(callback);

		conference.removeOnLinkInit(logLinkInitEvent);
		conference.removeOnLinkUp(logLinkUpEvent);
		conference.removeOnLinkDown(logLinkDownEvent);
		conference = null;

		videoStream.removeOnLinkInit(addRemoteVideoControlEvent);
		videoStream.removeOnLinkDown(removeRemoteVideoControlEvent);
		videoStream = null;
		
		audioStream = null;
	}

	private String getPeerName(BaseLinkArgs e) {
		@SuppressWarnings("unchecked")
		HashMap<String, Record> peerBindings = (useWebSyncExtension ? BaseLinkArgsExtensions
				.getPeerClient(e).getBoundRecords()
				: (HashMap<String, Record>) e.getPeerState());
		try {
			return Serializer.deserializeString(peerBindings.get("name")
					.getValueJson());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private String getPeerImei(BaseLinkArgs e) {
		@SuppressWarnings("unchecked")
		HashMap<String, Record> peerBindings = (useWebSyncExtension ? BaseLinkArgsExtensions
				.getPeerClient(e).getBoundRecords()
				: (HashMap<String, Record>) e.getPeerState());
		try {
			return Serializer.deserializeString(peerBindings.get("imei")
					.getValueJson());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void useNextVideoDevice() {
		localMedia.getLocalMediaStream().useNextVideoDevice();
	}

	public void pauseLocalVideo() {
		localMedia.getLocalMediaStream().pauseVideo();
	}

	public void resumeLocalVoice() {
		localMedia.getLocalMediaStream().resumeAudio();
	}
	public void muteLocalVideo() {
		localMedia.getLocalMediaStream().muteVideo();
	}

	public void unMuteLocalVoice() {
		localMedia.getLocalMediaStream().unmuteVideo();
	}
	public void pauseLocalVoice() {
		localMedia.getLocalMediaStream().pauseAudio();
	}

	public void resumeLocalVideo() {
		localMedia.getLocalMediaStream().resumeVideo();
	}

	public void setSignalling(Signalling signalling) {
		if (this.signalling == null)
			this.signalling = signalling;
	}
}
