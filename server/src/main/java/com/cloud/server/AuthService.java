package com.cloud.server;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.sql.*;

public class AuthService {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psSelect;

    public Boolean checkAuthByLoginAndPassword(String login, String password) {
        try {
            dbconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ;

        try {
            psSelect.setString(1, login);
            psSelect.setString(2, password);
            ResultSet rs = psSelect.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                System.out.println("Нет такого пользователя"); //TODO: сделать тут человеческий вывод ошибки
                return false;
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


