package cloudDownload;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Db {
	private final static String url = "jdbc:mysql://localhost:3306/cloudDownload";
	private final static String user = "root";
	private final static String password = "root";

	public static void main(String[] args) {
		Connection con = null;
		PreparedStatement pst = null;

		try {
			String name = "xudifsd";
			con = DriverManager.getConnection(url, user, password);

			pst = con.prepareStatement("INSERT INTO user(name) VALUES(?)");
			pst.setString(1, name);
			pst.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
}