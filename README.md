# 藥物交互系統 (Drug Interaction System)

## 📖 介紹

本系統是一個基於 Java Swing 的桌面應用程式，主要用於管理病患的藥品歷史記錄，並檢查新添加藥品之間的交互作用。

<img width="800" alt="image" src="https://github.com/user-attachments/assets/cb01a3e5-5bcb-4f2c-bda8-b0a26e158590" />


## ✨ 功能特點

- ✅ **病患資訊顯示**：顯示病患基本資訊，例如年齡、病史等。
- ✅ **藥品輸入與添加**：用戶可以輸入藥品名稱，並添加到當前藥物清單中。
- ✅ **藥品歷史查詢**：可查看過去記錄的用藥情況。
- ✅ **交互作用檢查**：當送出藥品時，系統會檢查是否有交互作用，並給出警告。
- ✅ **雙擊刪除藥品**：在表格中雙擊某個藥品可將其移除。
- ✅ **數據庫存儲**：所有藥品記錄會存入資料庫中，以便未來查詢。

## 🛠 環境需求

- 🔹 **JDK 8 或以上**
- 🔹 **MySQL 數據庫**
- 🔹 **JDBC 驅動** (確保 `mysql-connector-java` 已導入到專案中)

## 📌 主要類別

### `T2.java`

- `initialize()`：初始化 UI 組件。
- `isDrugExist(String drugName)`：檢查藥品是否存在於資料庫。
- `loadDrugHistory()`：讀取病患的歷史用藥記錄。
- `isDrugAlreadyAdded(String drugName)`：檢查藥品是否已經被添加到當前清單。
- `loadDrugsByDate(String date)`：根據日期載入病患過去的用藥記錄。
- `submitDrugs()`：送出藥品並檢查交互作用。

## 🗄 數據庫設計

```sql
CREATE TABLE drug (
    id INT PRIMARY KEY AUTO_INCREMENT,
    drug_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE drug_interaction (
    id INT PRIMARY KEY AUTO_INCREMENT,
    drug_name_1 VARCHAR(255) NOT NULL,
    drug_name_2 VARCHAR(255) NOT NULL,
    remark TEXT,
    FOREIGN KEY (drug_name_1) REFERENCES drug(drug_name),
    FOREIGN KEY (drug_name_2) REFERENCES drug(drug_name)
);

CREATE TABLE patient_drug_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    drug_name VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    FOREIGN KEY (drug_name) REFERENCES drug(drug_name)
);
```

## 🔍 檢查藥品交互作用邏輯

```java
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
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(frame, "送出失敗，請檢查資料庫連線！");
    }
}
```

## 🚀 執行方式

1. **確保數據庫已設置並運行**。
2. **修改 `DBConnection.java`** 以匹配你的數據庫設定。
3. **使用 IDE（如 IntelliJ IDEA、Eclipse）或命令行執行 `T2.java`**。
4. **開始添加藥品並檢查交互作用！**

## ⚠ 注意事項
- ⚠ **藥品名稱需與資料庫匹配，否則無法添加**。
- ⚠ **交互作用數據需事先錄入 `drug_interaction` 表中**。
- ⚠ **病患歷史記錄會根據日期分類顯示**。
