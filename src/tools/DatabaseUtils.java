package tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

public class DatabaseUtils {
	private Connection connection;
	private JPanel panel; 
	
	private static final String NOT_FOUND_ID = "Mã hàng không tồn tại";
	
	public DatabaseUtils(JPanel panel) {
		this.panel = panel;
		connection = (new MySQLConnector()).getConnection();
	}
	
	/**
	 * clear current rows in table-model 'data' 
	 * add data about Goods from database to table-model 'data' 
	 * @param data: a TableModel that manages data in JTable 
	 */
	public void initGoodsInfoFromDatabase(DefaultTableModel data) {
		//remove all current rows
		for (int i = 0; i < data.getRowCount(); i++) data.removeRow(i);
		//get data from database
		if (connection != null) { //create connection successfully
			try {
				Statement statement = connection.createStatement();
				String sql = "select * from HANG_HOA;";
				ResultSet result = statement.executeQuery(sql);
				//result set pointer start at null
				while(result.next()) {//move the pointer to next row
					//read data from a row
					String maHang = result.getString(1);//get HH_MAHANG by read column 1
					String tenHang = result.getString(2);//get HH_TENHANG by read column 2
					String donViTinh = result.getString(3);//get HH_DONVITINH by read column 3
					String donGiaBan = result.getString(4);//get HH_DONGIABAN by read column 4
					String numberOfGood = computeNumberOfGoods(maHang) + "";//compute amount and parse String
//					System.out.println(maHang + " | " + tenHang + " | " + donViTinh + " | " + donGiaBan);
					//add data from row to table-model data
					data.addRow(new String[] {maHang, tenHang, donViTinh,numberOfGood,donGiaBan});
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * compute number of goods that are being stored in storage
	 * @param id: value of field HH_MAHANG in table HANG_HOA
	 * @return number of the good with the specific id  
	 */
	public int computeNumberOfGoods(String id) {
		int numberOfGood = 0;
		if (connection != null) {//create connection successfully
			try {
				//get sum of number of imported good
				Statement statement = connection.createStatement();
				String sql = "select sum(PNK_SOLUONGNHAP) from PHIEU_NHAP_KHO"
						+ " where HH_MA='" + id + "';";
				ResultSet result = statement.executeQuery(sql);//result set pointer start at null
				result.next();// set pointer to the only first row
				int numberOfIm = result.getInt(1);
				//get sum of number of exported good
				statement = connection.createStatement();
				sql = "select sum(CTHD_SOLUONG) from CHI_TIET_HOA_DON"
						+ " where HH_MA='" + id + "';";
				result = statement.executeQuery(sql);//result set pointer start at null
				result.next();// set pointer to the only first row
				int numberOfEx = result.getInt(1);
				return numberOfIm-numberOfEx;
			}catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		}
		return numberOfGood;
	}
	
	public boolean insertGoodIntoDatabase(String maHang,String tenHang,String donViTinh, float donGiaBan) {
		if (connection != null) {//create connection successfully
			try {
				String sql = new String ("insert into HANG_HOA values (?,?,?,?);".getBytes(),"UTF-8");
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setString(1, maHang);
				statement.setString(2, tenHang);
				statement.setString(3, donViTinh);
				statement.setFloat(4, donGiaBan);
				statement.execute();
				return true;
			}
			catch (SQLIntegrityConstraintViolationException e) {
				//show message: this good is already exist
				JOptionPane.showMessageDialog(panel, "Dữ liệu đầu vào không hợp lệ!\nMã hàng này đã tồn tại trên hệ thống", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * clear current rows in table-model 'data' 
	 * add data about Imported reports from database to table-model 'data' 
	 * @param data: a TableModel that manages data in JTable 
	 */
	public void initImportsInfoFromDatabase(DefaultTableModel data) {
		//remove all current rows
		for (int i = 0; i < data.getRowCount(); i++) data.removeRow(i);
		//get data from database
		if (connection != null) { //create connection successfully
			try {
				Statement statement = connection.createStatement();
				String sql = "select * from PHIEU_NHAP_KHO;";
				ResultSet result = statement.executeQuery(sql);
				//result set pointer start at null
				while(result.next()) {//move the pointer to next row
					//read data from a row
					String stt = result.getString(1);//get PNK_STT by read column 1
					String maHang = result.getString(2);//get HH_MAHANG by read column 2
					String ngayNhap = result.getString(3);//get PNK_NGAYNHAP by read column 3
					String donGiaNhap = result.getString(4);//get PNK_DONGIANHAP by read column 4
					String soLuongNhap = result.getInt(5) + "";//get PNK_DONGIANHAP by read column 5
//					System.out.println(maHang + " | " + tenHang + " | " + donViTinh + " | " + donGiaBan);
					//add data from row to table-model data
					data.addRow(new String[] {stt, maHang, ngayNhap, donGiaNhap, soLuongNhap});
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * compute name of goods that has specific id
	 * @param id: value of field HH_MA in table HANG_HOA
	 * @return String: name if the good  
	 */
	public String computeNameOfGoods(String id) {
		if (connection != null) {//create connection successfully
			try {
				//get sum of number of imported good
				Statement statement = connection.createStatement();
				String sql = "select HH_TEN from HANG_HOA"
						+ " where HH_MA='" + id + "';";
				ResultSet result = statement.executeQuery(sql);//result set pointer start at null
				result.next();// set pointer to the only first row
				String name = result.getString(1);
				return name;
			}catch (Exception e) {
				e.printStackTrace();
				return NOT_FOUND_ID;
			}
		}
		return NOT_FOUND_ID;
	}
	
	public boolean insertImportIntoDatabase(String maHang, float donGiaNhap, int soLuongNhap) {
		if (connection != null) {//create connection successfully
			try {
				String sql = "insert into PHIEU_NHAP_KHO(HH_MA, PNK_NGAYNHAP, PNK_DONGIANHAP, PNK_SOLUONGNHAP) values (?,NOW(),?,?);";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setString(1, maHang);
				statement.setFloat(2, donGiaNhap);
				statement.setInt(3, soLuongNhap);
				statement.execute();
				return true;
			}
			catch (SQLIntegrityConstraintViolationException e) {
				//show message: this good is already exist
				JOptionPane.showMessageDialog(panel, "Dữ liệu đầu vào không hợp lệ!\nMã hàng này đã tồn tại trên hệ thống", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * check if the good id is in database
	 * @param id: value of field HH_MA in table HANG_HOA
	 * @return boolean  
	 */
	public boolean findGoodIdInDatabase(String id) {
		if (connection != null) {//create connection successfully
			try {
				Statement statement = connection.createStatement();
				String sql = "select count(*) from HANG_HOA where HH_MA='" + id + "';";
				ResultSet result = statement.executeQuery(sql);//result set pointer start at null
				result.next();// set pointer to the only first row
				int num = result.getInt(1);
				if (num == 1) return true;
			}catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
}
