/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author acv
 */
public class SemesterQueries {
    private static Connection connection;
    private static PreparedStatement addSemester;
    private static PreparedStatement getSemesterList;
    private static ResultSet resultSet;
    
    public static void addSemester(String name)
    {
        connection = DBConnection.getConnection();
        try
        {
            addSemester = connection.prepareStatement("insert into semester (semester) values (?)");
            addSemester.setString(1, name);
            addSemester.executeUpdate();
        }
        catch(SQLException sqlException)
        {
            sqlException.printStackTrace();
        }
        
    }
    
    public static ArrayList<String> getSemesterList() {
        Connection conn = DBConnection.getConnection();
        ArrayList<String> semesters = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                "SELECT SEMESTER FROM SEMESTER ORDER BY SEMESTER"
            );
            rs = ps.executeQuery();
            while (rs.next()) {
                semesters.add(rs.getString("SEMESTER"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null)    try { rs.close();    } catch (SQLException ignored) {}
            if (ps != null)    try { ps.close();    } catch (SQLException ignored) {}
        }

        return semesters;
    }
    
    
}
