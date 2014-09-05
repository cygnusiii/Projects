package sm.java.audio_message;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import sm.java.upload.FTPFileUpload;

public class SoundRecorder {

	private static File outputFile;
	private static final long serialVersionUID = 1L;

	static protected boolean running;
	static ByteArrayOutputStream out;
	private String fileName;
	// strFilename = nowLong.toString();
	private static Recorder recorder;
	private FTPFileUpload ftpUpload;
	private String answer_imei;
	private String offer_imei;
	private String create_time;
	private File file = null;

	public SoundRecorder(String answer_imei,String offer_imei,String create_time ) {
		this.offer_imei = offer_imei;
		this.answer_imei = answer_imei;
		this.create_time = create_time;
		fileName = create_time.replace("/", "").replace(":", "").replace(" ", "_");
		System.out.println("Filename will be..." + fileName + ".wav");

		captureAudio(true);
	}

	public boolean recordIsAlive() {
		if (recorder.isAlive()) {
			return true;
		}

		return false;
	}

	public boolean isComplete() {
		return ftpUpload.isComplete;
	}

	public void stopAudioCapture() {
		recorder.stopRecording();
	}

	public void upload() {
		
		
		Thread t = new Thread(new Runnable() {

			public void run() {

				try {
					ftpUpload = new FTPFileUpload(fileName,offer_imei, answer_imei, create_time, "call-log");
				} catch (Exception e) {

				}
			}

		});

		t.start();
	}

	private void captureAudio(boolean capturing) {

		outputFile = new File("data/audio_message/"+fileName + ".wav");
		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		float rate = 44100.0f;
		int channels = 2;
		int frameSize = 4;
		int sampleSize = 16;
		boolean bigEndian = true;

		AudioFormat audioFormat = new AudioFormat(encoding, rate, sampleSize,
				channels, (sampleSize / 8) * channels, rate, bigEndian);

		DataLine.Info info = new DataLine.Info(TargetDataLine.class,
				audioFormat);
		TargetDataLine targetDataLine = null;

		try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
			targetDataLine.open(audioFormat);
		} catch (LineUnavailableException e) {
			System.out.println("unable to get a recording line");
			e.printStackTrace();
			System.exit(1);
		}

		AudioFileFormat.Type targetType = AudioFileFormat.Type.WAVE;

		recorder = new Recorder(targetDataLine, targetType, outputFile);

		System.out.println("Recording...");
		if (capturing) {
			recorder.start();
		}

	}

}