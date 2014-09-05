package sm.java.main;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import sm.java.audio_message.AudioMessage;
import sm.java.conference.AppChat;
import sm.java.conference.AppVideo;
import sm.java.config.ConfigChannel;
import sm.java.config.ConfigServer;
import sm.java.config.DBAccess;
import sm.java.config.Encrypt;
import fm.EmptyAction;
import fm.Serializer;
import fm.SingleAction;
import fm.icelink.BaseLinkArgs;
import fm.icelink.Stream;
import fm.icelink.StreamFormat;
import fm.icelink.StreamType;
import fm.icelink.webrtc.ImageUtility;
import fm.icelink.webrtc.LocalMediaStream;
import fm.icelink.webrtc.VideoBuffer;
import fm.icelink.websync.BaseLinkArgsExtensions;
import fm.websync.Client;
import fm.websync.Record;

public class VideoChat extends JFrame {
	static boolean useWebSyncExtension = true;
	private final String PREFIX_VIDEO = ConfigChannel.PREFIX_VIDEO;
	private final String OFFER_VIDEO = ConfigChannel.OFFER_VIDEO;
	private JTextField txt_roomNumber, txt_numberVisit, txt_message;
	private JTextArea txt_Log;
	private JLabel lbl_roomNumber, lbl_numberVisit, lbl_message;
	private JButton btn_call, btn_delRoom, btn_delVisit, btn_message,
			btn_delMessage;
	LocalMediaStream localMedia;
	private String IMEI;
	private Boolean isCall = false;
	Client client;
	private ExecutorService background = Executors.newSingleThreadExecutor();
	private JPanel panel;
	private AppVideo appVideo;
//	public JComboBox<String> audioDevices;
//	public JComboBox<String> videoDevices;
	public JLayeredPane container;
	private Container container_message_2;

	private AudioMessage audioMessage;
	private AppChat appChat;

	public VideoChat() {
		initializeComponent();
		try {
			appVideo = AppVideo.getInstance();

			// sessionIdLabel.setText(app.getSessionId());

			// Start local media when the form opens.

		} catch (Exception ex) {
			alert(ex.getMessage());
		}

		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (UnsupportedLookAndFeelException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		btn_call.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (txt_roomNumber.getText().isEmpty())
					JOptionPane.showMessageDialog(null,
							"Thông báo : Phải nhập số phòng");
				else {
					String sql = "Select * from shm_houseinfo Where room_number='"
							+ txt_roomNumber.getText() + "'";
					ResultSet result = DBAccess.ExecQuery(sql);
					try {
						if (result.next() && !isCall) {
							IMEI = result.getString("imei");
							send(Encrypt.getMD5(OFFER_VIDEO + IMEI));
							String nameChannel = Encrypt.getMD5(PREFIX_VIDEO
									+ IMEI + ConfigServer.MAC);
							appVideo.setSessionId(nameChannel);
							appVideo.setAnswer_imei(IMEI);
							startSignalling();
							startLocalMedia();
							isCall = true;
							btn_call.setText("Ngắt Gọi");
							container_message_2.setVisible(false);

						} else {

							try {

								stopLocalMedia();
								stopSignalling();
							
								btn_call.setText("Gọi");
								txt_roomNumber.setText("");
								panel.remove(container);
								container = new JLayeredPane();
								container.setBackground(java.awt.Color.black);
								container.setOpaque(true);
								panel.add(container, 0);
								container_message_2.setVisible(true);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							if (!isCall)
								JOptionPane
										.showMessageDialog(null,
												"Thông báo : Không tìm thấy số phòng cần gọi ");
							isCall = false;

						}

					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
	}

	
	private void startLocalMedia() {
		try {
			final VideoChat self = this;
			appVideo.startLocalMedia(this, new SingleAction<String>() {
				public void invoke(String error) {
					if (error != null) {
						alert(error);
					} else {
						// Stop local media when the form closes.
						addOnClosing(self, new EmptyAction() {
							public void invoke() {
								stopLocalMedia();
							}
						});

						// Start conference now that the local media is
						// available.
						startConference();
					}
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void startSignalling() {
		try {
			final VideoChat self = this;
			appVideo.startSignalling(new SingleAction<String>() {
				public void invoke(String error) {
					if (error != null) {
						alert(error);
					} else {
						// Stop signalling when the form closes.
						addOnClosing(self, new EmptyAction() {
							public void invoke() {
								stopSignalling();
							}
						});
					}
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void stopSignalling() {
		try {
			appVideo.stopSignalling(new SingleAction<String>() {
				public void invoke(String error) {
					if (error != null) {
						alert(error);
					}
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void stopLocalMedia() {
		try {
			appVideo.stopLocalMedia(new SingleAction<String>() {
				public void invoke(String error) {
					if (error != null) {
						alert(error);
					}
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void startConference() {
		try {
			final VideoChat self = this;
			appVideo.startConference(new SingleAction<String>() {
				public void invoke(String error) {
					if (error != null) {
						alert(error);
					} else {
						// Stop conference when the form closes.
						addOnClosing(self, new EmptyAction() {
							public void invoke() {
								stopConference();
							}
						});
					}
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void addOnClosing(Window window, final EmptyAction action) {
		window.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {
			}

			public void windowClosed(WindowEvent e) {
			}

			public void windowClosing(WindowEvent e) {
				action.invoke();
			}

			public void windowDeactivated(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowOpened(WindowEvent e) {
			}
		});
	}

	private void addOnOpened(Window window, final EmptyAction action) {
		window.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {
			}

			public void windowClosed(WindowEvent e) {
			}

			public void windowClosing(WindowEvent e) {
			}

			public void windowDeactivated(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowOpened(WindowEvent e) {
				action.invoke();
			}
		});
	}

	private void stopConference() {
		try {
			appVideo.stopConference(new SingleAction<String>() {
				public void invoke(String error) {
					if (error != null) {
						alert(error);
					}
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void send(final String message) {
		background.execute(new Runnable() {
			public void run() {
				try {
					appChat.sendMessage(message);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	static String getPeerName(BaseLinkArgs e) {
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

	static String getPeerImei(BaseLinkArgs e) {
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

	static void alert(String format, Object... args) {
		JOptionPane.showMessageDialog(null, String.format(format, args));
	}

	public void initializeComponent() {
		// get device info from database
		ConfigServer.getDeivceInfo();
		try {
			txt_Log = new JTextArea();
			appChat = AppChat.getInstance();
			appChat.setSessionId(ConfigChannel.PUBLIC_CHANNEL);
			Stream textStream = new Stream(StreamType.Text, new StreamFormat(
					"utf8"));
			appChat.setTextStream(textStream);
			appChat.setTxt_Log(txt_Log);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.out.print(e1.toString());
		}
		setTitle("Smart Home");
		setUndecorated(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE & DO_NOTHING_ON_CLOSE);
		// setResizable(false);

//		audioDevices = new JComboBox<String>();
//		videoDevices = new JComboBox<String>();

		this.setLocationRelativeTo(null);
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		panel = new JPanel(new GridLayout(2, 2));
		panel.setBackground(Color.BLUE);

		txt_Log.setDisabledTextColor(Color.WHITE);
		txt_Log.setFont(new Font("Arial", Font.BOLD, 16));
		txt_Log.setBackground(new Color(104, 0, 255));
		txt_Log.setEnabled(false);
		txt_Log.setText("DANH SÁCH CHỜ\nSỐ PHÒNG\n");

		// -------------container for call--------------//

		Container container_call = new Container();
		container_call.setLayout(new GridLayout(2, 1));

		// ------------container call 1--------------//
		Container container_call_1 = new Container();
		container_call_1.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.insets = new Insets(5, 5, 5, 5);

		// room number label

		lbl_roomNumber = new JLabel("Nhập số phòng tới thăm: ");
		lbl_roomNumber.setFont(new Font("Arial", Font.BOLD, 16));
		lbl_roomNumber.setForeground(Color.WHITE);
		c1.anchor = GridBagConstraints.PAGE_END;
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.gridwidth = 3;
		c1.gridx = 0;
		c1.gridy = 1;
		c1.weightx = 0.4;
		c1.weighty = 0.0;
		container_call_1.add(lbl_roomNumber, c1);

		// room number text
		txt_roomNumber = new JTextField();
		txt_roomNumber.setEnabled(false);
		txt_roomNumber.setDisabledTextColor(Color.BLACK);
		txt_roomNumber.setPreferredSize(new Dimension(100, 40));
		txt_roomNumber.setFont(new Font("Arial", Font.BOLD, 16));

		c1.anchor = GridBagConstraints.PAGE_END;
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.gridwidth = 2;
		c1.gridx = 3;
		c1.gridy = 1;
		c1.weightx = 0.2;
		c1.weighty = 0.0;
		container_call_1.add(txt_roomNumber, c1);

		// button delete room number

		btn_delRoom = new JButton("Xóa");
		btn_delRoom.setPreferredSize(new Dimension(100, 40));
		btn_delRoom.setFont(new Font("Arial", Font.BOLD, 16));
		btn_delRoom.setForeground(Color.RED);
		btn_delRoom.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (txt_roomNumber.getText().length() > 0)
					txt_roomNumber.setText(txt_roomNumber.getText().substring(
							0, txt_roomNumber.getText().length() - 1));
			}
		});
		c1.anchor = GridBagConstraints.PAGE_END;
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.gridwidth = 2;
		c1.gridx = 5;
		c1.gridy = 1;
		c1.weightx = 0.1;
		c1.weighty = 0.0;
		container_call_1.add(btn_delRoom, c1);

		for (int i = 0; i <= 9; i++) {
			int gridy = 2;
			if (i < 5)
				gridy = 2;
			else
				gridy = 3;
			c1.anchor = GridBagConstraints.PAGE_END;
			c1.fill = GridBagConstraints.HORIZONTAL;
			c1.gridwidth = 1;
			c1.gridx = (i < 5) ? i : i - 5;
			c1.gridy = gridy;
			c1.weightx = 0.05;
			c1.weighty = 0.0;
			final JButton temp = new JButton(String.valueOf(i));
			temp.setPreferredSize(new Dimension(100, 40));
			temp.setForeground(Color.RED);
			temp.setFont(new Font("Arial", Font.BOLD, 30));
			container_call_1.add(temp, c1);

			temp.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					if (txt_roomNumber.getText().length() < 4)
						txt_roomNumber.setText(txt_roomNumber.getText()
								+ temp.getText());
				}
			});
		}

		// ------------container call 2--------------//
		Container container_call_2 = new Container();
		container_call_2.setLayout(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets = new Insets(5, 5, 5, 5);

		// number visit label

		lbl_numberVisit = new JLabel("Nhập số người tới thăm: ");
		lbl_numberVisit.setFont(new Font("Arial", Font.BOLD, 16));
		lbl_numberVisit.setForeground(Color.WHITE);
		c2.anchor = GridBagConstraints.PAGE_END;
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.gridwidth = 3;
		c2.gridx = 0;
		c2.gridy = 1;
		c2.weightx = 0.4;
		c2.weighty = 0.0;
		container_call_2.add(lbl_numberVisit, c2);

		// number visit text
		txt_numberVisit = new JTextField();
		txt_numberVisit.setEnabled(false);
		txt_numberVisit.setDisabledTextColor(Color.BLACK);
		txt_numberVisit.setPreferredSize(new Dimension(100, 40));
		txt_numberVisit.setPreferredSize(new Dimension(100, 40));
		txt_numberVisit.setFont(new Font("Arial", Font.BOLD, 16));

		c2.anchor = GridBagConstraints.PAGE_END;
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.gridwidth = 2;
		c2.gridx = 3;
		c2.gridy = 1;
		c2.weightx = 0.2;
		c2.weighty = 0.0;
		container_call_2.add(txt_numberVisit, c2);

		// button delete visit
		btn_delVisit = new JButton("Xóa");
		btn_delVisit.setPreferredSize(new Dimension(100, 40));
		btn_delVisit.setFont(new Font("Arial", Font.BOLD, 16));
		btn_delVisit.setForeground(Color.RED);
		btn_delVisit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (txt_numberVisit.getText().length() > 0)
					txt_numberVisit.setText(txt_numberVisit.getText()
							.substring(0,
									txt_numberVisit.getText().length() - 1));
			}
		});
		c2.anchor = GridBagConstraints.PAGE_END;
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.gridwidth = 2;
		c2.gridx = 5;
		c2.gridy = 1;
		c2.weightx = 0.1;
		c2.weighty = 0.0;
		container_call_2.add(btn_delVisit, c2);
		for (int i = 0; i <= 9; i++) {
			int gridy = 2;
			if (i < 5)
				gridy = 2;
			else
				gridy = 3;
			c2.anchor = GridBagConstraints.PAGE_END;
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridwidth = 1;
			c2.gridx = (i < 5) ? i : i - 5;
			c2.gridy = gridy;
			c2.weightx = 0.05;
			c2.weighty = 0.0;
			final JButton temp = new JButton(String.valueOf(i));
			temp.setForeground(Color.RED);
			temp.setFont(new Font("Arial", Font.BOLD, 30));
			temp.setPreferredSize(new Dimension(100, 40));
			container_call_2.add(temp, c2);

			temp.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					if (txt_numberVisit.getText().length() < 4)
						txt_numberVisit.setText(txt_numberVisit.getText()
								+ temp.getText());
				}
			});
		}
		// ----button call------///
		btn_call = new JButton("Gọi");
		btn_call.setPreferredSize(new Dimension(100, 40));
		btn_call.setFont(new Font("Arial", Font.BOLD, 16));
		btn_call.setForeground(Color.RED);
		c2.anchor = GridBagConstraints.PAGE_END;
		c2.fill = GridBagConstraints.VERTICAL;
		c2.gridheight = 2;
		c2.gridx = 5;
		c2.gridy = 2;
		c2.weightx = 0.1;
		c2.weighty = 0.0;
		container_call_2.add(btn_call, c2);

		container_call.add(container_call_1);
		container_call.add(container_call_2);
		// -------------container for message--------------//

		Container container_message = new Container();
		container_message.setLayout(new GridLayout(2, 1));

		// ------------container message 1--------------//
		Container container_message_1 = new Container();
		container_message_1.setLayout(new GridBagLayout());
		GridBagConstraints c3 = new GridBagConstraints();
		c3.insets = new Insets(5, 5, 5, 5);
		String[] notice = new String[] {
				"QUÝ KHÁCH CÓ THỂ ĐỂ LẠI LỜI NHẮN NẾU CHỦ NHÀ ĐI VĂNG",
				"1. NHẬP SỐ PHÒNG QUÝ KHÁCH MUỐN GỬI LẠI LỜI NHẮN",
				"	2. SAU TIẾNG BIP QUÝ KHÁCH NÓI LỜI NHẮN VÀO MIC",
				"	3. NHẤN PHÍM GỬI LỜI NHẮN" };
		for (int i = 0; i < notice.length; i++) {

			c3.anchor = GridBagConstraints.PAGE_END;
			c3.fill = GridBagConstraints.HORIZONTAL;
			c3.gridwidth = 7;
			c3.gridx = (i < 5) ? i : i - 5;
			c3.gridy = i;
			c3.weightx = 0.05;
			c3.weighty = 0.0;
			JLabel temp = new JLabel(notice[i]);
			temp.setForeground(Color.WHITE);
			temp.setFont(new Font("Arial", Font.BOLD, 20));
			temp.setPreferredSize(new Dimension(100, 40));
			container_message_1.add(temp, c3);
		}

		// ------------container message 2--------------//
		container_message_2 = new Container();
		container_message_2.setLayout(new GridBagLayout());
		GridBagConstraints c4 = new GridBagConstraints();
		c4.insets = new Insets(5, 5, 5, 5);
		lbl_message = new JLabel("Nhập số phòng gửi tin nhắn: ");
		lbl_message.setFont(new Font("Arial", Font.BOLD, 16));
		lbl_message.setForeground(Color.WHITE);

		c4.anchor = GridBagConstraints.PAGE_END;
		c4.fill = GridBagConstraints.HORIZONTAL;
		c4.gridwidth = 3;
		c4.gridx = 0;
		c4.gridy = 1;
		c4.weightx = 0.4;
		c4.weighty = 0.0;
		container_message_2.add(lbl_message, c4);

		// message text
		txt_message = new JTextField();
		txt_message.setEnabled(false);
		txt_message.setDisabledTextColor(Color.BLACK);
		txt_message.setPreferredSize(new Dimension(100, 40));
		txt_message.setFont(new Font("Arial", Font.BOLD, 16));

		c4.anchor = GridBagConstraints.PAGE_END;
		c4.fill = GridBagConstraints.HORIZONTAL;
		c4.gridwidth = 2;
		c4.gridx = 3;
		c4.gridy = 1;
		c4.weightx = 0.2;
		c4.weighty = 0.0;
		container_message_2.add(txt_message, c4);

		// button delete message
		btn_delMessage = new JButton("Xóa");
		btn_delMessage.setPreferredSize(new Dimension(100, 40));
		btn_delMessage.setFont(new Font("Arial", Font.BOLD, 16));
		btn_delMessage.setForeground(Color.RED);
		btn_delMessage.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (txt_message.getText().length() > 0)
					txt_message.setText(txt_message.getText().substring(0,
							txt_message.getText().length() - 1));
			}
		});
		c4.anchor = GridBagConstraints.PAGE_END;
		c4.fill = GridBagConstraints.HORIZONTAL;
		c4.gridwidth = 2;
		c4.gridx = 5;
		c4.gridy = 1;
		c4.weightx = 0.1;
		c4.weighty = 0.0;
		container_message_2.add(btn_delMessage, c4);
		for (int i = 0; i <= 9; i++) {
			int gridy = 2;
			if (i < 5)
				gridy = 2;
			else
				gridy = 3;
			c4.anchor = GridBagConstraints.PAGE_END;
			c4.fill = GridBagConstraints.HORIZONTAL;
			c4.gridwidth = 1;
			c4.gridx = (i < 5) ? i : i - 5;
			c4.gridy = gridy;
			c4.weightx = 0.05;
			c4.weighty = 0.0;
			final JButton temp = new JButton(String.valueOf(i));
			temp.setForeground(Color.RED);
			temp.setFont(new Font("Arial", Font.BOLD, 30));
			temp.setPreferredSize(new Dimension(100, 40));
			container_message_2.add(temp, c4);

			temp.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					if (txt_message.getText().length() < 4)
						txt_message.setText(txt_message.getText()
								+ temp.getText());
				}
			});
		}
		// ----button message------///
		btn_message = new JButton(
				"<html><p style='text-align:center'>Gửi lời nhắn</p></html>");
		btn_message.setPreferredSize(new Dimension(100, 40));
		btn_message.setFont(new Font("Arial", Font.BOLD, 16));
		btn_message.setForeground(Color.RED);
		// AudioMessage instancing
		audioMessage = new AudioMessage(ConfigServer.MAC, txt_message,
				btn_message, true);
		btn_message.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// audio message recorder
				audioMessage.audioMessageRecorder();

			}
		});
		c4.anchor = GridBagConstraints.PAGE_END;
		c4.fill = GridBagConstraints.VERTICAL;
		c4.gridheight = 2;
		c4.gridx = 5;
		c4.gridy = 2;
		c4.weightx = 0.1;
		c4.weighty = 0.0;
		container_message_2.add(btn_message, c4);

		container_call.add(container_call_1);
		container_call.add(container_call_2);
		container_message.add(container_message_1);
		container_message.add(container_message_2);

		container = new JLayeredPane();
		container.setBackground(java.awt.Color.black);
		container.setOpaque(true);
		panel.add(container);
		panel.add(container_call);
		panel.add(txt_Log);
		panel.add(container_message);
		this.add(panel);
	}

	private static final long serialVersionUID = 1L;
}
