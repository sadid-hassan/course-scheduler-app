
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import javax.swing.JOptionPane;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;
import java.sql.Statement;
import javax.swing.DefaultComboBoxModel;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author acv
 */
public class MainFrame extends javax.swing.JFrame {

    /**
     * Creates new form MainFrame
     */
    private String currentSemester;
    private String author;
    private String projectDate;
    private DefaultTableModel scheduledStudentsTableModel;
    private DefaultTableModel waitlistedStudentsTableModel;
    

    public MainFrame() {
        //clearAllTableData(); // For testing
        initComponents();
        checkData();
        refreshAllComboBoxes();
        scheduledStudentsTableModel = (DefaultTableModel) scheduledStudentsDisplayTable.getModel();
        waitlistedStudentsTableModel = (DefaultTableModel) waitlistedStudentsDisplayTable.getModel();
       
    }
    
    
    // TESTING FUNCTION TO ERASE TABLE DATA
    public static void clearAllTableData() {
    try (Connection conn = DBConnection.getConnection()) {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM SCHEDULE");
        stmt.executeUpdate("DELETE FROM CLASS");
        stmt.executeUpdate("DELETE FROM COURSE");
        stmt.executeUpdate("DELETE FROM STUDENT");
        stmt.executeUpdate("DELETE FROM SEMESTER");
        System.out.println("All tables cleared.");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    // There are a total of 5 combo boxes
    // 1) currentSemesterComboBox [CURRENT SEMESTER]
    // 2) addClassCourseCodeComboBox [ADMIN -> ADD CLASS -> COURSE CODE]
    // 3) selectClassComboBox [STUDENT -> SCHEDULE CLASS -> SELECT CLASS]
    // 4) selectStudentComboBox [STUDENT -> SCHEDULE CLASS -> SELECT STUDENT]
    // 5) displaySelectStudentComboBox [STUDENT -> DISPLAY SCHEDULE -> SELECT STUDENT] **DOES NOT REFRESH PER SEMESTER
    
    
    // Added more combo boxes, everything is handled by refreshAllComboBoxes
    
    private void refreshAllComboBoxes() {
    rebuildSemesterComboBoxes();
    refreshAddClassCourseComboBox();
    refreshStudentAndClassComboBoxes();
    refreshDisplayScheduleStudentComboBox();
    refreshDropStudentComboBox();
    refreshDropClassComboBox();
    refreshChooseDisplayClassComboBox();
    refreshStudentDropComboBox();
    refreshClassDropComboBoxForSelectedStudent();
}
    
    private void refreshStudentDropComboBox() {
        ArrayList<StudentEntry> students = StudentQueries.getAllStudents();
        studentDropComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(
            students.stream().map(StudentEntry::getStudentID).toArray(String[]::new)
        ));
    }

    private void refreshClassDropComboBoxForSelectedStudent() {
        String studentID = (String) studentDropComboBox.getSelectedItem();
        if (studentID == null) {
            classDropComboBox.setModel(new DefaultComboBoxModel<>(new String[0]));
            return;
        }

        // Look up only the classes this student is on the schedule/waitlist for:
        ArrayList<ScheduleEntry> sched =
            ScheduleQueries.getScheduleByStudent(currentSemester, studentID);

        String[] codes = sched.stream()
                              .map(ScheduleEntry::getCourseCode)
                              .toArray(String[]::new);

        classDropComboBox.setModel(new DefaultComboBoxModel<>(codes));
}
    
    private void refreshChooseDisplayClassComboBox() {
    ArrayList<String> courseCodes = ClassQueries.getAllCourseCodes(currentSemester);
    chooseDisplayClassComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(
        courseCodes.toArray(new String[0])
    ));
}
    
    private void refreshDropClassComboBox() {
    ArrayList<String> courseCodes = ClassQueries.getAllCourseCodes(currentSemester);
    chooseClassAdminComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(
        courseCodes.toArray(new String[0])
    ));
}
    
    private void refreshDropStudentComboBox() {
    ArrayList<StudentEntry> students = StudentQueries.getAllStudents();
    dropStudentComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(
        students.stream().map(StudentEntry::getStudentID).toArray(String[]::new)
    ));
}
    
    // Refreshes #1 (semester combo box), 
    // then calls on other refresh helper functions to refresh other combo boxes after current semester is changed
    public void rebuildSemesterComboBoxes() {
        ArrayList<String> semesters = SemesterQueries.getSemesterList();
        currentSemesterComboBox.setModel(new javax.swing.DefaultComboBoxModel(semesters.toArray()));
        if (semesters.size() > 0) {
            currentSemesterLabel.setText(semesters.get(0));
            currentSemester = semesters.get(0);
        } else {
            currentSemesterLabel.setText("None, add a semester.");
            currentSemester = "None";
        }
        
        // Refreshes #2
        refreshAddClassCourseComboBox();
        // Refreshes #3 + #4
        refreshStudentAndClassComboBoxes();
  
    }
    
    // Refreshes #2
    public void refreshAddClassCourseComboBox() {
        if (!currentSemester.equals("None")) {
                ArrayList<String> allCourses = CourseQueries.getAllCourseCodes();
                ArrayList<String> coursesWithClass = ClassQueries.getAllCourseCodes(currentSemester);

                allCourses.removeAll(coursesWithClass); // Only show ones not already used in CLASS for this semester

                addClassCourseCodeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(allCourses.toArray(new String[0])));
            }
}
    
    // Refreshes #3 and #4
    public void refreshStudentAndClassComboBoxes() {
    ArrayList<StudentEntry> students = StudentQueries.getAllStudents();
    ArrayList<String> courseCodes = ClassQueries.getAllCourseCodes(currentSemester);

    selectStudentComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(
        students.stream().map(StudentEntry::getStudentID).toArray(String[]::new)
    ));

    selectClassComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(
        courseCodes.toArray(new String[0])
    ));
}
    
    // Refreshes #5
    private void refreshDisplayScheduleStudentComboBox() {
    ArrayList<StudentEntry> students = StudentQueries.getAllStudents();

    displaySelectStudentComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(
        students.stream().map(StudentEntry::getStudentID).toArray(String[]::new)
    ));
}

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane6 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        addSemesterTextfield = new javax.swing.JTextField();
        addSemesterSubmitButton = new javax.swing.JButton();
        addSemesterStatusLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        addCourseCodeTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        addCourseDescriptionTextField = new javax.swing.JTextField();
        addCourseSubmitButton = new javax.swing.JButton();
        addCourseStatusLabel = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        addClassCodeLabel = new javax.swing.JLabel();
        addClassSeatsLabel = new javax.swing.JLabel();
        addClassSubmitButton = new javax.swing.JButton();
        addClassStatusLabel = new javax.swing.JLabel();
        addClassSeatsSpinner = new javax.swing.JSpinner();
        addClassCourseCodeComboBox = new javax.swing.JComboBox<>();
        jPanel7 = new javax.swing.JPanel();
        addStudentIDLabel = new javax.swing.JLabel();
        addStudentIDTextField = new javax.swing.JTextField();
        addStudentFirstNameLabel = new javax.swing.JLabel();
        addStudentFirstNameTextField = new javax.swing.JTextField();
        addStudentLastNameLabel = new javax.swing.JLabel();
        addStudentLastNameTextField = new javax.swing.JTextField();
        addStudentSubmitButton = new javax.swing.JButton();
        addStudentStatusLabel = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        dropStudentComboBox = new javax.swing.JComboBox<>();
        dropStudentSubmitButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        dropStudentTextArea = new javax.swing.JTextArea();
        jPanel11 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        chooseClassAdminComboBox = new javax.swing.JComboBox<>();
        dropAdminClassSubmitButton = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        dropClassAdminTextArea = new javax.swing.JTextArea();
        jPanel12 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        chooseDisplayClassComboBox = new javax.swing.JComboBox<>();
        displayClassListButton = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        scheduledStudentsDisplayTable = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        waitlistedStudentsDisplayTable = new javax.swing.JTable();
        jLabel14 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        displayClassesJTable = new javax.swing.JTable();
        displayClassesButton = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        selectClassComboBox = new javax.swing.JComboBox<>();
        selectStudentLabel = new javax.swing.JLabel();
        selectStudentComboBox = new javax.swing.JComboBox<>();
        scheduleClassSubmitBox = new javax.swing.JButton();
        scheduleClassStatusLabel = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        displayScheduleJTable = new javax.swing.JTable();
        displayScheduleButton = new javax.swing.JButton();
        displaySelectStudentLabel = new javax.swing.JLabel();
        displaySelectStudentComboBox = new javax.swing.JComboBox<>();
        jPanel13 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        studentDropComboBox = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        classDropComboBox = new javax.swing.JComboBox<>();
        dropStudentClassSubmitButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        dropClassTextArea = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        currentSemesterLabel = new javax.swing.JLabel();
        currentSemesterComboBox = new javax.swing.JComboBox<>();
        changeSemesterButton = new javax.swing.JButton();
        aboutButton = new javax.swing.JButton();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane6.setViewportView(jTable1);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane7.setViewportView(jTable2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Comic Sans MS", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 153, 153));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Course Scheduler");

        jLabel3.setText("Semester Name:");

        addSemesterTextfield.setColumns(20);

        addSemesterSubmitButton.setText("Submit");
        addSemesterSubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSemesterSubmitButtonActionPerformed(evt);
            }
        });

        addSemesterStatusLabel.setText("                                                   ");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(addSemesterTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(87, 87, 87)
                        .addComponent(addSemesterSubmitButton))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(addSemesterStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(383, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(addSemesterTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(addSemesterSubmitButton)
                .addGap(18, 18, 18)
                .addComponent(addSemesterStatusLabel)
                .addContainerGap(164, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Add Semester", jPanel3);

        jLabel4.setText("Course Code:");

        addCourseCodeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCourseCodeTextFieldActionPerformed(evt);
            }
        });

        jLabel5.setText("Course Description:");

        addCourseSubmitButton.setText("Submit");
        addCourseSubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCourseSubmitButtonActionPerformed(evt);
            }
        });

        addCourseStatusLabel.setText("     ");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addCourseStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 412, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addCourseSubmitButton)
                            .addComponent(addCourseCodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addCourseDescriptionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(312, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(addCourseCodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(addCourseDescriptionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(addCourseSubmitButton)
                .addGap(18, 18, 18)
                .addComponent(addCourseStatusLabel)
                .addContainerGap(124, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Add Course", jPanel4);

        addClassCodeLabel.setText("Course Code:");

        addClassSeatsLabel.setText("Total Seats:");

        addClassSubmitButton.setText("Submit");
        addClassSubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClassSubmitButtonActionPerformed(evt);
            }
        });

        addClassStatusLabel.setText("                                                ");

        addClassCourseCodeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        addClassCourseCodeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClassCourseCodeComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addClassStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addComponent(addClassSubmitButton))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(addClassCodeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(addClassCourseCodeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(addClassSeatsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(addClassSeatsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(401, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addClassCodeLabel)
                    .addComponent(addClassCourseCodeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addClassSeatsLabel)
                    .addComponent(addClassSeatsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(addClassSubmitButton)
                .addGap(18, 18, 18)
                .addComponent(addClassStatusLabel)
                .addContainerGap(121, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Add Class", jPanel6);

        addStudentIDLabel.setText("StudentID:");

        addStudentFirstNameLabel.setText("First Name:");

        addStudentLastNameLabel.setText("Last Name:");

        addStudentSubmitButton.setText("Submit");
        addStudentSubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStudentSubmitButtonActionPerformed(evt);
            }
        });

        addStudentStatusLabel.setText("                                        ");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                    .addComponent(addStudentFirstNameLabel)
                                    .addGap(18, 18, 18))
                                .addGroup(jPanel7Layout.createSequentialGroup()
                                    .addComponent(addStudentIDLabel)
                                    .addGap(23, 23, 23)))
                            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(addStudentIDTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                                .addComponent(addStudentFirstNameTextField)))
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addComponent(addStudentLastNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(addStudentSubmitButton)
                                .addComponent(addStudentLastNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))))
                    .addComponent(addStudentStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(342, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addStudentIDLabel)
                    .addComponent(addStudentIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addStudentFirstNameLabel)
                    .addComponent(addStudentFirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addStudentLastNameLabel)
                    .addComponent(addStudentLastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(addStudentSubmitButton)
                .addGap(18, 18, 18)
                .addComponent(addStudentStatusLabel)
                .addContainerGap(96, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Add Student", jPanel7);

        jLabel7.setText("Choose Student:");

        dropStudentComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        dropStudentComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dropStudentComboBoxActionPerformed(evt);
            }
        });

        dropStudentSubmitButton.setText("Submit");
        dropStudentSubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dropStudentSubmitButtonActionPerformed(evt);
            }
        });

        dropStudentTextArea.setColumns(20);
        dropStudentTextArea.setRows(5);
        jScrollPane3.setViewportView(dropStudentTextArea);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(dropStudentSubmitButton)
                        .addGroup(jPanel10Layout.createSequentialGroup()
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(dropStudentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(399, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(dropStudentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addComponent(dropStudentSubmitButton)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(53, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Drop Student", jPanel10);

        jLabel10.setText("Choose Class:");

        chooseClassAdminComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        dropAdminClassSubmitButton.setText("Submit");
        dropAdminClassSubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dropAdminClassSubmitButtonActionPerformed(evt);
            }
        });

        dropClassAdminTextArea.setColumns(20);
        dropClassAdminTextArea.setRows(5);
        jScrollPane5.setViewportView(dropClassAdminTextArea);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dropAdminClassSubmitButton)
                            .addComponent(chooseClassAdminComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(399, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(chooseClassAdminComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(dropAdminClassSubmitButton)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Drop Class", jPanel11);

        jLabel11.setText("Choose Class: ");

        chooseDisplayClassComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        chooseDisplayClassComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseDisplayClassComboBoxActionPerformed(evt);
            }
        });

        displayClassListButton.setText("Display");
        displayClassListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayClassListButtonActionPerformed(evt);
            }
        });

        jLabel12.setText("Scheduled Students in the Class");

        scheduledStudentsDisplayTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Last Name", "First Name", "StudentID"
            }
        ));
        jScrollPane8.setViewportView(scheduledStudentsDisplayTable);

        jLabel13.setText("Waitlisted Students in the Class in Waitlist Order");

        waitlistedStudentsDisplayTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Last Name", "First Name", "StudentID"
            }
        ));
        jScrollPane9.setViewportView(waitlistedStudentsDisplayTable);

        jLabel14.setText("Scheduled Students in the Class");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(chooseDisplayClassComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(displayClassListButton))
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(366, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(chooseDisplayClassComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(displayClassListButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addGap(2, 2, 2)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(413, 413, 413)
                .addComponent(jLabel12)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Display Class List", jPanel12);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Admin", jPanel1);

        displayClassesJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Course Code", "Description", "Seats"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(displayClassesJTable);

        displayClassesButton.setText("Display");
        displayClassesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayClassesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 738, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(displayClassesButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(displayClassesButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Display Classes", jPanel5);

        jLabel6.setText("Select Class:");

        selectClassComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        selectClassComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectClassComboBoxActionPerformed(evt);
            }
        });

        selectStudentLabel.setText("Select Student:");

        selectStudentComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        selectStudentComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectStudentComboBoxActionPerformed(evt);
            }
        });

        scheduleClassSubmitBox.setText("Submit");
        scheduleClassSubmitBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scheduleClassSubmitBoxActionPerformed(evt);
            }
        });

        scheduleClassStatusLabel.setText("                                       ");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scheduleClassStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 414, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(selectStudentLabel))
                        .addGap(28, 28, 28)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scheduleClassSubmitBox)
                            .addComponent(selectClassComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(selectStudentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(selectClassComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectStudentLabel)
                    .addComponent(selectStudentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(scheduleClassSubmitBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scheduleClassStatusLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Schedule Class", jPanel8);

        displayScheduleJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Course Code", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(displayScheduleJTable);

        displayScheduleButton.setText("Display");
        displayScheduleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayScheduleButtonActionPerformed(evt);
            }
        });

        displaySelectStudentLabel.setText("Select Student:");

        displaySelectStudentComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(displayScheduleButton)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(displaySelectStudentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(displaySelectStudentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(displaySelectStudentLabel)
                    .addComponent(displaySelectStudentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(displayScheduleButton)
                .addGap(34, 34, 34))
        );

        jTabbedPane3.addTab("Display Schedule", jPanel9);

        jLabel8.setText("Choose Student:");

        studentDropComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        studentDropComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                studentDropComboBoxActionPerformed(evt);
            }
        });

        jLabel9.setText("Choose Class:");

        classDropComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        dropStudentClassSubmitButton.setText("Submit");
        dropStudentClassSubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dropStudentClassSubmitButtonActionPerformed(evt);
            }
        });

        dropClassTextArea.setColumns(20);
        dropClassTextArea.setRows(5);
        jScrollPane4.setViewportView(dropClassTextArea);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dropStudentClassSubmitButton)
                            .addComponent(studentDropComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(classDropComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(406, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(studentDropComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(classDropComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(dropStudentClassSubmitButton)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Drop Class", jPanel13);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane3)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jTabbedPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTabbedPane3.getAccessibleContext().setAccessibleName("");

        jTabbedPane1.addTab("Student", jPanel2);

        jLabel2.setFont(new java.awt.Font("Comic Sans MS", 1, 16)); // NOI18N
        jLabel2.setText("Current Semester: ");

        currentSemesterLabel.setFont(new java.awt.Font("Comic Sans MS", 0, 16)); // NOI18N
        currentSemesterLabel.setText("           ");

        currentSemesterComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        currentSemesterComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentSemesterComboBoxActionPerformed(evt);
            }
        });

        changeSemesterButton.setText("Change Semester");
        changeSemesterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeSemesterButtonActionPerformed(evt);
            }
        });

        aboutButton.setText("About");
        aboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(currentSemesterLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(currentSemesterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(changeSemesterButton)
                                .addGap(31, 31, 31)
                                .addComponent(aboutButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(currentSemesterLabel)
                    .addComponent(currentSemesterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changeSemesterButton)
                    .addComponent(aboutButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

   
    private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
        // TODO add your handling code here:
        // display about information.
        JOptionPane.showMessageDialog(null, "Author: " + author + " Project Date: " + projectDate);
    }//GEN-LAST:event_aboutButtonActionPerformed

    private void addSemesterSubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSemesterSubmitButtonActionPerformed
        String semester = addSemesterTextfield.getText();
        SemesterQueries.addSemester(semester);
        addSemesterStatusLabel.setText("Semester " + semester + " has been added.");
        rebuildSemesterComboBoxes();
    }//GEN-LAST:event_addSemesterSubmitButtonActionPerformed

    private void addCourseCodeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCourseCodeTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addCourseCodeTextFieldActionPerformed

    private void addCourseSubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCourseSubmitButtonActionPerformed
        String courseCode = addCourseCodeTextField.getText().trim();
    String courseDescription = addCourseDescriptionTextField.getText().trim();

        if (!courseCode.isEmpty() && !courseDescription.isEmpty()) {
            CourseEntry newCourse = new CourseEntry(courseCode, courseDescription);
            CourseQueries.addCourse(newCourse);
            addCourseStatusLabel.setText("Course " + courseCode + " has been added.");
            refreshAddClassCourseComboBox();
        } else {
            addCourseStatusLabel.setText("Please enter both a course code and description.");
        }
        
       
    }//GEN-LAST:event_addCourseSubmitButtonActionPerformed

    private void addClassSubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClassSubmitButtonActionPerformed
        String selectedCourseCode = (String) addClassCourseCodeComboBox.getSelectedItem();
        int totalSeats = (int) addClassSeatsSpinner.getValue();

        if (selectedCourseCode != null && !currentSemester.equals("None")) {
            ClassEntry classEntry = new ClassEntry(currentSemester, selectedCourseCode, totalSeats);
            ClassQueries.addClass(classEntry);
            addClassStatusLabel.setText("Class " + selectedCourseCode + " added for " + currentSemester + ".");
        } else {
            addClassStatusLabel.setText("Select a course and valid semester.");
        }
        
        refreshAllComboBoxes();
    }//GEN-LAST:event_addClassSubmitButtonActionPerformed

    private void addStudentSubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStudentSubmitButtonActionPerformed
        String id = addStudentIDTextField.getText().trim();
    String firstName = addStudentFirstNameTextField.getText().trim();
    String lastName = addStudentLastNameTextField.getText().trim();

        if (!id.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty()) {
            StudentEntry student = new StudentEntry(id, firstName, lastName);
            StudentQueries.addStudent(student);
            addStudentStatusLabel.setText("Student " + firstName + " " + lastName + " has been added.");
            
            // Refresh combo box selections
            refreshAllComboBoxes();
        } else {
            addStudentStatusLabel.setText("Please enter all fields.");
        }
        
        
        
    }//GEN-LAST:event_addStudentSubmitButtonActionPerformed

    private void scheduleClassSubmitBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scheduleClassSubmitBoxActionPerformed
        String selectedStudent = (String) selectStudentComboBox.getSelectedItem();
    String selectedClass = (String) selectClassComboBox.getSelectedItem();

    if (selectedStudent == null || selectedClass == null || currentSemester.equals("None")) {
        scheduleClassStatusLabel.setText("Select a valid student and class.");
        return;
    }

    Timestamp timestamp = new Timestamp(Calendar.getInstance().getTime().getTime());

    // Check how many students are scheduled for this class
    int scheduledCount = ScheduleQueries.getScheduledStudentCount(currentSemester, selectedClass);

    // Get seat limit from CLASS table
    int seatLimit = ClassQueries.getAllCourseCodes(currentSemester).contains(selectedClass) ? getSeatsForClass(currentSemester, selectedClass) : 0;

    String status = (scheduledCount < seatLimit) ? "S" : "W";

    ScheduleEntry entry = new ScheduleEntry(currentSemester, selectedStudent, selectedClass, status, timestamp);
    ScheduleQueries.addScheduleEntry(entry);

    scheduleClassStatusLabel.setText("Student " + selectedStudent + " " +
        (status.equals("S") ? "scheduled" : "waitlisted") + " for " + selectedClass + ".");
    }//GEN-LAST:event_scheduleClassSubmitBoxActionPerformed

    private void changeSemesterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeSemesterButtonActionPerformed
        currentSemester = (String) currentSemesterComboBox.getSelectedItem();
        currentSemesterLabel.setText(currentSemester);

        // Refresh components that depend on the current semester
        rebuildClassComboBoxes();
        refreshAddClassCourseComboBox();
        rebuildDisplayClassesTable();
        refreshStudentAndClassComboBoxes();
        
    }//GEN-LAST:event_changeSemesterButtonActionPerformed

    private void selectClassComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectClassComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_selectClassComboBoxActionPerformed

    
    
    private void displayScheduleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayScheduleButtonActionPerformed
    // Get selected student
    String studentID = (String) displaySelectStudentComboBox.getSelectedItem();

    // Clear table
    DefaultTableModel model = (DefaultTableModel) displayScheduleJTable.getModel();
    model.setRowCount(0);

    // Query student’s schedule
    ArrayList<ScheduleEntry> schedule = ScheduleQueries.getScheduleByStudent(currentSemester, studentID);

    // Fill table
        for (ScheduleEntry entry : schedule) {
            String readableStatus = entry.getStatus().equals("S") ? "Scheduled" : "Waitlisted";
            model.addRow(new Object[]{entry.getCourseCode(), readableStatus});
        }
    }//GEN-LAST:event_displayScheduleButtonActionPerformed

    private void displayClassesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayClassesButtonActionPerformed
        // Clear the table
    DefaultTableModel model = (DefaultTableModel) displayClassesJTable.getModel();
    model.setRowCount(0);

    // Get all class info for the current semester
    ArrayList<ClassDescription> classDescriptions = MultiTableQueries.getAllClassDescriptions(currentSemester);

    // Add to table
    for (ClassDescription c : classDescriptions) {
        model.addRow(new Object[]{
                c.getCourseCode(),
                c.getDescription(),
                c.getSeats()
            });
        }
    }//GEN-LAST:event_displayClassesButtonActionPerformed

    private void addClassCourseCodeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClassCourseCodeComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addClassCourseCodeComboBoxActionPerformed

    private void currentSemesterComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentSemesterComboBoxActionPerformed
        currentSemester = (String) currentSemesterComboBox.getSelectedItem();
    }//GEN-LAST:event_currentSemesterComboBoxActionPerformed

    private void selectStudentComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectStudentComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_selectStudentComboBoxActionPerformed

    private void chooseDisplayClassComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseDisplayClassComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chooseDisplayClassComboBoxActionPerformed

    private void dropStudentSubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dropStudentSubmitButtonActionPerformed
        String studentID = (String) dropStudentComboBox.getSelectedItem();
        ArrayList<String> droppedInfo = ScheduleQueries.dropStudentScheduleBySemester(currentSemester, studentID);
        StudentQueries.dropStudent(studentID);
        
        StringBuilder result = new StringBuilder();
        result.append("Student ").append(studentID).append(" has been dropped.\n");

        for (String message : droppedInfo) {
            result.append(message).append("\n");
        }

        dropStudentTextArea.setText(result.toString());

        refreshAllComboBoxes();
    
    }//GEN-LAST:event_dropStudentSubmitButtonActionPerformed

    private void dropAdminClassSubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dropAdminClassSubmitButtonActionPerformed
        String selectedClass = (String) chooseClassAdminComboBox.getSelectedItem();

        if (selectedClass == null || currentSemester.equals("None")) {
            dropClassAdminTextArea.setText("Please select a valid class.");
            return;
        }

        // 1. Drop class entries from SCHEDULE and get affected students
        ArrayList<String> droppedMessages = ScheduleQueries.dropClassBySemesterAndCourse(currentSemester, selectedClass);

        // 2. Drop the class itself
        ClassQueries.dropClass(currentSemester, selectedClass);

        // 3. Output text
        StringBuilder result = new StringBuilder("Class " + selectedClass + " has been dropped.\n");
        for (String msg : droppedMessages) {
            result.append(msg).append("\n");
        }

        dropClassAdminTextArea.setText(result.toString());

        // 4. Refresh combo boxes
        refreshAllComboBoxes();
    }//GEN-LAST:event_dropAdminClassSubmitButtonActionPerformed

    private void dropStudentComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dropStudentComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dropStudentComboBoxActionPerformed

    private void displayClassListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayClassListButtonActionPerformed
        String courseCode = (String) chooseDisplayClassComboBox.getSelectedItem();

        ArrayList<ScheduleEntry> entries = ScheduleQueries.getScheduleByCourse(currentSemester, courseCode);

        scheduledStudentsTableModel.setRowCount(0);
        waitlistedStudentsTableModel.setRowCount(0);

        for (ScheduleEntry entry : entries) {
            StudentEntry student = StudentQueries.getStudent(entry.getStudentID());

            Object[] row = {
                student.getLastName(),
                student.getFirstName(),
                student.getStudentID()
            };

            if (entry.getStatus().equalsIgnoreCase("S")) {
                scheduledStudentsTableModel.addRow(row);
            } else if (entry.getStatus().equalsIgnoreCase("W")) {
                waitlistedStudentsTableModel.addRow(row);
            }
        }
    }//GEN-LAST:event_displayClassListButtonActionPerformed

    private void dropStudentClassSubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dropStudentClassSubmitButtonActionPerformed
        String semester    = (String) currentSemesterComboBox.getSelectedItem();
        String studentID   = (String) studentDropComboBox.getSelectedItem();
        String courseCode  = (String) classDropComboBox.getSelectedItem();

        if (studentID == null || courseCode == null) {
            JOptionPane.showMessageDialog(this, "Please select both student and class.");
            return;
        }

        // Remove that one schedule row
        ScheduleQueries.dropStudentScheduleByClass(semester, studentID, courseCode);

        // Get the very next waitlisted student (oldest timestamp)
        ArrayList<ScheduleEntry> waitlist = ScheduleQueries.getWaitlistedStudentsByClass(semester, courseCode);

        // Build our output
        StringBuilder out = new StringBuilder();
        out.append("Dropped ").append(studentID)
           .append(" from ").append(courseCode).append("\n");

        if (!waitlist.isEmpty()) {
            ScheduleEntry promoted = waitlist.get(0);
            // Flip the status to S
            ScheduleQueries.updateScheduleEntry(promoted);

            out.append("Promoted waitlisted student: ")
               .append(promoted.getStudentID())
               .append(" into ").append(courseCode).append("\n");
        }

        // Refresh everything, update message
        refreshAllComboBoxes();
        dropClassTextArea.setText(out.toString());

    }//GEN-LAST:event_dropStudentClassSubmitButtonActionPerformed

    private void studentDropComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_studentDropComboBoxActionPerformed
        refreshClassDropComboBoxForSelectedStudent();
    }//GEN-LAST:event_studentDropComboBoxActionPerformed

    private void rebuildClassComboBoxes() {
        ArrayList<String> courseCodes = ClassQueries.getAllCourseCodes(currentSemester);
        addClassCourseCodeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(courseCodes.toArray(new String[0])));
}
    
    
    private void rebuildDisplayClassesTable() {
        ArrayList<ClassDescription> classDescriptions = MultiTableQueries.getAllClassDescriptions(currentSemester);
        DefaultTableModel model = (DefaultTableModel) displayClassesJTable.getModel();
        model.setRowCount(0); // clear table

        for (ClassDescription desc : classDescriptions) {
            model.addRow(new Object[]{desc.getCourseCode(), desc.getDescription(), desc.getSeats()});
        }
}
    
    private int getSeatsForClass(String semester, String courseCode) {
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT SEATS FROM CLASS WHERE SEMESTER = ? AND COURSECODE = ?"
            );
            stmt.setString(1, semester);
            stmt.setString(2, courseCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("SEATS");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
}
    
    
    private void checkData() {
        try {
            FileReader reader = new FileReader("xzq789yy.txt");
            BufferedReader breader = new BufferedReader(reader);

            String encodedAuthor = breader.readLine();
            String encodedProject = breader.readLine();
            byte[] decodedAuthor = Base64.getDecoder().decode(encodedAuthor);
            author = new String(decodedAuthor);
            byte[] decodedProject = Base64.getDecoder().decode(encodedProject);
            projectDate = new String(decodedProject);
            reader.close();

        } catch (FileNotFoundException e) {
            //get user info and create file
            author = JOptionPane.showInputDialog("Enter your first and last name.");
            projectDate = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()).toString();

            //write data to the data file.
            try {
                FileWriter writer = new FileWriter("xzq789yy.txt", true);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);

                // encode the output data.
                String encodedAuthor = Base64.getEncoder().encodeToString(author.getBytes());

                bufferedWriter.write(encodedAuthor);
                bufferedWriter.newLine();

                String encodedProject = Base64.getEncoder().encodeToString(projectDate.getBytes());
                bufferedWriter.write(encodedProject);

                bufferedWriter.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.exit(1);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutButton;
    private javax.swing.JLabel addClassCodeLabel;
    private javax.swing.JComboBox<String> addClassCourseCodeComboBox;
    private javax.swing.JLabel addClassSeatsLabel;
    private javax.swing.JSpinner addClassSeatsSpinner;
    private javax.swing.JLabel addClassStatusLabel;
    private javax.swing.JButton addClassSubmitButton;
    private javax.swing.JTextField addCourseCodeTextField;
    private javax.swing.JTextField addCourseDescriptionTextField;
    private javax.swing.JLabel addCourseStatusLabel;
    private javax.swing.JButton addCourseSubmitButton;
    private javax.swing.JLabel addSemesterStatusLabel;
    private javax.swing.JButton addSemesterSubmitButton;
    private javax.swing.JTextField addSemesterTextfield;
    private javax.swing.JLabel addStudentFirstNameLabel;
    private javax.swing.JTextField addStudentFirstNameTextField;
    private javax.swing.JLabel addStudentIDLabel;
    private javax.swing.JTextField addStudentIDTextField;
    private javax.swing.JLabel addStudentLastNameLabel;
    private javax.swing.JTextField addStudentLastNameTextField;
    private javax.swing.JLabel addStudentStatusLabel;
    private javax.swing.JButton addStudentSubmitButton;
    private javax.swing.JButton changeSemesterButton;
    private javax.swing.JComboBox<String> chooseClassAdminComboBox;
    private javax.swing.JComboBox<String> chooseDisplayClassComboBox;
    private javax.swing.JComboBox<String> classDropComboBox;
    private javax.swing.JComboBox<String> currentSemesterComboBox;
    private javax.swing.JLabel currentSemesterLabel;
    private javax.swing.JButton displayClassListButton;
    private javax.swing.JButton displayClassesButton;
    private javax.swing.JTable displayClassesJTable;
    private javax.swing.JButton displayScheduleButton;
    private javax.swing.JTable displayScheduleJTable;
    private javax.swing.JComboBox<String> displaySelectStudentComboBox;
    private javax.swing.JLabel displaySelectStudentLabel;
    private javax.swing.JButton dropAdminClassSubmitButton;
    private javax.swing.JTextArea dropClassAdminTextArea;
    private javax.swing.JTextArea dropClassTextArea;
    private javax.swing.JButton dropStudentClassSubmitButton;
    private javax.swing.JComboBox<String> dropStudentComboBox;
    private javax.swing.JButton dropStudentSubmitButton;
    private javax.swing.JTextArea dropStudentTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JLabel scheduleClassStatusLabel;
    private javax.swing.JButton scheduleClassSubmitBox;
    private javax.swing.JTable scheduledStudentsDisplayTable;
    private javax.swing.JComboBox<String> selectClassComboBox;
    private javax.swing.JComboBox<String> selectStudentComboBox;
    private javax.swing.JLabel selectStudentLabel;
    private javax.swing.JComboBox<String> studentDropComboBox;
    private javax.swing.JTable waitlistedStudentsDisplayTable;
    // End of variables declaration//GEN-END:variables
}
