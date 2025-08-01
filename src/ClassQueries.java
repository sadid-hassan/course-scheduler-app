/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author sadid
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClassQueries {
    private static Connection connection;
    private static PreparedStatement addClass;
    private static PreparedStatement getAllCourseCodes;
    private static ResultSet resultSet;

    public static void addClass(ClassEntry classEntry) {
        connection = DBConnection.getConnection();
        try {
            addClass = connection.prepareStatement(
                "INSERT INTO CLASS (SEMESTER, COURSECODE, SEATS) VALUES (?, ?, ?)"
            );
            addClass.setString(1, classEntry.getSemester());
            addClass.setString(2, classEntry.getCourseCode());
            addClass.setInt(3, classEntry.getSeats());
            addClass.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public static ArrayList<String> getAllCourseCodes(String semester) {
        Connection conn = DBConnection.getConnection();
        ArrayList<String> codes = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                "SELECT COURSECODE FROM CLASS WHERE SEMESTER = ?"
            );
            ps.setString(1, semester);
            rs = ps.executeQuery();
            while (rs.next()) {
                codes.add(rs.getString("COURSECODE"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null)    try { rs.close();    } catch (SQLException ignored) {}
            if (ps != null)    try { ps.close();    } catch (SQLException ignored) {}
        }

        return codes;
    }
    
    public static void dropClass(String semester, String courseCode) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM CLASS WHERE SEMESTER = ? AND COURSECODE = ?"
            );
            stmt.setString(1, semester);
            stmt.setString(2, courseCode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
}