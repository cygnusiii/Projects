package sm.java.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;

import sm.java.conference.AppChat;
import sm.java.config.ConfigServer;
import sm.java.config.DBAccess;
import sm.java.config.Encrypt;

public class FTPFileUpload {
	FTPClient client = new FTPClient();
	FTPClient client2 = new FTPClient();
	FileInputStream fis = null;
	FileInputStream fis2 = null;
	boolean result;
	boolean result2;
	String ftpServerAddress = ConfigServer.FTP_SERVER;
	String userName = ConfigServer.USER_FTP;
	String password = ConfigServer.PASSWORD_FTP;
	private String answer_imei;
	private String offer_imei;
	private String create_time;
	private String end_time;
	private File fileName = null;
	private File fileNameVideo = null;
	String type;
	private AppChat appChat;
	File file;
	private String path = "";
	private String path2 = "";
	public boolean isComplete = false;

	public FTPFileUpload(String fileName, String answer_imei,
			String offer_imei, String type, String create_time, String end_time) {
		this.fileName = new File(fileName + ".jpg");
		this.fileNameVideo = new File(fileName + ".mp4");
		this.answer_imei = answer_imei;
		this.offer_imei = offer_imei;
		this.create_time = create_time;
		this.end_time = end_time;
		this.type = type;
		try {
			UploadTask();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FTPFileUpload(String fileName, String offer_imei,
			String answer_imei, String create_time, String type) {
		this.fileName = new File(fileName + ".wav");
		this.answer_imei = answer_imei;
		this.offer_imei = offer_imei;
		this.create_time = create_time;
		this.type = type;
		try {
			UploadTask();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public File getFileName() {
		return fileName;
	}

	public File getFileNameVideo() {
		return fileNameVideo;
	}

	public void UploadTask() throws IOException {

		try {
			try {
				appChat = AppChat.getInstance();

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			client.connect(ftpServerAddress);
			result = client.login(userName, password);

			if (result == true) {
				System.out.println("Successfully logged in!");
			} else {
				System.out.println("Login Fail!");
				return;
			}

			client.setFileType(FTP.BINARY_FILE_TYPE);
			if (this.type.equalsIgnoreCase("call-log")) {
				try {
					path = "data/audio_message/";
					client.changeWorkingDirectory("/media/audio_message/");
					file = getFileName();
					fis = new FileInputStream(path + file);
					// Upload file to the ftp server
					result = client.storeFile(file.getName(), fis);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (result == true) {
					System.out.println("File is uploaded successfully");
					appChat.sendMessage(Encrypt
							.getMD5(answer_imei + "call-log"));

					// update to database
					String sql = "INSERT INTO shm_call_log(offer_imei,answer_imei,create_time,flag,audio_message,image) VALUES('"
							+ offer_imei
							+ "','"
							+ answer_imei
							+ "','"
							+ create_time
							+ "','0','"
							+ fileName
							+ "','"
							+ fileName.toString().replace(".wav", ".jpg")
							+ "')";
					insertToDB(sql);

				} else {
					System.out.println("File uploading failed");
				}
			}

			if (this.type.equalsIgnoreCase("pic-video-capture")) {
				client2.connect(ftpServerAddress);
				result2 = client2.login(userName, password);

				if (result2) {
					System.out.println("Successfully logged in!");
				} else {
					System.out.println("Login Fail!");
					return;
				}

				client2.setFileType(FTP.BINARY_FILE_TYPE);
				
						try {
							path = "data/pic/";
							client.changeWorkingDirectory("/media/capture/");
							fis = new FileInputStream(path + fileName);
							// Upload file to the ftp server
							result = client.storeFile(fileName.toString(), fis);

							path2 = "data/video/";
							client2.changeWorkingDirectory("/media/video/");
							fis2 = new FileInputStream(path2 + fileNameVideo);
							// Upload file to the ftp server
							result = client2.storeFile(
									fileNameVideo.toString(), fis2);
							isComplete = result;

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}


				if (isComplete) {
					System.out.println("File is uploaded successfully");
					// update to database
					String sql = "INSERT INTO shm_conference(offer_imei,answer_imei,start_time,end_time,picture,video,path,flag) "
							+ "VALUES('"
							+ offer_imei
							+ "','"
							+ answer_imei
							+ "','"
							+ create_time
							+ "','"
							+ end_time
							+ "','"
							+ fileName
							+ "','"
							+ fileNameVideo
							+ "','http://117.6.131.222:6789/smarthomews/media/','0')";
					insertToDB(sql);

				} else {
					System.out.println("File uploading failed");
				}

			}

			client.logout();
			client2.logout();
		} catch (FTPConnectionClosedException e) {
			e.printStackTrace();
		} finally {
			try {
				client.disconnect();
				fis.close();
				client2.disconnect();
				fis2.close();

				boolean check = new File(path + fileName).delete();
				File file = new File(path2 + fileNameVideo);
				if (file.isFile()) {
					file.delete();
				}
				if (check) {
					System.out.println("File is deleted successfully");
				}
			} catch (FTPConnectionClosedException e) {
				System.out.println(e);
			}
		}
	}

	private void insertToDB(String sql) {

		String result = DBAccess.ExcecNonQuery(sql);
		if (result.endsWith("True")) {
			System.out.println("ok");
		}
	}

}