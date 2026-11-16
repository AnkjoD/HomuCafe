package com.homucafe.connectdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Lớp kết nối Cơ sở dữ liệu SQL Server (ConnectDB - Singleton Pattern)
 * Chuẩn môn học Lập trình Hướng sự kiện / Phát triển ứng dụng (IUH).
 * Hỗ trợ đọc cấu hình ngoài từ file config.properties nếu có tại thư mục chạy.
 */
public class ConnectDB {
    private static Connection con = null;
    private static ConnectDB instance = new ConnectDB();

    public static ConnectDB getInstance() {
        return instance;
    }

    public static Properties loadExternalConfig() {
        Properties props = new Properties();
        File cfg = new File("config.properties");
        if (cfg.isFile()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(cfg), StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (Exception e) {
                System.err.println("Không đọc được file config.properties: " + e.getMessage());
            }
        }
        return props;
    }

    public void connect() {
        Properties cfg = loadExternalConfig();
        String customUrl = cfg.getProperty("db.url", "").trim();
        String user = cfg.getProperty("db.user", "sa").trim();
        String password = cfg.getProperty("db.password", "sapassword").trim();

        if (!customUrl.isEmpty()) {
            try {
                con = DriverManager.getConnection(customUrl, user, password);
                System.out.println("Kết nối SQL Server theo config.properties thành công!");
                return;
            } catch (SQLException e) {
                System.err.println("Chưa kết nối được SQL Server theo config.properties, chuyển sang In-Memory DataStore: " + e.getMessage());
                return;
            }
        }

        String url1 = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyCoffeePOS_HomuCafe;trustServerCertificate=true";
        String url2 = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyCoffeePOS;trustServerCertificate=true";
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

