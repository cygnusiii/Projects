package sm.java.config;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigServer {
	public static String websyncRequestUrl = "";
	public static String icelinkServerAddress = "";
	public static String FTP_SERVER = "";
	public static String USER_FTP = "";
	public static String PASSWORD_FTP = "";
	public static String MAC = "";
	public static String PUBLIC_DEVICE_NAME = "";
	public static String DATABASE_SERVER = "117.6.131.222:1433";
	public static String DATABASE_NAME = "SmartHome";
	public static String DATABASE_USER = "sa";
	public static String DATABASE_PASSWORD = "iii2015win";

	public static void getDeivceInfo() {
		InetAddress ip;
		StringBuilder sb = new StringBuilder();
		try {

			ip = InetAddress.getLocalHost();
			System.out.println("Current IP address : " + ip.getHostAddress());

			NetworkInterface network = NetworkInterface.getByInetAddress(ip);

			byte[] mac = network.getHardwareAddress();

			System.out.print("Current MAC address : ");

			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i],
						(i < mac.length - 1) ? "-" : ""));
			}
			System.out.println(sb.toString());
			String sql = "Select name from shm_device_public Where imei='"
					+ sb.toString() + "'";
			ResultSet resultSet = DBAccess.ExecQuery(sql);

			String sql1 = "Select * from shm_config_server";
			ResultSet resultSet1 = DBAccess.ExecQuery(sql1);

			try {
				if (resultSet.next()) {
					MAC = sb.toString();
					PUBLIC_DEVICE_NAME = resultSet.getString(1);
				}

				if (resultSet1.next()) {
					icelinkServerAddress = resultSet1
							.getString("client_icelinkServer");
					websyncRequestUrl = "http://"
							+ resultSet1.getString("client_websyncServer")
							+ "/websync.ashx";
					FTP_SERVER = resultSet1.getString("ftpServerAddress");
					USER_FTP = resultSet1.getString("userFTP");
					PASSWORD_FTP = resultSet1.getString("passwordFTP");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (UnknownHostException e) {

			e.printStackTrace();

		} catch (SocketException e) {

			e.printStackTrace();

		}

	}
}