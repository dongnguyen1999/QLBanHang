package main;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import tabs.NhapKho;
import tabs.QLHangHoa;
import tabs.ThanhToan;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import com.jgoodies.forms.layout.FormSpecs;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * This class display the main window
 * @author ndong
 *
 */
public class MainWindow {
	private JFrame frame;
	private QLHangHoa qlHangHoa;
	private NhapKho nhapKho;
	private ThanhToan thanhToan;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		//init main window
		frame = new JFrame();
		frame.setBounds(100, 100, 1000, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Quản Lý Bán Hàng");//set title for main window
		
		//divide tab for main
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setFont(new Font("Dialog", Font.BOLD, 15));
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);//add tabbed panel to the center of main window 
		
		qlHangHoa = new QLHangHoa();
		tabbedPane.addTab("QL Hàng hóa", null, qlHangHoa, null);
		
		//definition for the tab "Nhập kho"
		nhapKho = new NhapKho(qlHangHoa);
		tabbedPane.addTab("Nhập kho", null, nhapKho, null);
		
		//definition for the tab "Thanh toán" 
		thanhToan = new ThanhToan(qlHangHoa);
		tabbedPane.addTab("Thanh toán", null, thanhToan, null);
		
	}
	
	

}
