package sm.java.audio_message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import sm.java.config.DBAccess;
import sm.java.config.Settings;

public class AudioMessage {
	private String fileName;
	private boolean flag = false;
	private SoundRecorder soundRecorder;
	private JTextField txt_message;
	private JButton btn_message;
	private String offer_imei;
	private String answer_imei;
	private String time_stamp;

	public AudioMessage() {
		// TODO Auto-generated constructor stub
	}

	public AudioMessage(String offer_imei, JTextField txt_message,
			JButton btn_message, boolean flag) {
		this.offer_imei = offer_imei;
		this.txt_message = txt_message;
		this.btn_message = btn_message;
		this.flag = flag;
	}

	// this method will perform Recorder Audio message
	public void audioMessageRecorder() {

		if (flag) {
			if (!txt_message.getText().isEmpty()) {

				String sql = "Select * from shm_houseinfo Where room_number='"
						+ txt_message.getText() + "'";
				ResultSet result = DBAccess.ExecQuery(sql);
				try {
					if (result.next()) {
						answer_imei = result.getString("imei").toString();
						JOptionPane
								.showMessageDialog(null,
										"Lời nhắn sẽ tự động đóng trong vòng 60 giây nếu bạn không chọn Ngừng !");

						btn_message
								.setText("<html><p style='text-align:center'>Ngừng</p></html>");
						DateFormat df = new SimpleDateFormat(
								"yyyy/MM/dd HH:mm:ss");
						Date dateobj = new Date();
						time_stamp = df.format(dateobj).toString();
						fileName = new SimpleDateFormat("yyyMMdd_HHmmss",
								Locale.UK).format(new Date());
						soundRecorder =new SoundRecorder(answer_imei, offer_imei, time_stamp);
						Timer timer = new Timer();

						TimerTask delayedThreadStartTask = new TimerTask() {
							@Override
							public void run() {

								// captureCDRProcess();
								// moved to TimerTask
								new Thread(new Runnable() {
									public void run() {
										if (soundRecorder.recordIsAlive()) {
											soundRecorder.stopAudioCapture();
											flag = true;

											JOptionPane
													.showMessageDialog(null,
															"Gửi lời nhắn thành công !");

											btn_message
													.setText("<html><p style='text-align:center'>Gửi lời nhắn</p></html>");
											txt_message.setText("");
											soundRecorder.stopAudioCapture();
											Thread t = new Thread(
													new Runnable() {

														public void run() {
															soundRecorder.upload();
														}

													});

											t.setDaemon(true);
											t.start();

										}
									}
								}).start();

							}
						};

						timer.schedule(delayedThreadStartTask, Settings.AUDIO_MESSAGE_TIME_OUT * 1000); // 1 minute
					
						flag = false;

					} else {
						JOptionPane.showMessageDialog(null,
								"Số phòng không tồn !");
					}
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(null, "Hãy nhập số phòng !");
			}
		} else {
			if (soundRecorder.recordIsAlive()) {
				flag = true;

				JOptionPane.showMessageDialog(null,
						"Gửi lời nhắn thành công !");
				btn_message
						.setText("<html><p style='text-align:center'>Gửi lời nhắn</p></html>");
				txt_message.setText("");
				soundRecorder.stopAudioCapture();
				Thread t = new Thread(new Runnable() {
					public void run() {
						soundRecorder.upload();

					}
				});
				t.setDaemon(true);
				t.start();
			}

		}

	}

}
