package sm.java.conference;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.imageio.ImageIO;

import jopus.OpusCodec;
import jvp8.Vp8Codec;

import org.jcodec.api.SequenceEncoder;

import sm.java.codec.WavHeader;
import sm.java.config.ConfigServer;
import sm.java.main.VideoChat;
import sm.java.ultility.CopyFile;
import sm.java.ultility.FileWriter;
import sm.java.ultility.SortFiles;
import sm.java.upload.FTPFileUpload;
import fm.ConsoleLogProvider;
import fm.DoubleAction;
import fm.EmptyFunction;
import fm.Log;
import fm.LogLevel;
import fm.SingleAction;
import fm.icelink.Conference;
import fm.icelink.LinkDownArgs;
import fm.icelink.LinkInitArgs;
import fm.icelink.LinkUpArgs;
import fm.icelink.Stream;
import fm.icelink.StreamLinkDownArgs;
import fm.icelink.StreamLinkInitArgs;
import fm.icelink.webrtc.AudioBuffer;
import fm.icelink.webrtc.AudioCodec;
import fm.icelink.webrtc.AudioStream;
import fm.icelink.webrtc.ImageUtility;
import fm.icelink.webrtc.LinkExtensions;
import fm.icelink.webrtc.MediaStream;
import fm.icelink.webrtc.VideoBuffer;
import fm.icelink.webrtc.VideoCodec;
import fm.icelink.webrtc.VideoStream;

public class AppVideo {
	private String icelinkServerAddress = ConfigServer.icelinkServerAddress;
	private String websyncServerUrl = ConfigServer.websyncRequestUrl;

	private Signalling signalling;
	private LocalMedia localMedia;
	private ArrayList<BufferedImage> al;
	private AudioStream audioStream;
	private VideoStream videoStream;
	private Conference conference;
	private String create_time;
	private String end_time;
	private String sessionId;
	private int countFrame = 0;
	private int countAudioFrame = 0;
	private SequenceEncoder se = null;
	private String fileName;
	private String answer_imei;

	public String getAnswer_imei() {
		return answer_imei;
	}

	public void setAnswer_imei(String answer_imei) {
		this.answer_imei = answer_imei;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(String sid) {
		this.sessionId = sid;
	}

	public ArrayList<BufferedImage> getAl() {
		return al;
	}

	public String getFileName() {
		return fileName;
	}

	public String getCreate_time() {
		return create_time;
	}

	public String getEnd_time() {
		return end_time;
	}

	private SingleAction<LinkInitArgs> logLinkInitEvent;
	private SingleAction<LinkUpArgs> logLinkUpEvent;
	private SingleAction<LinkDownArgs> logLinkDownEvent;
	private SingleAction<StreamLinkInitArgs> addRemoteVideoControlEvent;
	private SingleAction<StreamLinkDownArgs> removeRemoteVideoControlEvent;

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
		// removeRemoteVideoControlEvent = new
		// SingleAction<StreamLinkDownArgs>() {
		// public void invoke(StreamLinkDownArgs e) {
		// removeRemoteVideoControl(e);
		// }
		// };

		// Log to the console.
		Log.setProvider(new ConsoleLogProvider(LogLevel.Info));

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
		AudioStream.registerCodec("opus", 48000, 2,
				new EmptyFunction<AudioCodec>() {
					public AudioCodec invoke() {
						return new OpusCodec();
					}
				}, true);
	}

	private static AppVideo app;

	public static synchronized AppVideo getInstance() throws Exception {
		if (app == null) {
			app = new AppVideo();
		}
		return app;
	}

	public void startSignalling(SingleAction<String> callback) throws Exception {
		signalling = new Signalling(websyncServerUrl);
		signalling.start(callback);
	}

	public boolean signallingIsStarted() {
		return signalling != null;
	}

	public void stopSignalling(SingleAction<String> callback) throws Exception {
		signalling.stop(callback);
		signalling = null;
	}

	public void startLocalMedia(VideoChat videoChat,
			SingleAction<String> callback) throws Exception {
		localMedia = new LocalMedia();
		localMedia.start(videoChat, callback);
		this.create_time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",
				Locale.UK).format(new Date());
		this.fileName = create_time.replace("/", "").replace(":", "")
				.replace(" ", "_");
		File theDir = new File("data/pic");
		doMakeDirectory(theDir);
		theDir = new File("data/video/" + this.getFileName());
		doMakeDirectory(theDir);
		theDir = new File("data/audio");
		doMakeDirectory(theDir);
		theDir = new File("data/audio_message");
		doMakeDirectory(theDir);

	}

	// make directory
	private void doMakeDirectory(File theDir) {
		// if the directory does not exist, create it
		File file = new File("data");
		if (!(file.exists())) {
			System.out.println("creating directory: " + theDir);
			boolean result = false;

			try {
				file.mkdir();
				result = true;
			} catch (SecurityException se) {
				// handle it
			}
			if (result) {
				System.out.println("DIR created");
			}

		}

		file = new File("data/video");
		if (!(file.exists())) {
			System.out.println("creating directory: " + theDir);
			boolean result = false;

			try {
				file.mkdir();
				result = true;
			} catch (SecurityException se) {
				// handle it
			}
			if (result) {
				System.out.println("DIR created");
			}
		}
		if (!theDir.exists()) {
			System.out.println("creating directory: " + theDir);
			boolean result = false;

			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				// handle it
			}
			if (result) {
				System.out.println("DIR created");
			}
		}
	}

	public void stopLocalMedia(SingleAction<String> callback) throws Exception {
		this.end_time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.UK)
				.format(new Date());
		localMedia.stop(callback);
		localMedia = null;
		this.countFrame = 0;
		if (localMedia == null) {
			Thread t = new Thread(new Runnable() {

				public void run() {
					File folder = new File("data/video/" + getFileName());
					combineToMP4(folder);
					// WavHeader wh = new WavHeader(48000, 48000 * 2 * 16 / 8,
					// countAudioFrame * 2 * 16 / 8);
					//
					// new FileWriter("data/audio/" + getFileName()
					// + "_header.wav", wh.getHeader());
					// new FTPFileUpload(fileName, offer_imei, answer_imei,
					// create_time, "");
					new FTPFileUpload(getFileName(), getAnswer_imei(),
							ConfigServer.MAC, "pic-video-capture", getCreate_time(),
							getEnd_time());
				}
			});

			t.setDaemon(true);
			t.start();
			
		}

	}

	// this function will read all file in folder and combine to file .mp4
	public void combineToMP4(final File folder) {
		File dir = new File("data/video/" + getFileName() + ".mp4");

		try {
			se = new SequenceEncoder(dir);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		File[] files = folder.listFiles();

		// sort all file in current folder
		new SortFiles(files);
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				combineToMP4(files[i]);
			} else {

				try {
					se.encodeImage(ImageIO.read(files[i]));
					if (i == 0) {
						new CopyFile(new File(files[i].toString()), new File(
								"data/pic/" + getFileName() + ".jpg"));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					// delete this file is reading
					files[i].delete();

				}
			}
		}

		try {
			if (folder.listFiles().length == 0) {
				// delete this folder if it's empty
				folder.delete();
			}
			se.finish();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private File dirAudio = null;

	public void startConference(SingleAction<String> callback) throws Exception {
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

		// audio
		// if (localMedia != null) {
		//
		// localMedia.getLocalMediaStream().addOnAudioCaptured(
		// new DoubleAction<MediaStream, AudioBuffer>() {
		// public void invoke(MediaStream ms, AudioBuffer ab) {
		//
		// countAudioFrame++;
		// dirAudio = new File("data/audio/" + getFileName()
		// + "_data.wav");
		//
		// new FileWriter(dirAudio.toString(), ab.getData());
		// };
		// });
		// }
		// capture
		// localMedia.getLocalMediaStream().addOnVideoCaptured(
		// new DoubleAction<MediaStream, VideoBuffer>() {
		// @Override
		// public void invoke(MediaStream ms, VideoBuffer vb) {
		//
		// File dir = new File("data/pic/" + getFileName()
		// + ".jpg");
		//
		// try {
		// ImageIO.write(ImageUtility.bufferToBufferedImage(
		// vb, true), "jpg", dir);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// }
		//
		// });

		// video
		localMedia.getLocalMediaStream().addOnVideoCaptured(
				new DoubleAction<MediaStream, VideoBuffer>() {

					public void invoke(MediaStream ms, final VideoBuffer vb) {
						countFrame++;
						File dir = new File("data/video/" + getFileName() + "/"
								+ countFrame + ".jpg");

						try {

							ImageIO.write(ImageUtility.bufferToBufferedImage(
									vb, true), "jpg", dir);

						} catch (IOException ex) {
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}

					};
				});

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

		// Attach signalling to the conference.
		signalling.attach(conference, sessionId, callback);
	}

	private void addRemoteVideoControl(StreamLinkInitArgs e) {
		try {
			Component remoteVideoControl = (Component) LinkExtensions
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
}
