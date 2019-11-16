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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.ibm.icu.util.BytesTrie.Result;

import tools.MySQLConnector;


/**
 * This class design UI for tab "QL Hàng hóa" and necessary controls
 * @author ndong
 *
 */
public class QLHangHoa extends JPanel{
	
	private JTable table;
	private JTextField tfMaHang;
	private JTextField tfTenHang;
	private JTextField tfDonViTinh;
	private JTextField tfDonGiaBan;
	private DefaultTableModel tableData;
	private Connection connection;

	private static final String[] COLUMN_NAMES = {"Mã hàng","Tên hàng","Đơn vị tính","Tồn kho", "Đơn giá bán"};
	//event listener
	private ActionListener clickAddButton = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (addNewGood()) {//add new good successfully
				//refresh jtable
				refreshTable();
				//clear text-fields
				tfMaHang.setText(new String());
				tfTenHang.setText(new String());
				tfDonViTinh.setText(new String());
				tfDonGiaBan.setText(new String());
			}
		}
	};
	
	
	public QLHangHoa() {
		connection = new MySQLConnector().getConnection();
		// //definition for the tab "QL Hàng hóa" 
		setLayout(new BorderLayout(0, 0));
		JPanel northPanel = new JPanel();
		add(northPanel, BorderLayout.NORTH);//add northPanel to the north of main panel
		northPanel.setLayout(new BorderLayout(0, 0));
		JPanel addGoodsPanel = makeImportPanel();//make form for adding goods 
		northPanel.add(addGoodsPanel, BorderLayout.WEST);//add form to the west-north of main panel
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
		panel.setLayout(new GridLayout(5, 2, 30, 10));// make a grid layout 5x2
		
		JLabel lbMaHang = new JLabel("Mã hàng:");
		lbMaHang.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbMaHang);
		
		tfMaHang = new JTextField();
		panel.add(tfMaHang);
		tfMaHang.setColumns(10);
		
		JLabel lbTenHang = new JLabel("Tên hàng:");
		lbTenHang.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbTenHang);
		
		tfTenHang = new JTextField();
		panel.add(tfTenHang);
		tfTenHang.setColumns(10);
		
		JLabel lbDonViTinh = new JLabel("Đơn vị tính:");
		lbDonViTinh.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbDonViTinh);
		
		tfDonViTinh = new JTextField();
		panel.add(tfDonViTinh);
		tfDonViTinh.setColumns(10);
		
		JLabel lbDongiaBan = new JLabel("Đơn giá bán:");
		lbDongiaBan.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbDongiaBan);
		
		tfDonGiaBan = new JTextField();
		panel.add(tfDonGiaBan);
		tfDonGiaBan.setColumns(10);
		
		JButton addButton = new JButton("Thêm hàng hóa"); 
		addButton.setFont(new Font("Dialog", Font.BOLD, 15));
		addButton.addActionListener(clickAddButton);
		panel.add(addButton);
		
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
	 * Add information about new good into table HANG_HOA
	 * refresh the table-model 'data' to update new changes
	 * @param data: a TableModel that manages data in JTable 
	 */
	private boolean addNewGood() {
		String maHang = tfMaHang.getText();
		if (maHang.isEmpty()) {
			//Show message invalid input
			JOptionPane.showMessageDialog(this, "Dữ liệu đầu vào không hợp lệ!\nMã hàng không được bỏ trống", "Lỗi", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		String tenHang = tfTenHang.getText();
		System.out.print(tenHang);
		String donViTinh = tfDonViTinh.getText();
		float donGiaBan;
		try{
			donGiaBan = Float.parseFloat(tfDonGiaBan.getText());
		}catch (Exception e) {
			//Show message invalid input
			JOptionPane.showMessageDialog(this, "Dữ liệu đầu vào không hợp lệ!\nĐơn giá tính phải là kiểu số", "Lỗi", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
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
				JOptionPane.showMessageDialog(this, "Dữ liệu đầu vào không hợp lệ!\nMã hàng này đã tồn tại trên hệ thống", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public void refreshTable() {
		tableData = new DefaultTableModel(COLUMN_NAMES,0);
		initDataFromDatabase(tableData);
		table.setModel(tableData);
	}
}
