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

public class StudentQueries {
    private static Connection connection;
    private static PreparedStatement addStudent;
    private static PreparedStatement getAllStudents;
    private static ResultSet resultSet;

    public static void addStudent(StudentEntry student) {
        connection = DBConnection.getConnection();
        try {
            addStudent = connection.prepareStatement(
                "INSERT INTO STUDENT (STUDENTID, FIRSTNAME, LASTNAME) VALUES (?, ?, ?)"
            );
            addStudent.setString(1, student.getStudentID());
            addStudent.setString(2, student.getFirstName());
            addStudent.setString(3, student.getLastName());
            addStudent.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public static ArrayList<StudentEntry> getAllStudents() {
        Connection conn = DBConnection.getConnection();
        ArrayList<StudentEntry> students = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                "SELECT STUDENTID, FIRSTNAME, LASTNAME FROM STUDENT"
            );
            rs = ps.executeQuery();
            while (rs.next()) {
                students.add(new StudentEntry(
                    rs.getString("STUDENTID"),
                    rs.getString("FIRSTNAME"),
                    rs.getString("LASTNAME")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null)    try { rs.close();    } catch (SQLException ignored) {}
            if (ps != null)    try { ps.close();    } catch (SQLException ignored) {}
        }

        return students;
    }

    
    public static void dropStudent(String studentID) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM STUDENT WHERE STUDENTID = ?");
            stmt.setString(1, studentID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
           }
    }
    
    
    
    public static StudentEntry getStudent(String studentID) {
        Connection conn = DBConnection.getConnection();
        StudentEntry student = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                "SELECT FIRSTNAME, LASTNAME FROM STUDENT WHERE STUDENTID = ?"
            );
            ps.setString(1, studentID);
            rs = ps.executeQuery();
            if (rs.next()) {
                student = new StudentEntry(
                    studentID,
                    rs.getString("FIRSTNAME"),
                    rs.getString("LASTNAME")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null)    try { rs.close();    } catch (SQLException ignored) {}
            if (ps != null)    try { ps.close();    } catch (SQLException ignored) {}
        }

        return student;
    }
}
