package org.cs305.assignment1;

import org.cs305.assignment1.classes.Actor;
import org.cs305.assignment1.classes.Film;
import org.cs305.assignment1.classes.SakilaTest0Output;
import org.cs305.assignment1.classes.SakilaTest1;
import org.cs305.assignment1.classes.SakilaTest2;
import org.cs305.assignment1.classes.SakilaTest3;
import org.cs305.assignment1.classes.SakilaTest4;
import org.cs305.assignment1.classes.SakilaTest9;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila","root","root123");
            sqlExecuter = new SqlExecuter("C:\\Users\\tanuj\\Desktop\\CS305\\cs305_2022\\lib\\src\\test\\resources\\SQLTest.xml", dbConnection);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test setup failed");
        }
    }
    
    @Test
    void testDelete() {
        //test 7 -> int/Integer (and all other primitives)
        int deleteId = 203;
        int rowsAffected = sqlExecuter.delete("test7", deleteId);
        assertEquals(0, rowsAffected);

        //test ? -> no query id match
        assertThrows(RuntimeException.class, () -> {
            sqlExecuter.delete("test?", null);
        });

        //test 9 -> Class fields must be primitive/string/array/collection
        assertThrows(RuntimeException.class, () -> {
            SakilaTest1 obj1 = new SakilaTest1("PG", (float)0.99, 5.99, (short)70, 190);
            SakilaTest9 test9Obj = new SakilaTest9(obj1);
            sqlExecuter.delete("test9", test9Obj);
        });
    }

    @Test
    void testInsert() {
        //test 6 -> String
        String fname = new String("RAHUL");
        int rowsAffected = sqlExecuter.insert("test6", fname);
        assertEquals(1, rowsAffected);

        //test 10 -> no such field in paramQuery
        assertThrows(RuntimeException.class, () -> {
            SakilaTest1 obj1 = new SakilaTest1("PG", (float)0.99, 5.99, (short)70, 190);
            SakilaTest9 test10Obj = new SakilaTest9(obj1);
            sqlExecuter.insert("test10", test10Obj);
        });
    }

    @Test
    void testSelectMany() {
        //test 0 -> null operator
        List<SakilaTest0Output> test0 = sqlExecuter.selectMany("test0", null, SakilaTest0Output.class);
        assertEquals("ACADEMY DINOSAUR", test0.get(0).getTitle());

        //test 1 -> {String, primitives(float, double, int, short)}
        SakilaTest1 test1Obj = new SakilaTest1("PG", (float)0.99, 5.99, (short)70, 190);
        List<Film> test1 = sqlExecuter.selectMany("test1", test1Obj, Film.class);
        assertEquals(158, test1.size());

        //test 2 -> {String[], int[]}
        String[] lastnameArray = new String[]{"KILMER", "JOHANSSON", "HOPKINS"};
        int[] actoridArray = new int[]{50, 134, 8, 157, 153, 55};
        SakilaTest2 test2Obj = new SakilaTest2(lastnameArray, actoridArray);
        List<Actor> test2 = sqlExecuter.selectMany("test2", test2Obj, Actor.class);
        assertEquals("FAY", test2.get(2).getFirst_name());

        //test 4.1 -> {boolean[], char[]}, returns null
        boolean[] boolArray = new boolean[]{true, false};
        char[] charArray = new char[]{'a', 'b', 'c'};
        SakilaTest4 test4Obj = new SakilaTest4(boolArray, charArray);
        List<Film> test4 = sqlExecuter.selectMany("test4", test4Obj, Film.class);
        assertEquals(null, test4);

        //test 11.a -> no such filed in POJO exception
        assertThrows(RuntimeException.class, () -> {
            sqlExecuter.selectMany("test11", test1Obj, Film.class);
        });
    }

    @Test
    void testSelectOne() {
        //test 3 -> {short[], byte[], long[], float[], double[]}
        short[] idArray = new short[]{(short)275};
        byte[] idArray2 = new byte[]{(byte)23};
        long[] idArray3 = new long[]{(long)323};
        float[] rateStart = new float[]{(float)1.99};
        double[] rateEnd = new double[]{2.99};
        SakilaTest3 test3Obj = new SakilaTest3(idArray, idArray2, idArray3, rateStart, rateEnd);
        Film test3 = sqlExecuter.selectOne("test3", test3Obj, Film.class);
        assertEquals("ADAPTATION HOLES", test3.getTitle());

        //test 4.2 -> {boolean[], char[]}, returns null
        boolean[] boolArray = new boolean[]{true, false};
        char[] charArray = new char[]{'a', 'b', 'c'};
        SakilaTest4 test4Obj = new SakilaTest4(boolArray, charArray);
        Film test4 = sqlExecuter.selectOne("test4", test4Obj, Film.class);
        assertEquals(null, test4);

        //test 5 -> List<Integer>
        List<Integer> actoridList = new ArrayList<>();
        actoridList.add(1); actoridList.add(2); actoridList.add(3);
        Actor test5 = sqlExecuter.selectOne("test5", actoridList, Actor.class);
        assertEquals("PENELOPE", test5.getFirst_name());

        //test 11.b -> no such filed in POJO exception
        assertThrows(RuntimeException.class, () -> {
            SakilaTest1 test11Obj = new SakilaTest1("PG", (float)0.99, 5.99, (short)70, 190);
            sqlExecuter.selectOne("test11", test11Obj, Film.class);
        });
    }

    @Test
    void testUpdate() {
        //test 8
        Integer updateId = 202;
        int rowsAffected = sqlExecuter.update("test8", updateId);
        assertEquals(1, rowsAffected);

        //test 8(b) -> null object passed
        assertThrows(RuntimeException.class, () -> {
            sqlExecuter.update("test8", null);
        });

        //test 8(c) -> queryParam and paramType mismatch
        assertThrows(RuntimeException.class, () -> {
            String str = new String("xyz");
            sqlExecuter.update("test8", str);
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
