package ui;

import java.awt.EventQueue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class T2 {

	private JFrame frame;
	private JTable addedDrugTable;
	private JTextField drugInputField;
	private JButton addButton, submitButton;
	private JList<String> historyList;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					T2 window = new T2();
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
	public T2() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("藥物交互系統");
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		// 左側病患日期藥物添加檔案列表
		historyList = new JList<>();
		JScrollPane historyPane = new JScrollPane(historyList);
		historyPane.setBounds(10, 10, 250, 540);
		frame.getContentPane().add(historyPane);
		loadDrugHistory();

		// 病患資料顯示
		JTextArea patientInfo = new JTextArea("病患資訊：年齡45歲，男性，有高血壓病史，目前服用降壓藥，最近感覺頭暈和輕微噁心，無其他重大病史。");
		patientInfo.setBounds(270, 10, 500, 80);
		patientInfo.setLineWrap(true);
		patientInfo.setWrapStyleWord(true);
		patientInfo.setEditable(false);
		frame.getContentPane().add(patientInfo);

		// 藥品輸入區
		drugInputField = new JTextField();
		drugInputField.setBounds(270, 100, 400, 30);
		frame.getContentPane().add(drugInputField);

		addButton = new JButton("確認添加");
		addButton.setBounds(680, 100, 90, 30);
		frame.getContentPane().add(addButton);
		addButton.addActionListener(e -> {
		    String drugName = drugInputField.getText().trim();

		    if (drugName.isEmpty()) {
		        JOptionPane.showMessageDialog(frame, "請輸入藥品名稱");
		        return;
		    }

		    if (!isDrugExist(drugName)) {
		        JOptionPane.showMessageDialog(frame, "藥品 " + drugName + " 不存在於資料庫，請確認名稱是否正確");
		        return;
		    }
		    
		    if (isDrugAlreadyAdded(drugName)) {
		        JOptionPane.showMessageDialog(frame, "藥品 " + drugName + " 已經添加，請勿重複添加");
		        return;
		    }
		    DefaultTableModel model = (DefaultTableModel) addedDrugTable.getModel();
		    model.addRow(new Object[]{drugName});
		    drugInputField.setText("");
		});

		// 已添加藥品表格
		addedDrugTable = new JTable(new DefaultTableModel(new Object[] {"藥品名稱"}, 0));
		JScrollPane tablePane = new JScrollPane(addedDrugTable);
		tablePane.setBounds(270, 140, 500, 360);
		frame.getContentPane().add(tablePane);
		addedDrugTable.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseClicked(java.awt.event.MouseEvent evt) {
		        if (evt.getClickCount() == 2) {  // 雙擊刪除
		            int row = addedDrugTable.getSelectedRow();
		            if (row != -1) {
		                DefaultTableModel model = (DefaultTableModel) addedDrugTable.getModel();
		                model.removeRow(row);
		            }
		        }
		    }
		});

		// 送出按鈕
		submitButton = new JButton("送出");
		submitButton.setBounds(670, 510, 100, 40);
		frame.getContentPane().add(submitButton);
		submitButton.addActionListener(e -> submitDrugs());

		// 添加點擊事件以載入選定日期的藥品
		historyList.addListSelectionListener(e -> {
		    if (!e.getValueIsAdjusting()) {
		        String selectedDate = historyList.getSelectedValue();
		        if ("今天".equals(selectedDate)) {
		            ((DefaultTableModel) addedDrugTable.getModel()).setRowCount(0);
		        } else {
		            loadDrugsByDate(selectedDate);
		        }
		    }
		});
	}
	private boolean isDrugExist(String drugName) {
	    try (Connection conn = DBConnection.getConnection()) {
	        String sql = "SELECT COUNT(*) FROM drug WHERE drug_name = ?";
	        PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setString(1, drugName);
	        ResultSet rs = ps.executeQuery();

	        if (rs.next() && rs.getInt(1) > 0) {
	            return true; // 藥品存在
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        JOptionPane.showMessageDialog(frame, "檢查藥品時發生錯誤");
	    }
	    return false; // 藥品不存在
	}

	private void loadDrugHistory() {
		DefaultListModel<String> listModel = new DefaultListModel<>();
		listModel.addElement("今天"); // 新增今天選項
		try (Connection conn = DBConnection.getConnection()) {
		    String sql = "SELECT DISTINCT date FROM patient_drug_history ORDER BY date ASC";
		    PreparedStatement ps = conn.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while (rs.next()) {
		        listModel.addElement(rs.getString("date"));
		    }
		    historyList.setModel(listModel);
		} catch (Exception e) {
		    e.printStackTrace();
		    JOptionPane.showMessageDialog(frame, "讀取歷史紀錄失敗！");
		}
	}

	private boolean isDrugAlreadyAdded(String drugName) {
	    DefaultTableModel model = (DefaultTableModel) addedDrugTable.getModel();
	    for (int i = 0; i < model.getRowCount(); i++) {
	        if (model.getValueAt(i, 0).toString().equalsIgnoreCase(drugName)) {
	            return true; // 藥品已存在
	        }
	    }
	    return false;
	}
	
	private void loadDrugsByDate(String date) {
		DefaultTableModel model = (DefaultTableModel) addedDrugTable.getModel();
		model.setRowCount(0);
		try (Connection conn = DBConnection.getConnection()) {
		    String sql = "SELECT drug_name FROM patient_drug_history WHERE date = ?";
		    PreparedStatement ps = conn.prepareStatement(sql);
		    ps.setString(1, date);
		    ResultSet rs = ps.executeQuery();
		    while (rs.next()) {
		        model.addRow(new Object[]{rs.getString("drug_name")});
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		    JOptionPane.showMessageDialog(frame, "讀取藥品失敗！");
		}
	}
	
	private void submitDrugs() {
		DefaultTableModel model = (DefaultTableModel) addedDrugTable.getModel();
		int rowCount = model.getRowCount();

		if (rowCount == 0) {
		    JOptionPane.showMessageDialog(frame, "尚未添加任何藥品！");
		    return;
		}

		try (Connection conn = DBConnection.getConnection()) {
		    for (int i = 0; i < rowCount; i++) {
		        for (int j = i + 1; j < rowCount; j++) {
		            String drugA = model.getValueAt(i, 0).toString();
		            String drugB = model.getValueAt(j, 0).toString();
		            String sql = "SELECT remark FROM drug_interaction WHERE (drug_name_1=? AND drug_name_2=?) OR (drug_name_1=? AND drug_name_2=?)";
		            PreparedStatement ps = conn.prepareStatement(sql);
		            ps.setString(1, drugA);
		            ps.setString(2, drugB);
		            ps.setString(3, drugB);
		            ps.setString(4, drugA);
		            ResultSet rs = ps.executeQuery();
		            if (rs.next()) {
		                JOptionPane.showMessageDialog(frame, "藥物交互作用警告：" + drugA + " 與 " + drugB + " 可能產生衝突！\n\n原因：" + rs.getString("remark"), "交互作用警告", JOptionPane.WARNING_MESSAGE);
		                return;
		            }
		        }
		    }
		    JOptionPane.showMessageDialog(frame, "藥品成功送出！無交互作用");
		    
		    // 將藥品存入 patient_drug_history
		    String date = LocalDate.now().toString();
		    for (int i = 0; i < rowCount; i++) {
		        String drugName = model.getValueAt(i, 0).toString();
		        String insertSql = "INSERT INTO patient_drug_history (drug_name, date) VALUES (?, ?)";
		        PreparedStatement insertPs = conn.prepareStatement(insertSql);
		        insertPs.setString(1, drugName);
		        insertPs.setString(2, date);
		        insertPs.executeUpdate();
		    }
		    model.setRowCount(0);
		    loadDrugHistory();
		} catch (Exception ex) {
		    ex.printStackTrace();
		    JOptionPane.showMessageDialog(frame, "送出失敗，請檢查資料庫連線！");
		}
	}
}
