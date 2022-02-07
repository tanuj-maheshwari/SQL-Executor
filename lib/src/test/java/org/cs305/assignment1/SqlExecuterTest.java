package org.cs305.assignment1;

import org.cs305.assignment1.classes.Course;
import org.cs305.assignment1.classes.CourseId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlExecuterTest {
    private SqlExecuter sqlExecuter;
    private Connection dbConnection;
    
    @BeforeEach
    void setUp() {
        try {
            //Class.forName("com.mysql.jdbc.Driver");
            Class.forName("org.postgresql.Driver");
            //dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sonoo","root","root");
            dbConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres","postgres","392139");
            sqlExecuter = new SqlExecuter("C:\\Users\\tanuj\\Desktop\\CS305\\cs305_2022\\lib\\src\\test\\java\\org\\cs305\\assignment1\\SQLTest.xml", dbConnection);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test setup failed");
        }
    }
    
    @Test
    void testDelete() {

    }

    @Test
    void testInsert() {

    }

    @Test
    void testSelectMany() {
        int[] a = new int[]{201, 211, 303, 506};
        String[] b = new String[]{"DSA", "FD", "MET", "DM"};
        List<Integer> c = new ArrayList<>();
        c.add(201); c.add(211); c.add(303); c.add(506);
        List<Course> l = sqlExecuter.selectMany("test", c, Course.class);
        for(int i=0; i<l.size(); i++) {
            System.out.println(l.get(i).getTitle());
        }
    }

    @Test
    void testSelectOne() {
        try {
            //Class.forName("org.postgresql.jdbc.Driver");
            //dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sonoo","root","root");
            //dbConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres","postgres","392139");
            //sqlExecuter = new SqlExecuter("SQLTest.xml", dbConnection);
            //CourseId idObj = new CourseId(506);
            //Course c = sqlExecuter.selectOne("test", idObj, Course.class);
            Integer[] a = new Integer[]{506};
            Course c = sqlExecuter.selectOne("test", a, Course.class);
            System.out.println(c.getTitle() + " " + c.getDept_name());
            System.out.println(c.getCredits());
            assertEquals(c.getTitle(), "DM");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testUpdate() {

    }

    @AfterEach
    void tearDown() {
        try {
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
