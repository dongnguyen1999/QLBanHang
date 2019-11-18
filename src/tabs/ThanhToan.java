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

import tools.DatabaseUtils;
import tools.MySQLConnector;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * This class design UI for tab "Thanh toán" and necessary controls
 * @author ndong
 *
 */
public class ThanhToan extends JPanel{
	private QLHangHoa qlHangHoa;
	private JTable table;
	private JTextField tfMaHang;
	private JLabel lbValTenHang;
	private JTextField tfSoLuong;
	private JButton addButton;
	private JButton sellButton; 
	private DefaultTableModel tableData;
	private DatabaseUtils dbUtils;
	private boolean importable;
	private int sttCounter;
	private JLabel lbValTongGiaTri;

	private static final String[] COLUMN_NAMES = {"STT","Mã hàng","Tên hàng", "Đơn giá bán", "Số lượng"};
	private static final String NOT_FOUND_ID = "Mã hàng không khả dụng";
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
			} else {
				lbValTenHang.setText(NOT_FOUND_ID);
			}
			addButton.setEnabled(importable);
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
	private ActionListener clickAddButton = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (addNewItem()) {//add new good successfully
				//refresh qlHangHoa
				qlHangHoa.refreshTable();
				//refresh jtable
//				tableData = new DefaultTableModel(COLUMN_NAMES,0);
				table.setModel(tableData);
				//clear text-fields
				tfMaHang.setText(new String());
				lbValTenHang.setText(NOT_FOUND_ID);
				tfSoLuong.setText("1");
				float sum = 0;
				for (int i = 0 ; i < tableData.getRowCount(); i++) {
					sum += Float.parseFloat(tableData.getValueAt(i, 3).toString()) * Integer.parseInt(tableData.getValueAt(i, 4).toString());
				}
				lbValTongGiaTri.setText(sum+"");
			}
		}
	};
	private ActionListener clickSellButton = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (addNewBill(tableData)) {//add new good successfully
				//refresh qlHangHoa
				qlHangHoa.refreshTable();
				//refresh jtable
				tableData = new DefaultTableModel(COLUMN_NAMES,0);
				table.setModel(tableData);
				lbValTongGiaTri.setText("0");
			}
		}
	};
	
	public ThanhToan(QLHangHoa hanghoa) {
		qlHangHoa = hanghoa;
		connection = new MySQLConnector().getConnection();
		importable = false;
		sttCounter = 0;
		// //definition for the tab "QL Hàng hóa" 
		setLayout(new BorderLayout(0, 0));
		JPanel northPanel = new JPanel();
		add(northPanel, BorderLayout.NORTH);//add northPanel to the north of main panel
		northPanel.setLayout(new BorderLayout(0, 0));
		JPanel addGoodsPanel = makeAddGoodsPanel();//make form for adding goods 
		northPanel.add(addGoodsPanel, BorderLayout.WEST);//add form to the west-north of main panel
		JPanel eastPanel = new JPanel();//create a east panel
		eastPanel.setLayout(new BorderLayout());
		//make an addButton
		addButton = new JButton("Thêm sản phẩm"); 
		addButton.setFont(new Font("Dialog", Font.BOLD, 15));
		addButton.addActionListener(clickAddButton);
		addButton.setEnabled(importable);
		JPanel border = new JPanel();//add padding for addButton
		border.setBorder(new EmptyBorder(20, 30, 20, 30));
		border.add(addButton);
		eastPanel.add(border, BorderLayout.CENTER);//set button into the center of eastPanel
		northPanel.add(eastPanel, BorderLayout.EAST);//set east panel to the north
		//make the table
		tableData = new DefaultTableModel(COLUMN_NAMES, 0);//make new data for table with the top bar for columnNames
		table = new JTable(tableData);
		table.setRowHeight(25);
		table.getTableHeader().setFont(new Font("Dialog", Font.CENTER_BASELINE, 20));
		table.setFont(new Font("Dialog", Font.LAYOUT_LEFT_TO_RIGHT, 20));
		add(new JScrollPane(table), BorderLayout.CENTER);
		//add south panel
		JPanel southPanel = new JPanel();
		southPanel.setBorder(new EmptyBorder(20, 30, 20, 0));
		add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new BorderLayout());
		JPanel westPanel = new JPanel();
		westPanel.setLayout(new GridLayout(2,2,10,10));
		JLabel lbTongGiaTri = new JLabel("Tổng giá trị:");
		lbTongGiaTri.setFont(new Font("Dialog", Font.BOLD, 15));
		lbValTongGiaTri = new JLabel("0");
		lbValTongGiaTri.setFont(new Font("Dialog", Font.BOLD, 15));
		westPanel.add(lbTongGiaTri);
		westPanel.add(lbValTongGiaTri);
		southPanel.add(westPanel, BorderLayout.WEST);
		//make an sellButton
		sellButton = new JButton("Thanh toán"); 
		sellButton.setFont(new Font("Dialog", Font.BOLD, 15));
		sellButton.addActionListener(clickSellButton);
		westPanel.add(sellButton, BorderLayout.CENTER);//set button into west panel
	}
	
	/**
	 * Make a panel object contain form to add a new good to database
	 * @return a panel
	 */
	private JPanel makeAddGoodsPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(20, 30, 20, 0));
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lbMaHang = new JLabel("Mã hàng:");
		lbMaHang.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbMaHang);
		
		tfMaHang = new JTextField();
		panel.add(tfMaHang);
		tfMaHang.setColumns(10);
		tfMaHang.getDocument().addDocumentListener(enteringGoodId);
		
//		JLabel lbSoLuongNhap = new JLabel("Số lượng:");
//		lbSoLuongNhap.setFont(new Font("Dialog", Font.BOLD, 15));
//		panel.add(lbSoLuongNhap);
		
		tfSoLuong = new JTextField();
		panel.add(tfSoLuong);
		tfSoLuong.setColumns(10);
		tfSoLuong.setText("1");
		
		JLabel lbTenHang = new JLabel("Tên hàng:");
		lbTenHang.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbTenHang);
		
		lbValTenHang = new JLabel(NOT_FOUND_ID);
		lbValTenHang.setFont(new Font("Dialog", Font.BOLD, 15));
		panel.add(lbValTenHang);
		
//		JLabel lbTonKho = new JLabel("Tồn kho:");
//		lbTonKho.setFont(new Font("Dialog", Font.BOLD, 15));
//		panel.add(lbTonKho);
//		
//		lbValTonKho = new JLabel(NOT_FOUND_ID);
//		lbValTonKho.setFont(new Font("Dialog", Font.BOLD, 15));
//		panel.add(lbValTonKho);
		
		
		return panel;
	}
	
	/**
	 * add data about Goods from database to table-model 'data' 
	 * @param data: a TableModel that manages data in JTable 
	 * @param id: a value from field HH_MA of HANG_HOA table
	 * @param num: a number show how many items with this id will be taken
	 */
	private void addItemToTable(String id,int num, DefaultTableModel data) {
		//check whether this good have enough in storage or not
		if (computeNumberOfGoods(id) < num) {
			//Show message invalid input
			JOptionPane.showMessageDialog(this, "Loại hàng hóa này đã hết hàng!", "Có gì đó không đúng", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		for (int i = 0; i < data.getRowCount(); i++) {//loop through values in column MA_HANG
//			System.out.println(data.getValueAt(i, 1));
			if (data.getValueAt(i, 1).equals(id)) {//if value at column MA_HANG == id (input MA_HANG)
				data.setValueAt(Integer.parseInt(data.getValueAt(i, 4).toString()) + num, i, 4);//increase value at column SO_LUONG by num
				return;
			}
		}
		//get data from database
		if (connection != null) { //create connection successfully
			try {
				Statement statement = connection.createStatement();
				String sql = "select HH_TEN, HH_DONGIABAN from HANG_HOA where HH_MA='" + id + "';";
				ResultSet result = statement.executeQuery(sql);
				//result set pointer start at null
				result.next();//move the pointer to next row
				//read data from a row
				String stt = ++sttCounter + "";
				String maHang = id;
				String tenHang = result.getString(1);
				String donGiaBan = result.getString(2);//get PNK_DONGIANHAP by read column 4
				String soLuong = num + "";
//					System.out.println(maHang + " | " + tenHang + " | " + donViTinh + " | " + donGiaBan);
				//add data from row to table-model data
				data.addRow(new String[] {stt, maHang, tenHang, donGiaBan, soLuong});
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
	private boolean addNewItem() {
		String maHang = tfMaHang.getText();
		if (maHang.isEmpty()) {
			//Show message invalid input
			JOptionPane.showMessageDialog(this, "Dữ liệu đầu vào không hợp lệ!\nMã hàng không được bỏ trống", "Lỗi", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		int soLuong;
		try{
			soLuong = Integer.parseInt(tfSoLuong.getText());
			if (soLuong < 1) {
				//Show message invalid input
 				JOptionPane.showMessageDialog(this, "Dữ liệu đầu vào không hợp lệ!\nSố lượng nhập phải lớn hơn 0", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}catch (Exception e) {
			//Show message invalid input
			JOptionPane.showMessageDialog(this, "Dữ liệu đầu vào không hợp lệ!\nSố lượng nhập nhập phải là kiểu số", "Lỗi", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		addItemToTable(maHang, soLuong, tableData);
		return true;
	}
	
	private boolean addNewBill(DefaultTableModel data) {
		if (Float.parseFloat(lbValTongGiaTri.getText()) == 0) {
			//Show message invalid input
			JOptionPane.showMessageDialog(this, "Hãy nhập vào một sản phẩm nào đó để thanh toán", "Có gì đó không đúng", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		//compute sum of cost in bill, get data for CHI_TIET_HOA_DON
		String[][] cthd = new String[data.getRowCount()][3];
		float sum = 0;
		for (int i = 0 ; i < data.getRowCount(); i++) {
			sum += Float.parseFloat(data.getValueAt(i, 3).toString()) * Integer.parseInt(data.getValueAt(i, 4).toString());
			cthd[i][0] = data.getValueAt(i, 1).toString();//get MAHANG
			cthd[i][1] = data.getValueAt(i, 4).toString();//get SOLUONG
			cthd[i][2] = data.getValueAt(i, 3).toString();//get DONGIBAN
		}
		
		if (connection != null) {//create connection successfully
			try {
				//insert new HOA_DON
				String sql = "insert into HOA_DON(HD_NGAYLAPHD, HD_TONGGIATRI) values (NOW(),?);";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setFloat(1, sum);
				statement.execute();
				//insert detail information for new HOA_DON
				//get stt of the newest bill
				ResultSet result = connection.createStatement().executeQuery("select HD_STT from HOA_DON order by HD_NGAYLAPHD desc");
				result.next();//to get the first result
				int stt = result.getInt(1);//get HD_STT from the first result
				System.out.println(stt);
				//prepare statement
				sql = "insert into CHI_TIET_HOA_DON(HH_MA,HD_STT,CTHD_SOLUONG,CTHD_GIABAN) values (?,?,?,?);";
				statement = connection.prepareStatement(sql);
				for (int i = 0; i < cthd.length; i++) {
					statement.setString(1, cthd[i][0]);
					statement.setInt(2, stt);
					statement.setInt(3, Integer.parseInt(cthd[i][1]));
					statement.setFloat(4, Float.parseFloat(cthd[i][2]));
					statement.execute();
				}
				return true;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
