package org.guess880.h2_oracle_funcs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class OracleFunctionsTest {

    private static Connection con;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Class.forName("org.h2.Driver");
        con = DriverManager.getConnection("jdbc:h2:mem:");
        final Statement stmt = con.createStatement();
        try {
            stmt.execute("CREATE TABLE dual (DUMMY VARCHAR2(1))");
            stmt.execute("INSERT INTO dual (dummy) values ('X')");
        } finally {
            stmt.close();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        con.close();
    }

    @Test
    public void testItAddMonths() throws Exception {
        final Statement stmt = con.createStatement();
        try {
            stmt.execute("CREATE ALIAS ADD_MONTH FOR \"org.guess880.h2_oracle_funcs.OracleFunctions.addMonths\"");
            stmt.execute("CREATE TABLE tbl_add_months (DT DATETIME, D DATE)");
            stmt.execute("INSERT INTO tbl_add_months SELECT * FROM CSVREAD('classpath:/org/guess880/h2_oracle_funcs/add_months.csv')");
            ResultSet rs = stmt.executeQuery("SELECT ADD_MONTH(dt, 1), ADD_MONTH(d, 1) FROM tbl_add_months");
            if (rs.next()) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Date expected = format.parse("2000-02-01 12:34:56.789");
                assertThat(rs.getTimestamp(1), equalTo(expected));
                format = new SimpleDateFormat("yyyy-MM-dd");
                expected = format.parse("2000-02-01");
                assertThat(rs.getDate(2), equalTo(expected));
            } else {
                fail("tbl_add_months has no record.");
            }
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testAsciiStr() {
        assertThat(OracleFunctions.asciistr("ABÄCDE"), equalTo("AB\\00C4CDE"));
        final char[] chars = new char[2];
        chars[0] = '\u007f';
        chars[1] = '\u0080';
        assertThat(OracleFunctions.asciistr(String.copyValueOf(chars)), equalTo("\\0080"));
    }

    @Test
    public void testItAsciiStr() throws Exception {
        final Statement stmt = con.createStatement();
        try {
            stmt.execute("CREATE ALIAS ASCIISTR FOR \"org.guess880.h2_oracle_funcs.OracleFunctions.asciistr\"");
            stmt.execute("CREATE TABLE tbl_asciistr (S VARCHAR)");
            stmt.execute("INSERT INTO tbl_asciistr SELECT * FROM CSVREAD('classpath:/org/guess880/h2_oracle_funcs/asciistr.csv')");
            ResultSet rs = stmt.executeQuery("SELECT ASCIISTR(s) FROM tbl_asciistr");
            if (rs.next()) {
                assertThat(rs.getString(1), equalTo("AB\\00C4CDE"));
            } else {
                fail("tbl_asciistr has no record.");
            }
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testBinToNum() {
        assertThat(OracleFunctions.binToNum(1, 0, 1, 0), equalTo(10L));
    }

    @Test
    public void testItBinToNum() throws Exception {
        final Statement stmt = con.createStatement();
        try {
            stmt.execute("CREATE ALIAS BIN_TO_NUM FOR \"org.guess880.h2_oracle_funcs.OracleFunctions.binToNum\"");
            ResultSet rs = stmt.executeQuery("SELECT BIN_TO_NUM(1, 0, 1, 0) FROM dual");
            if (rs.next()) {
                assertThat(rs.getLong(1), equalTo(10L));
            } else {
                fail("dual has no record.");
            }
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testCompose() {
        assertThat(OracleFunctions.compose("o" + '\u0308'), equalTo("ö"));
    }

    @Test
    public void testItCompose() throws Exception {
        final Statement stmt = con.createStatement();
        try {
            stmt.execute("CREATE ALIAS COMPOSE FOR \"org.guess880.h2_oracle_funcs.OracleFunctions.compose\"");
            ResultSet rs = stmt.executeQuery("SELECT COMPOSE( 'o' || STRINGDECODE('\u0308')) FROM dual");
            if (rs.next()) {
                assertThat(rs.getString(1), equalTo("ö"));
            } else {
                fail("dual has no record.");
            }
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testDbTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("IST"));
        assertThat(OracleFunctions.dbTimeZone(), equalTo("+05:30"));
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        assertThat(OracleFunctions.dbTimeZone(), equalTo("+00:00"));
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Honolulu"));
        assertThat(OracleFunctions.dbTimeZone(), equalTo("-10:00"));
    }

    @Test
    public void testItDbTimeZone() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("JST"));
        final Statement stmt = con.createStatement();
        try {
            stmt.execute("CREATE ALIAS DBTIMEZONE FOR \"org.guess880.h2_oracle_funcs.OracleFunctions.dbTimeZone\"");
            ResultSet rs = stmt.executeQuery("SELECT DBTIMEZONE() FROM dual");
            if (rs.next()) {
                assertThat(rs.getString(1), equalTo("+09:00"));
            } else {
                fail("dual has no record.");
            }
        } finally {
            stmt.close();
        }
    }

//    @Test
//    public void testDecompose() {
//        assertThat(OracleFunctions.decompose("Châteaux"), equalTo("Cha^teaux"));
//    }

//    @Test
//    public void testItDeompose() throws Exception {
//        final Statement stmt = con.createStatement();
//        try {
//            stmt.execute("CREATE ALIAS DECOMPOSE FOR \"org.guess880.h2_oracle_funcs.OracleFunctions.decompose\"");
//            ResultSet rs = stmt.executeQuery("SELECT DECOMPOSE('Châteaux') FROM dual");
//            if (rs.next()) {
//                assertThat(rs.getString(1), equalTo("Cha^teaux"));
//            } else {
//                fail("dual has no record.");
//            }
//        } finally {
//            stmt.close();
//        }
//    }

}
