/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author sadid
 */
import java.sql.*;
import java.util.ArrayList;

public class MultiTableQueries {
    private static Connection connection;
    private static PreparedStatement getAllClassDescriptions;
    private static ResultSet resultSet;

    public static ArrayList<ClassDescription> getAllClassDescriptions(String semester) {
        Connection conn = DBConnection.getConnection();
        ArrayList<ClassDescription> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                "SELECT C.COURSECODE, C.DESCRIPTION, CL.SEATS " +
                "FROM COURSE C JOIN CLASS CL ON C.COURSECODE = CL.COURSECODE " +
                "WHERE CL.SEMESTER = ?"
            );
            ps.setString(1, semester);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ClassDescription(
                    rs.getString("COURSECODE"),
                    rs.getString("DESCRIPTION"),
                    rs.getInt("SEATS")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null)    try { rs.close();    } catch (SQLException ignored) {}
            if (ps != null)    try { ps.close();    } catch (SQLException ignored) {}
        }

        return list;
    }
}
