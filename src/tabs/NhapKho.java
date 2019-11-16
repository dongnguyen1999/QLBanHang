package tabs;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.ibm.icu.util.BytesTrie.Result;

import tools.MySQLConnector;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * This class design UI for tab "QL Hàng hóa" and necessary controls
 * @author ndong
 *
 */
public class NhapKho extends JPanel{
	private QLHangHoa qlHangHoa;
	private JTable table;
	private JTextField tfMaHang;
	private JLabel lbValTenHang;
	private JLabel lbValTonKho;
	private JTextField tfDonGiaNhap;
	private JTextField tfSoLuongNhap;
	private JButton importButton; 
	private DefaultTableModel tableData;
	private Connection connection;
	private boolean importable;

	private static final String[] COLUMN_NAMES = {"STT","Mã hàng","Thời gian nhập", "Đơn giá nhập", "Số lượng nhập"};
	private static final String NOT_FOUND_ID = "Mã hàng không tồn tại";
	//event listener
	private DocumentListener enteringGoodId = new DocumentListener() {
		/**
		 * find good in database
		 * update prop importable and value for lbValTenHang, lbValTonKho if found id from database
		 * set enable for import button
		 * @param id: value of field HH_MA in table HANG_HOA
		 */
		private void checkImportable(String id) {
			importable = findGoodIdInDatabase(id);
			if (importable) {
				lbValTenHang.setText(computeNameOfGoods(id));
				lbValTonKho.setText(computeNumberOfGoods(id)+"");
			} else {
				lbValTenHang.setText(NOT_FOUND_ID);
				lbValTonKho.setText(NOT_FOUND_ID);
			}
			importButton.setEnabled(importable);
		}
		
		@Override
		public void removeUpdate(DocumentEvent arg0) {
//			System.out.println("removed");
			checkImportable(tfMaHang.getText());
		}
		
		@Override
		public void insertUpdate(DocumentEvent arg0) {
//			System.out.println("inserted");
			checkImportable(tfMaHang.getText());
		}
		
		@Override
		public void changedUpdate(DocumentEvent arg0) {
//			System.out.println("changed");
			checkImportable(tfMaHang.getText());
		}
	};
	private ActionListener clickImportButton = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (addNewImport()) {//add new good successfully
				//refresh qlHangHoa
				qlHangHoa.refreshTable();
				//refresh jtable
				tableData = new DefaultTableModel(COLUMN_NAMES,0);
				initDataFromDatabase(tableData);
				table.setModel(tableData);
				//clear text-fields
				tfMaHang.setText(new String());
				lbValTenHang.setText(NOT_FOUND_ID);
				lbValTonKho.setText(NOT_FOUND_ID);
				tfDonGiaNhap.setText(new String());
				tfSoLuongNhap.setText(new String());
			}
		}
	};
	
	
	public NhapKho(QLHangHoa hanghoa) {
		qlHangHoa = hanghoa;
		connection = new MySQLConnector().getConnection();
		importable = false;
		// //definition for the tab "QL Hàng hóa" 
		setLayout(new BorderLayout(0, 0));
		JPanel northPanel = new JPanel();
		add(northPanel, BorderLayout.NORTH);//add northPanel to the north of main panel
		northPanel.setLayout(new BorderLayout(0, 0));
		JPanel addGoodsPanel = makeImportPanel();//make form for adding goods 
		northPanel.add(addGoodsPanel, BorderLayout.WEST);//add form to the west-north of main panel
		JScrollPane scrollPane = new JScrollPane();
		tableData = new DefaultTableModel(COLUMN_NAMES, 0);//make new data for table with the top bar for columnNames
		initDataFromDatabase(tableData);
		table = new JTable(tableData);
		table.setRowHeight(25);
		table.getTableHeader().setFont(new Font("Dialog", Font.CENTER_BASELINE, 20));
		table.setFont(new Font("Dialog", Font.LAYOUT_LEFT_TO_RIGHT, 20));
		add(new JScrollPane(table), BorderLayout.CENTER);
	}
	
	/**
	 * Make a panel object contain form to add a new good to database
	 * @return a panel
	 */
	private JPanel makeImportPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(20, 30, 20, 0));// add left padding
		panel.setLayout(new GridLayout(6, 2, 30, 10));// make a grid layout 5x2
		
		JLabel lbMaHang = new JLabel("Mã hàng:");
		lbMaHang.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbMaHang);
		
		tfMaHang = new JTextField();
		panel.add(tfMaHang);
		tfMaHang.setColumns(10);
		tfMaHang.getDocument().addDocumentListener(enteringGoodId);
		
		JLabel lbTenHang = new JLabel("Tên hàng:");
		lbTenHang.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbTenHang);
		
		lbValTenHang = new JLabel(NOT_FOUND_ID);
		lbValTenHang.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbValTenHang);
		
		JLabel lbTonKho = new JLabel("Tồn kho:");
		lbTonKho.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbTonKho);
		
		lbValTonKho = new JLabel(NOT_FOUND_ID);
		lbValTonKho.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbValTonKho);
		
		JLabel lbDonGiaNhap = new JLabel("Đơn giá nhập:");
		lbDonGiaNhap.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbDonGiaNhap);
		
		tfDonGiaNhap = new JTextField();
		panel.add(tfDonGiaNhap);
		tfDonGiaNhap.setColumns(10);
		
		JLabel lbSoLuongNhap = new JLabel("Số lượng nhập:");
		lbSoLuongNhap.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbSoLuongNhap);
		
		tfSoLuongNhap = new JTextField();
		panel.add(tfSoLuongNhap);
		tfSoLuongNhap.setColumns(10);
		
		importButton = new JButton("Lập phiếu nhập kho"); 
		importButton.setFont(new Font("Dialog", Font.BOLD, 15));
		importButton.addActionListener(clickImportButton);
		importButton.setEnabled(importable);
		panel.add(importButton);
		
		return panel;
	}
	
	/**
	 * clear current rows in table-model 'data' 
	 * add data about Goods from database to table-model 'data' 
	 * @param data: a TableModel that manages data in JTable 
	 */
	private void initDataFromDatabase(DefaultTableModel data) {
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
	private String computeNameOfGoods(String id) {
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
	
	/**
	 * compute number of goods that are being stored in storage
	 * @param id: value of field HH_MAHANG in table HANG_HOA
	 * @return number of the good with the specific id  
	 */
	private int computeNumberOfGoods(String id) {
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
	
	/**
	 * check if the good id is in database
	 * @param id: value of field HH_MA in table HANG_HOA
	 * @return boolean  
	 */
	private boolean findGoodIdInDatabase(String id) {
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
	
	/**
	 * Add information about importing report into table PHIEU_NHAP_KHO
	 * refresh the table-model 'data' to update new changes
	 * @param data: a TableModel that manages data in JTable 
	 */
	private boolean addNewImport() {
		String maHang = tfMaHang.getText();
		if (maHang.isEmpty()) {
			//Show message invalid input
			JOptionPane.showMessageDialog(this, "Dữ liệu đầu vào không hợp lệ!\nMã hàng không được bỏ trống", "Lỗi", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		float donGiaNhap;
		try{
			donGiaNhap = Float.parseFloat(tfDonGiaNhap.getText());
		}catch (Exception e) {
			//Show message invalid input
			JOptionPane.showMessageDialog(this, "Dữ liệu đầu vào không hợp lệ!\nĐơn giá nhập phải là kiểu số", "Lỗi", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		int soLuongNhap;
		try{
			soLuongNhap = Integer.parseInt(tfSoLuongNhap.getText());
			if (soLuongNhap < 1) {
				//Show message invalid input
 				JOptionPane.showMessageDialog(this, "Dữ liệu đầu vào không hợp lệ!\nSố lượng nhập phải lớn hơn 0", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}catch (Exception e) {
			//Show message invalid input
			JOptionPane.showMessageDialog(this, "Dữ liệu đầu vào không hợp lệ!\nSố lượng nhập nhập phải là kiểu số", "Lỗi", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
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
				JOptionPane.showMessageDialog(this, "Dữ liệu đầu vào không hợp lệ!\nMã hàng này đã tồn tại trên hệ thống", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
