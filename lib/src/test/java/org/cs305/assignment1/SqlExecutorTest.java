package org.cs305.assignment1;

import org.cs305.assignment1.classes.Actor;
import org.cs305.assignment1.classes.Film;
import org.cs305.assignment1.classes.SakilaTest0Output;
import org.cs305.assignment1.classes.SakilaTest1;
import org.cs305.assignment1.classes.SakilaTest12A;
import org.cs305.assignment1.classes.SakilaTest12B;
import org.cs305.assignment1.classes.SakilaTest2;
import org.cs305.assignment1.classes.SakilaTest3;
import org.cs305.assignment1.classes.SakilaTest4;
import org.cs305.assignment1.classes.SakilaTest9;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SqlExecutorTest {
    private SqlExecutor sqlExecutor;
    private Connection dbConnection;
    
    @BeforeEach
    void setUp() {
        try {
            //Class.forName("com.mysql.jdbc.Driver");
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila","root","root123");
            dbConnection.setAutoCommit(false);
            sqlExecutor = new SqlExecutor("C:\\Users\\tanuj\\Desktop\\CS305\\cs305_2022\\lib\\src\\test\\resources\\SQLTest.xml", dbConnection);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test setup failed");
        }
    }
    
    @Test
    void testDelete() throws SQLException {
        //test 7 -> int/Integer (and all other primitives)
        dbConnection.createStatement().executeUpdate("INSERT INTO actor(actor_id, first_name, last_name) VALUES(201, \"JAMES\", \"GANDOLFINI\");");
        int deleteId = 201;
        int rowsAffected = sqlExecutor.delete("test7", deleteId);
        assertEquals(1, rowsAffected);

        //test ?.1 -> no query id match
        assertThrows(RuntimeException.class, () -> sqlExecutor.delete("test?", null));

        //test 9 -> Class fields must be primitive/string/array/collection or must have toString() overridden
        assertThrows(RuntimeException.class, () -> {
            SakilaTest1 obj1 = new SakilaTest1("PG", (float)0.99, 5.99, (short)70, 190);
            SakilaTest9 test9Obj = new SakilaTest9(obj1);
            sqlExecutor.delete("test9", test9Obj);
        });
    }

    @Test
    void testInsert() {
        //test 6 -> String
        String fname = "RAHUL";
        int rowsAffected = sqlExecutor.insert("test6", fname);
        assertEquals(1, rowsAffected);

        //test 10 -> no such field in paramQuery
        assertThrows(RuntimeException.class, () -> {
            SakilaTest1 obj1 = new SakilaTest1("PG", (float)0.99, 5.99, (short)70, 190);
            SakilaTest9 test10Obj = new SakilaTest9(obj1);
            sqlExecutor.insert("test10", test10Obj);
        });
    }

    @Test
    void testSelectMany() {
        //test 0 -> null operator
        List<SakilaTest0Output> test0 = sqlExecutor.selectMany("test0", null, SakilaTest0Output.class);
        assertEquals("ACADEMY DINOSAUR", test0.get(0).getTitle());

        //test 1 -> {String, primitives(float, double, int, short)}
        SakilaTest1 test1Obj = new SakilaTest1("PG", (float)0.99, 5.99, (short)70, 190);
        List<Film> test1 = sqlExecutor.selectMany("test1", test1Obj, Film.class);
        assertEquals(158, test1.size());

        //test 2 -> {String[], int[]}
        String[] lastnameArray = new String[]{"KILMER", "JOHANSSON", "HOPKINS"};
        int[] actoridArray = new int[]{50, 134, 8, 157, 153, 55};
        SakilaTest2 test2Obj = new SakilaTest2(lastnameArray, actoridArray);
        List<Actor> test2 = sqlExecutor.selectMany("test2", test2Obj, Actor.class);
        assertEquals("FAY", test2.get(2).getFirst_name());

        //test 4.1 -> {boolean[], char[]}, returns empty list
        boolean[] boolArray = new boolean[]{true, false};
        char[] charArray = new char[]{'a', 'b', 'c'};
        SakilaTest4 test4Obj = new SakilaTest4(boolArray, charArray);
        List<Film> test4 = sqlExecutor.selectMany("test4", test4Obj, Film.class);
        assertEquals(0, test4.size());

        //test 11.a -> no such field in POJO exception
        assertThrows(RuntimeException.class, () -> sqlExecutor.selectMany("test11", test1Obj, Film.class));

        //test 12 -> Generic object (with overriden toString() method)
        SakilaTest12A obj = new SakilaTest12A(lastnameArray);
        SakilaTest12B test12Obj = new SakilaTest12B(obj);
        List<Actor> test12 = sqlExecutor.selectMany("test12", test12Obj, Actor.class);
        assertEquals("GENE", test12.get(2).getFirst_name());
    }

    @Test
    void testSelectOne() {
        //test 3 -> {short[], byte[], long[], float[], double[]}
        assertThrows(RuntimeException.class, () -> {
            short[] idArray = new short[]{(short)275};
            byte[] idArray2 = new byte[]{(byte)23};
            long[] idArray3 = new long[]{(long)323};
            float[] rateStart = new float[]{(float)1.99};
            double[] rateEnd = new double[]{2.99};
            SakilaTest3 test3Obj = new SakilaTest3(idArray, idArray2, idArray3, rateStart, rateEnd);
            sqlExecutor.selectOne("test3", test3Obj, Film.class);
        });

        //test 4.2 -> {boolean[], char[]}, returns null
        boolean[] boolArray = new boolean[]{true, false};
        char[] charArray = new char[]{'a', 'b', 'c'};
        SakilaTest4 test4Obj = new SakilaTest4(boolArray, charArray);
        Film test4 = sqlExecutor.selectOne("test4", test4Obj, Film.class);
        assertNull(test4);

        //test 5 -> List<Integer>
        List<Integer> actoridList = new ArrayList<>();
        actoridList.add(1);
        Actor test5 = sqlExecutor.selectOne("test5", actoridList, Actor.class);
        assertEquals("PENELOPE", test5.getFirst_name());

        //test 11.b -> no such field in POJO exception
        assertThrows(RuntimeException.class, () -> {
            SakilaTest1 test11Obj = new SakilaTest1("PG", (float)0.99, 5.99, (short)70, 190);
            sqlExecutor.selectOne("test11", test11Obj, Film.class);
        });

        //test ?.2 -> no query id match
        assertThrows(RuntimeException.class, () -> sqlExecutor.selectOne("test?", test4Obj, Film.class));
    }

    @Test
    void testUpdate() throws SQLException {
        //test 8
        dbConnection.createStatement().executeUpdate("INSERT INTO actor(actor_id, first_name, last_name) VALUES(201, \"JAMES\", \"GANDOLFINI\");");
        Integer updateId = 201;
        int rowsAffected = sqlExecutor.update("test8", updateId);
        assertEquals(1, rowsAffected);

        //test 8(b) -> null object passed
        assertThrows(RuntimeException.class, () -> sqlExecutor.update("test8", null));

        //test 8(c) -> queryParam and paramType mismatch
        assertThrows(RuntimeException.class, () -> {
            String str = "xyz";
            sqlExecutor.update("test8", str);
        });
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
