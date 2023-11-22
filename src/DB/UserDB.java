package DB;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * 
 * @author GUOFENG  --��¼�������ݿ�
 * 
 */
public class UserDB {
	// ���ݿ�����
	private static String driver;
	private static String url;
	private static String sqluser;
	private static String sqlpassword;

	static {
		// �������ݿ�����
		try (InputStream input = UserDB.class.getClassLoader().getResourceAsStream("dbconfig.properties")) {
			Properties prop = new Properties();
			if (input == null) {
				System.out.println("Sorry, unable to find dbconfig.properties");
				throw new IOException("dbconfig.properties not found");
			}
			// ���������ļ�
			prop.load(input);

			// �������ļ���ȡ���ݿ����
			driver = prop.getProperty("driver");
			url = prop.getProperty("url");
			sqluser = prop.getProperty("user");
			sqlpassword = prop.getProperty("password");
//			System.out.println(driver);
//			System.out.println(url);
//			System.out.println(sqluser);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}


	String userpwd_;
	String username_;
	boolean n = false;

	public UserDB(String name, String pwd) {
		username_ = name;
		userpwd_ = pwd;
	}

	public Boolean selectsql() {
		n = false;
		try {
			// ��������
			Class.forName(driver);
			// �������ݿ�
			Connection conn = DriverManager.getConnection(url, sqluser,
					sqlpassword);
			if (!conn.isClosed())
				System.out.println("�������ݿ�ɹ�!");
			// statement����ִ��SQL���
			Statement statement = conn.createStatement();
			// Ҫִ�е�SQL���
			String sql = "select userpwd from info where username=" + "'" + username_ + "';";
			// �����
			ResultSet rs = statement.executeQuery(sql);
			String readpwd = null;
			while (rs.next()) {
				// ѡ��passworld��������
				readpwd = rs.getString("userpwd");
				// ����ʹ��ISO-8859-1�ַ����������Ϊ�ֽ����в�������洢�µ��ֽ������С�
				// Ȼ��ʹ��GB2312�ַ�������ָ�����ֽ�����
				readpwd = new String(readpwd.getBytes("ISO-8859-1"), "GB2312");
				// ������
				System.out.println(readpwd);
				if (readpwd.equals(userpwd_)) {
					n = true;
				}
			}
			rs.close();
			conn.close();
		} catch (ClassNotFoundException e) {
			System.out.println("����MySQL����ʧ��!");
		} catch (SQLException e1) {
			System.out.println("1.hellosql:" + e1.getMessage());
		} catch (Exception e2) {
			System.out.println("2.hellosql:" + e2.getMessage());
		}
		return n;
	}

	public boolean addsql() {
		int count = 0;
		n = false;
		try {
			// ��������
			Class.forName(driver);
			// �������ݿ�
			Connection conn = DriverManager.getConnection(url, sqluser,
					sqlpassword);
			if (!conn.isClosed())
				System.out.println("�������ݿ�ɹ�!");

			String sql = "insert into info (username, userpwd) values (?,?);";

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, username_);
			ps.setString(2, userpwd_);
			count = ps.executeUpdate();
			if (this.selectsql() == true)
				{	n = true;
					System.out.println("***");
				}
			else {
				JOptionPane.showMessageDialog(new JFrame(), "ע��ʧ�ܣ�", "����",
						JOptionPane.ERROR_MESSAGE);
			}
			ps.close();
			conn.close();
		} catch (ClassNotFoundException e) {
			System.out.println("����MySQL����ʧ��!");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQLException!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception!");
		}
		return n;
	}

}