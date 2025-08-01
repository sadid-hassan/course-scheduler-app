/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author acv
 */
public class DBConnection {
    private static Connection connection;
    private static final String USER     = "java";
    private static final String PASSWORD = "java";
    private static final String URL      = "jdbc:derby://localhost:1527/CourseSchedulerDBSadidHassanszh6173";

    public static Connection getConnection() {
        try {
            // if we've never opened it, or if it's been closed, (re)open it
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Could not open database.");
            System.exit(1);
        }
        return connection;
    }


   
}
