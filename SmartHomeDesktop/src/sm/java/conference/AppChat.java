package sm.java.conference;

import java.util.HashMap;

import javax.swing.JTextArea;

import sm.java.config.ConfigChannel;
import sm.java.config.ConfigServer;

import fm.*;
import fm.Serializer;
import fm.icelink.*;
import fm.icelink.websync.BaseLinkArgsExtensions;
import fm.icelink.websync.ClientExtensions;
import fm.icelink.websync.JoinConferenceArgs;
import fm.icelink.websync.JoinConferenceFailureArgs;
import fm.icelink.websync.JoinConferenceSuccessArgs;
import fm.websync.BindArgs;
import fm.websync.Client;
import fm.websync.ConnectArgs;
import fm.websync.ConnectFailureArgs;
import fm.websync.ConnectSuccessArgs;
import fm.websync.DisconnectArgs;
import fm.websync.Record;

public class AppChat {
	private String icelinkServerAddress = ConfigServer.icelinkServerAddress;
	private String websyncServerUrl = ConfigServer.websyncRequestUrl;

	private JTextArea txt_Log;
	private Signalling signalling;
	private Conference conference;
	private Stream textStream;

	public void setTxt_Log(JTextArea txt_Log) {
		this.txt_Log = txt_Log;
	}
	
	public Conference getConference() {
		return conference;
	}

	public Stream getTextStream() {
		return this.textStream;
	}

	public void setTextStream(Stream textStream) {
		this.textStream = textStream;
	}

	private String sessionId;

	public String getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(String sid) {
		this.sessionId = sid;
	}

	private boolean useWebSyncExtension = true;

	private AppChat() throws Exception {
		try {
			

			// Log to the console.
			Log.setProvider(new ConsoleLogProvider(LogLevel.Info));

			// Create a custom stream description that indicates text
			// data will be sent using a format labelled "utf8".
			textStream = new Stream(StreamType.Text, new StreamFormat("utf8"));
			textStream
					.addOnLinkReceiveRTP(new SingleAction<StreamLinkReceiveRTPArgs>() {
						public void invoke(StreamLinkReceiveRTPArgs e) {
							writeLine(
									"%s: %s",
									getPeerName(e),
									Encoding.getUTF8().getString(
											e.getPacket().getPayload()));
						}
					});

			// Create a conference using our stream description.
			conference = new Conference(icelinkServerAddress,
					new Stream[] { textStream });

			// Supply TURN relay credentials in case we are behind a
			// highly restrictive firewall. These credentials will be
			// verified by the TURN server.
			conference.setRelayUsername("test");
			conference.setRelayPassword("pa55w0rd!");

			// Add a few event handlers to the conference so we can see
			// when a new P2P link is created or changes state.
			conference.addOnLinkInit(new SingleAction<LinkInitArgs>() {
				public void invoke(LinkInitArgs e) {
					// Log.info("Link to " + getPeerName(e) +
					// " initializing...");
					// writeLine("Link to %s is initializing....",
					// getPeerName(e));
				}
			});
			conference.addOnLinkUp(new SingleAction<LinkUpArgs>() {
				public void invoke(LinkUpArgs e) {
					/*
					 * Log.info("Link to " + getPeerName(e) + " is UP.");
					 * Log.info("Type a message and hit Enter to send.");
					 */
					// writeLine("Link to %s is UP.", getPeerName(e));
					if (txt_Log.getText().contains(getPeerName(e) + "\n")) {
						txt_Log.setText(txt_Log.getText().replace(
								getPeerName(e) + "\n", ""));
						txt_Log.setText(txt_Log.getText() + getPeerName(e)
								+ "\n");
					} else {
						txt_Log.setText(txt_Log.getText() + getPeerName(e)
								+ "\n");
					}
				}
			});
			conference.addOnLinkDown(new SingleAction<LinkDownArgs>() {
				public void invoke(LinkDownArgs e) {
					// Log.info("Link to " + getPeerName(e) + " is DOWN. " +
					// e.getException().getMessage());
					// writeLine("Link to %s is DOWN.", getPeerName(e));
					txt_Log.setText(txt_Log.getText().replace(
							getPeerName(e) + "\n", ""));
				}
			});

			// Before we can create a P2P link, the peers have to exchange
			// some information - specifically descriptions of the streams
			// (called the offer/answer) and some local network addresses
			// (called candidates). IceLink generates this information
			// automatically, but you are responsible for distributing it
			// to the peer as quickly as possible. This is called "signalling".

			// We're going to use WebSync here, but other popular options
			// include SIP and XMPP - any real-time messaging system will do.
			// We use WebSync since it uses HTTP (WebSockets/long-polling) and
			// therefore has no issues with firewalls or connecting from
			// JavaScript-based web applications.

			// Create a WebSync client and establish a persistent
			// connection to the server.
			final Client client = new Client(websyncServerUrl);
			client.connect(new ConnectArgs() {
				{
					setOnSuccess(new SingleAction<ConnectSuccessArgs>() {
						public void invoke(ConnectSuccessArgs e) {
							// writeLine("Connected to WebSync.");
						}
					});
					setOnFailure(new SingleAction<ConnectFailureArgs>() {
						public void invoke(ConnectFailureArgs e) {
							writeLine("Could not connect to WebSync. %s", e
									.getException().getMessage());

							e.setRetry(false);
						}
					});
				}
			});

			// Disconnect automatically when the runtime shuts down.
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						client.disconnect(new DisconnectArgs() {
							{
								setSynchronous(new NullableBoolean(true));
							}
						});
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			// Bind the user-supplied name to the WebSync client. Later,
			// when linking, we will store a peer's bound records in the
			// IceLink peer state so we can get their name at any time.
			client.bind(new BindArgs("name", Serializer
					.serializeString(ConfigServer.PUBLIC_DEVICE_NAME)));
			client.bind(new BindArgs("imei", Serializer.serializeString(ConfigServer.MAC)));
			// IceLink includes a WebSync client extension that will
			// automatically manage session negotiation for you. If
			// you are not using WebSync, see the 'else' block for a
			// session negotiation template.
			if (useWebSyncExtension) {
				// Manage the conference automatically using a WebSync
				// channel. P2P links will be created automatically to
				// peers that join the same channel.
				ClientExtensions.joinConference(client, new JoinConferenceArgs(
						"/" + ConfigChannel.PUBLIC_CHANNEL, conference) {
					{
						setOnSuccess(new SingleAction<JoinConferenceSuccessArgs>() {
							public void invoke(JoinConferenceSuccessArgs e) {
								// writeLine("Joined conference %s.",e.getConferenceChannel());
							}
						});
						setOnFailure(new SingleAction<JoinConferenceFailureArgs>() {
							public void invoke(JoinConferenceFailureArgs e) {
								writeLine("Could not join conference %s.",
										e.getConferenceChannel());
							}
						});
					}
				});
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static AppChat app;

	public static synchronized AppChat getInstance() throws Exception {
		if (app == null) {
			app = new AppChat();
		}
		return app;
	}

	public void sendMessage(String message) {
		try {
			// Send message to the conference links.
			conference.sendRTP(textStream, textStream.getFormat(),
					new RTPPacket(Encoding.getUTF8().getBytes(message)));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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

	private void writeLine(String format, Object... args) {
		System.out.println(String.format(format, args) + "\n");
	}

	public Signalling getSignalling() {
		return this.signalling;
	}

}
