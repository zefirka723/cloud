package com.cloud.server;

import com.cloud.common.Command;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.sql.*;

public class AuthService {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psSelect;

    public String getPathByLoginAndPassword(String request) {
        try {
            dbconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ;

        try {
            String[] params = request.split("\\s", 2);
            psSelect.setString(1, params[0]);
            psSelect.setString(2, params[1]);
            ResultSet rs = psSelect.executeQuery();
            if (rs.next()) {
                return params[0];
                        //new Command("AUTH_OK", params[0]);
            } else {
                return null;
                        //new Command("AUTH_DENIED", null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            dbdisconnect();
        }
        return null;
    }

    public static void dbconnect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        prepareStatements();
        stmt = connection.createStatement();
    }

    public static void prepareStatements() throws SQLException {
        psSelect = connection.prepareStatement("SELECT login FROM auth where login = ? AND password = ?;");
    }

    public static void dbdisconnect() {
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            psSelect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}


