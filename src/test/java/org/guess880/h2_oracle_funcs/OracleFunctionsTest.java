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
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
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
    @SuppressWarnings("deprecation")
    public void testAddMonths() {
        assertThat(OracleFunctions.addMonths(new Date(2000, 11, 1), 1), equalTo(new Date(2001, 0, 1)));
        assertThat(OracleFunctions.addMonths(new Date(2000, 0, 1), -1), equalTo(new Date(1999, 11, 1)));
    }

    @Test
    public void testItAddMonths() throws Exception {
        final Statement stmt = con.createStatement();
        try {
            stmt.execute("CREATE ALIAS ADD_MONTH FOR \"org.guess880.h2_oracle_funcs.OracleFunctions.addMonths\"");
            ResultSet rs = stmt.executeQuery(
                    "SELECT" +
                    " ADD_MONTH(PARSEDATETIME('20000101123456789', 'yyyyMMddHHmmssSSS'), 1)," +
                    " ADD_MONTH(PARSEDATETIME('20000101', 'yyyyMMdd'), 1)" +
                    " FROM dual");
            if (rs.next()) {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                Date expected = format.parse("20000201123456789");
                assertThat(rs.getTimestamp(1), equalTo(expected));
                format = new SimpleDateFormat("yyyyMMdd");
                expected = format.parse("20000201");
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
            ResultSet rs = stmt.executeQuery("CALL ASCIISTR('ABÄCDE')");
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
            ResultSet rs = stmt.executeQuery("CALL BIN_TO_NUM(1, 0, 1, 0)");
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
            ResultSet rs = stmt.executeQuery("CALL COMPOSE( 'o' || STRINGDECODE('\u0308'))");
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
        final TimeZone defTZ = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("IST"));
            assertThat(OracleFunctions.dbTimeZone(), equalTo("+05:30"));
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            assertThat(OracleFunctions.dbTimeZone(), equalTo("+00:00"));
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Honolulu"));
            assertThat(OracleFunctions.dbTimeZone(), equalTo("-10:00"));
        } finally {
            TimeZone.setDefault(defTZ);
        }
    }

    @Test
    public void testItDbTimeZone() throws Exception {
        final TimeZone defTZ = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("JST"));
            final Statement stmt = con.createStatement();
            try {
                stmt.execute("CREATE ALIAS DBTIMEZONE FOR \"org.guess880.h2_oracle_funcs.OracleFunctions.dbTimeZone\"");
                ResultSet rs = stmt.executeQuery("CALL DBTIMEZONE()");
                if (rs.next()) {
                    assertThat(rs.getString(1), equalTo("+09:00"));
                } else {
                    fail("dual has no record.");
                }
            } finally {
                stmt.close();
            }
        } finally {
            TimeZone.setDefault(defTZ);
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

    @Test
    public void testLastDay() {
        assertThat(
                OracleFunctions.lastDay(
                        new Date(new DateTime(2000, 1, 1, 0, 0).getMillis()))
                        .getTime(),
                equalTo(new DateTime(2000, 1, 31, 0, 0).getMillis()));
        assertThat(
                OracleFunctions.lastDay(
                        new Date(new DateTime(2000, 2, 11, 0, 0).getMillis()))
                        .getTime(),
                equalTo(new DateTime(2000, 2, 29, 0, 0).getMillis()));
        assertThat(
                OracleFunctions.lastDay(
                        new Date(new DateTime(2000, 4, 21, 0, 0).getMillis()))
                        .getTime(),
                equalTo(new DateTime(2000, 4, 30, 0, 0).getMillis()));
    }

    @Test
    public void testItLastDay() throws Exception {
        final Statement stmt = con.createStatement();
        try {
            stmt.execute("CREATE ALIAS LAST_DAY FOR \"org.guess880.h2_oracle_funcs.OracleFunctions.lastDay\"");
            ResultSet rs = stmt.executeQuery(
                    "SELECT LAST_DAY(PARSEDATETIME('20000101', 'yyyyMMdd'))," +
                    " LAST_DAY(PARSEDATETIME('20000211', 'yyyyMMdd'))," +
                    " LAST_DAY(PARSEDATETIME('20000421', 'yyyyMMdd')) FROM dual");
            if (rs.next()) {
                assertThat(rs.getDate(1).getTime(), equalTo(new DateTime(2000,
                        1, 31, 0, 0).getMillis()));
                assertThat(rs.getDate(2).getTime(), equalTo(new DateTime(2000,
                        2, 29, 0, 0).getMillis()));
                assertThat(rs.getDate(3).getTime(), equalTo(new DateTime(2000,
                        4, 30, 0, 0).getMillis()));
            } else {
                fail("dual has no record.");
            }
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testMonthsBetween() {
        assertThat(OracleFunctions.monthsBetween(
                new Date(new DateTime(1995, 2, 2, 0, 0).getMillis()),
                new Date(new DateTime(1995, 1, 1, 0, 0).getMillis())),
                equalTo(1.03225806));
        assertThat(OracleFunctions.monthsBetween(
                new Date(new DateTime(1995, 2, 28, 0, 0).getMillis()),
                new Date(new DateTime(1995, 1, 31, 0, 0).getMillis())),
                equalTo(1.0));
        assertThat(OracleFunctions.monthsBetween(
                new Date(new DateTime(1994, 12, 31, 0, 0).getMillis()),
                new Date(new DateTime(1995, 1, 31, 0, 0).getMillis())),
                equalTo(-1.0));
    }

    @Test
    public void testItMonthsBetween() throws Exception {
        final Statement stmt = con.createStatement();
        try {
            stmt.execute("CREATE ALIAS MONTH_BETWEEN FOR \"org.guess880.h2_oracle_funcs.OracleFunctions.monthsBetween\"");
            ResultSet rs = stmt.executeQuery(
                    "SELECT" +
                    " MONTH_BETWEEN(PARSEDATETIME('19950202', 'yyyyMMdd'), PARSEDATETIME('19950101', 'yyyyMMdd'))," +
                    " MONTH_BETWEEN(PARSEDATETIME('19950228', 'yyyyMMdd'), PARSEDATETIME('19950131', 'yyyyMMdd'))," +
                    " MONTH_BETWEEN(PARSEDATETIME('19941231', 'yyyyMMdd'), PARSEDATETIME('19950131', 'yyyyMMdd'))" +
                    " FROM dual");
            if (rs.next()) {
                assertThat(rs.getDouble(1), equalTo(1.03225806));
                assertThat(rs.getDouble(2), equalTo(1.0));
                assertThat(rs.getDouble(3), equalTo(-1.0));
            } else {
                fail("dual has no record.");
            }
        } finally {
            stmt.close();
        }
    }

//    @Test
//    public void testNewTime() {
//        System.out.println(OracleFunctions.newTime(
//                new DateTime(2009, 11, 10, 1, 23, 45, DateTimeZone.forID("America/Halifax")).toDate(),
//                "America/Los_Angeles", "America/Halifax"));
//    }

    @Test
    public void testNextDay() {
        Locale def = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            assertThat(OracleFunctions.nextDay(new LocalDateTime(2009, 10, 15, 0,
                    0, 0).toDate(), "TUESDAY"), equalTo(new LocalDateTime(2009, 10, 20,
                    0, 0, 0).toDate()));
            Locale.setDefault(Locale.JAPANESE);
            assertThat(OracleFunctions.nextDay(new LocalDateTime(2009, 10, 15, 0,
                    0, 0).toDate(), "火"), equalTo(new LocalDateTime(2009, 10, 20,
                    0, 0, 0).toDate()));
        } finally {
            Locale.setDefault(def);
        }
    }

}
