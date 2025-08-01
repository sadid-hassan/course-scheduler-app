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

public class CourseQueries {
    private static Connection connection;
    private static PreparedStatement addCourse;
    private static PreparedStatement getAllCourseCodes;
    private static ResultSet resultSet;

    public static void addCourse(CourseEntry course) {
        connection = DBConnection.getConnection();
        try {
            addCourse = connection.prepareStatement("INSERT INTO COURSE (COURSECODE, DESCRIPTION) VALUES (?, ?)");
            addCourse.setString(1, course.getCourseCode());
            addCourse.setString(2, course.getDescription());
            addCourse.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public static ArrayList<String> getAllCourseCodes() {
        Connection conn = DBConnection.getConnection();
        ArrayList<String> codes = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                "SELECT COURSECODE FROM COURSE ORDER BY COURSECODE"
            );
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
}