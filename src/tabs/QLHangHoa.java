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

import tools.DatabaseUtils;
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
	private DatabaseUtils dbUtils;

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
		dbUtils = new DatabaseUtils(this);
		// //definition for the tab "QL Hàng hóa" 
		setLayout(new BorderLayout(0, 0));
		JPanel northPanel = new JPanel();
		add(northPanel, BorderLayout.NORTH);//add northPanel to the north of main panel
		northPanel.setLayout(new BorderLayout(0, 0));
		JPanel addGoodsPanel = makeImportPanel();//make form for adding goods 
		northPanel.add(addGoodsPanel, BorderLayout.WEST);//add form to the west-north of main panel
		tableData = new DefaultTableModel(COLUMN_NAMES, 0);//make new data for table with the top bar for columnNames
		dbUtils.initGoodsInfoFromDatabase(tableData);
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
		return dbUtils.insertGoodIntoDatabase(maHang, tenHang, donViTinh, donGiaBan);
		
	}
	
	public void refreshTable() {
		tableData = new DefaultTableModel(COLUMN_NAMES,0);
		dbUtils.initGoodsInfoFromDatabase(tableData);
		table.setModel(tableData);
	}
}
