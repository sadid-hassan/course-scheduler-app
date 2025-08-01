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

public class ScheduleQueries {
    
    private static Connection conn;
    
    
    public static void addScheduleEntry(ScheduleEntry entry) {
        conn = DBConnection.getConnection();
        String sql = "INSERT INTO SCHEDULE "
                   + "(SEMESTER, STUDENTID, COURSECODE, STATUS, TIMESTAMP) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entry.getSemester());
            ps.setString(2, entry.getStudentID());
            ps.setString(3, entry.getCourseCode());
            ps.setString(4, entry.getStatus());
            ps.setTimestamp(5, entry.getTimestamp());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    public static int getScheduledStudentCount(String semester, String courseCode) {
        conn = DBConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM SCHEDULE "
                   + "WHERE SEMESTER = ? AND COURSECODE = ? AND STATUS = 'S'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, semester);
            ps.setString(2, courseCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    
    public static ArrayList<ScheduleEntry> getScheduleByStudent(String semester, String studentID) {
        conn = DBConnection.getConnection();
        ArrayList<ScheduleEntry> schedule = new ArrayList<>();
        String sql = "SELECT * FROM SCHEDULE WHERE SEMESTER = ? AND STUDENTID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, semester);
            ps.setString(2, studentID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    schedule.add(new ScheduleEntry(
                        rs.getString("SEMESTER"),
                        rs.getString("STUDENTID"),
                        rs.getString("COURSECODE"),
                        rs.getString("STATUS"),
                        rs.getTimestamp("TIMESTAMP")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedule;
    }

    
    public static ArrayList<String> dropStudentScheduleBySemester(String semester, String studentID) {
        conn = DBConnection.getConnection();
        ArrayList<String> result = new ArrayList<>();

        String fetchSql = "SELECT COURSECODE, STATUS "
                        + "FROM SCHEDULE WHERE SEMESTER = ? AND STUDENTID = ?";
        try (PreparedStatement fetch = conn.prepareStatement(fetchSql)) {
            fetch.setString(1, semester);
            fetch.setString(2, studentID);
            try (ResultSet rs = fetch.executeQuery()) {
                while (rs.next()) {
                    String course = rs.getString("COURSECODE");
                    String status = rs.getString("STATUS");
                    if ("S".equals(status)) {
                        result.add("Dropped from scheduled class: " + course);
                        // promote next in waitlist
                        String promSql = 
                          "SELECT STUDENTID FROM SCHEDULE "
                        + "WHERE SEMESTER = ? AND COURSECODE = ? AND STATUS = 'W' "
                        + "ORDER BY TIMESTAMP ASC FETCH FIRST ROW ONLY";
                        try (PreparedStatement prom = conn.prepareStatement(promSql)) {
                            prom.setString(1, semester);
                            prom.setString(2, course);
                            try (ResultSet prs = prom.executeQuery()) {
                                if (prs.next()) {
                                    String nextID = prs.getString("STUDENTID");
                                    // promote
                                    String updSql = 
                                      "UPDATE SCHEDULE SET STATUS = 'S' "
                                    + "WHERE SEMESTER = ? AND COURSECODE = ? AND STUDENTID = ?";
                                    try (PreparedStatement upd = conn.prepareStatement(updSql)) {
                                        upd.setString(1, semester);
                                        upd.setString(2, course);
                                        upd.setString(3, nextID);
                                        upd.executeUpdate();
                                        result.add("Promoted waitlisted: " + nextID + " for " + course);
                                    }
                                }
                            }
                        }
                    } else {
                        result.add("Removed from waitlist for: " + course);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result.add("Error during dropStudentScheduleBySemester");
        }

        // now delete all that student's entries
        String delSql = "DELETE FROM SCHEDULE WHERE SEMESTER = ? AND STUDENTID = ?";
        try (PreparedStatement del = conn.prepareStatement(delSql)) {
            del.setString(1, semester);
            del.setString(2, studentID);
            del.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            result.add("Error deleting schedule rows");
        }

        return result;
    }

    // Drop a single student/class pair
    public static void dropStudentScheduleByClass(String semester, String studentID, String courseCode){
        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                "DELETE FROM SCHEDULE\n" +
                " WHERE SEMESTER = ?\n" +
                "   AND STUDENTID = ?\n" +
                "   AND COURSECODE = ?"
            );
            ps.setString(1, semester);
            ps.setString(2, studentID);
            ps.setString(3, courseCode);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (ps != null)    try { ps.close();    } catch (SQLException ignored) {}
        }
    }

    // Drop an entire course, and return who was removed
    public static ArrayList<String> dropClassBySemesterAndCourse(String semester, String courseCode) {
        conn = DBConnection.getConnection();
        ArrayList<String> dropped = new ArrayList<>();

        String fetch = "SELECT STUDENTID, STATUS "
                     + "FROM SCHEDULE WHERE SEMESTER = ? AND COURSECODE = ?";
        try (PreparedStatement ps = conn.prepareStatement(fetch)) {
            ps.setString(1, semester);
            ps.setString(2, courseCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sid = rs.getString("STUDENTID");
                    String readable = "S".equals(rs.getString("STATUS")) ? "Scheduled" : "Waitlisted";
                    dropped.add(readable + " student " + sid + " dropped from " + courseCode);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dropped.add("Error fetching enrolled students");
        }

        // now delete
        String del = "DELETE FROM SCHEDULE WHERE SEMESTER = ? AND COURSECODE = ?";
        try (PreparedStatement ps = conn.prepareStatement(del)) {
            ps.setString(1, semester);
            ps.setString(2, courseCode);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            dropped.add("Error deleting schedule rows");
        }

        return dropped;
    }

    // Get schedule by course for Admin / display
    public static ArrayList<ScheduleEntry> getScheduleByCourse(String semester, String courseCode) {
        conn = DBConnection.getConnection();
        ArrayList<ScheduleEntry> schedule = new ArrayList<>();
        String sql = "SELECT * FROM SCHEDULE WHERE SEMESTER = ? AND COURSECODE = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, semester);
            ps.setString(2, courseCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    schedule.add(new ScheduleEntry(
                        rs.getString("SEMESTER"),
                        rs.getString("STUDENTID"),
                        rs.getString("COURSECODE"),
                        rs.getString("STATUS"),
                        rs.getTimestamp("TIMESTAMP")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedule;
    }

    
    public static ArrayList<ScheduleEntry> getWaitlistedStudentsByClass(String semester, String courseCode){
        ArrayList<ScheduleEntry> waitlist = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                "SELECT *\n" +
                "  FROM SCHEDULE\n" +
                " WHERE SEMESTER  = ?\n" +
                "   AND COURSECODE= ?\n" +
                "   AND STATUS     = 'W'\n" +
                " ORDER BY TIMESTAMP"
            );
            ps.setString(1, semester);
            ps.setString(2, courseCode);
            rs = ps.executeQuery();
            while (rs.next()) {
                waitlist.add(new ScheduleEntry(
                    rs.getString("SEMESTER"),
                    rs.getString("STUDENTID"),
                    rs.getString("COURSECODE"),
                    "W",
                    rs.getTimestamp("TIMESTAMP")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null)    try { rs.close();    } catch (SQLException ignored) {}
            if (ps != null)    try { ps.close();    } catch (SQLException ignored) {}
        }

        return waitlist;
    }

    // All students WITH any schedule row this semester
    public static ArrayList<StudentEntry> getAllStudentsInSemester(String semester) {
        Connection conn = DBConnection.getConnection();
        ArrayList<StudentEntry> students = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                "SELECT DISTINCT S.STUDENTID, S.FIRSTNAME, S.LASTNAME\n" +
                "  FROM STUDENT S\n" +
                "  JOIN SCHEDULE SCH ON S.STUDENTID = SCH.STUDENTID\n" +
                " WHERE SCH.SEMESTER = ?"
            );
            ps.setString(1, semester);
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

    // All classes WITH any schedule row this semester
    public static ArrayList<ClassEntry> getAllClassesWithEnrolledStudents(String semester) {
        Connection conn = DBConnection.getConnection();
        ArrayList<ClassEntry> classes = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                "SELECT DISTINCT CL.SEMESTER, CL.COURSECODE, CL.SEATS\n" +
                "  FROM CLASS CL\n" +
                "  JOIN SCHEDULE SCH ON CL.COURSECODE = SCH.COURSECODE\n" +
                " WHERE SCH.SEMESTER = ?"
            );
            ps.setString(1, semester);
            rs = ps.executeQuery();
            while (rs.next()) {
                classes.add(new ClassEntry(
                    rs.getString("SEMESTER"),
                    rs.getString("COURSECODE"),
                    rs.getInt("SEATS")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null)    try { rs.close();    } catch (SQLException ignored) {}
            if (ps != null)    try { ps.close();    } catch (SQLException ignored) {}
        }

        return classes;
    }
    
    
    public static void updateScheduleEntry(ScheduleEntry entry) {
        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(
                "UPDATE SCHEDULE\n" +
                "   SET STATUS = 'S'\n" +
                " WHERE SEMESTER  = ?\n" +
                "   AND COURSECODE= ?\n" +
                "   AND STUDENTID = ?"
            );
            ps.setString(1, entry.getSemester());
            ps.setString(2, entry.getCourseCode());
            ps.setString(3, entry.getStudentID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (ps != null)    try { ps.close();    } catch (SQLException ignored) {}
        }
    }
}



