package tools;

import java.sql.*;

public class MySQLConnector {
	private static Connection connection;
	private static final String ADDRESS = "localhost";
	private static final String DATABASE_NAME = "QLBanHang";
	private static final String USERNAME = "ndong";
	private static final String PASSWORD = "minhthu2610";
	
	public MySQLConnector() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			String connectionString = "jdbc:mysql://" + ADDRESS + "/" + DATABASE_NAME
								+ "?user=" + USERNAME + "&password=" + PASSWORD
								+ "&useUnicode=true&characterEncoding=utf-8";
			connection = DriverManager.getConnection(connectionString);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Connection getConnection() {
		return connection;
	}
	
//	public static void main(String[] args) {
//		MySQLConnector connector = new  MySQLConnector();
//		
//	}
}
