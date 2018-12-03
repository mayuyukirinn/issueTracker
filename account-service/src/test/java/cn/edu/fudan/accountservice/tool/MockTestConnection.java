package cn.edu.fudan.accountservice.tool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MockTestConnection {

    private Connection conn;

    public void setupCoon() {
        try {
            Class.forName("com.mysql.jdbc.Driver");//加载数据库驱动
            System.out.println("加载数据库驱动成功");
            String url = "jdbc:mysql://10.141.221.75:3306/issueTracker";//声明数据库test的url
            String user = "root";//数据库的用户名
            String password = "root";//数据库的密码
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("连接数据库成功");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConn() {
        return this.conn;
    }


    public void closeCoon() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

