package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp kết nối Cơ sở dữ liệu SQL Server (ConnectDB - Singleton Pattern)
 * Chuẩn môn học Lập trình Hướng sự kiện / Phát triển ứng dụng (IUH).
 */
public class ConnectDB {
    private static Connection con = null;
    private static ConnectDB instance = new ConnectDB();

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() {
        String url1 = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyCoffeePOS_HomuCafe;trustServerCertificate=true";
        String url2 = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyCoffeePOS;trustServerCertificate=true";
        String user = "sa";
        String password = "sapassword";
        try {
            con = DriverManager.getConnection(url1, user, password);
            System.out.println("Kết nối SQL Server QuanLyCoffeePOS_HomuCafe thành công!");
        } catch (SQLException e1) {
            try {
                con = DriverManager.getConnection(url2, user, password);
                System.out.println("Kết nối SQL Server QuanLyCoffeePOS thành công!");
            } catch (SQLException e2) {
                System.err.println("Chưa kết nối được SQL Server, ứng dụng sẽ chạy trên In-Memory DataStore: " + e2.getMessage());
            }
        }
    }

    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection() {
        return con;
    }
}

