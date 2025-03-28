package ui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnection {
    // MySQL 連線資訊
    private static final String URL = "jdbc:mysql://localhost:3306/T1?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    // 建立資料庫連線的方法
    public static Connection getConnection() {
        try {
            // 載入 MySQL 驅動
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 建立連線
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("無法找到 JDBC 驅動程式");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("資料庫連線失敗");
            e.printStackTrace();
        }
        return null;
    }

    // 測試查詢
    public static void queryData() {
        String sql = "SELECT * FROM drug"; // 指定 T1 資料庫中的 drug 表
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("drug_id") + ", Name: " + rs.getString("drug_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 主函式執行
    public static void main(String[] args) {
        queryData();
    }
}
