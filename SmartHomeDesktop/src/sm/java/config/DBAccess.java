package sm.java.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;



public class DBAccess {
	private static final String connectionString = "jdbc:sqlserver://"+ConfigServer.DATABASE_SERVER+";DatabaseName="+ConfigServer.DATABASE_NAME;
    private static Connection conn = null;
    	public static ResultSet ExecQuery(String sql){
            try{
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
                conn = DriverManager.getConnection(connectionString,ConfigServer.DATABASE_USER,ConfigServer.DATABASE_PASSWORD);
                Statement s = conn.createStatement();
                s.executeQuery(sql);
                return s.getResultSet();
            }catch(Exception ex){
                return null;
            }

	}
	public static String ExcecNonQuery(String sql){
            try{
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
                conn = DriverManager.getConnection(connectionString,ConfigServer.DATABASE_USER,ConfigServer.DATABASE_PASSWORD);
                Statement s = conn.createStatement();
                s.executeUpdate(sql);
                s.close();
                conn.close();
                return "True";
            }catch(Exception ex){
                return ex.toString();
            }
	}
    
}
