package org.guess880.h2_oracle_funcs;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalDateTime.Property;
import org.joda.time.Period;
import org.joda.time.PeriodType;

public class OracleFunctions {

    // TODO if date is null, what to do.
    // TODO support implicit type conversion of date.
    public static final Date addMonths(final Date date, final int integer) {
        return new LocalDateTime(date.getTime()).plusMonths(integer).toDate();
    }

    public static final String asciistr(final String str) {
        final StringBuilder sb = new StringBuilder();
        final char[] uu = new char[5];
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c > '\u007f') {
                uu[0] = '\\';
                uu[1] = toHex((c >> 12) & 0xf);
                uu[2] = toHex((c >> 8) & 0xf);
                uu[3] = toHex((c >> 4) & 0xf);
                uu[4] = toHex(c & 0xf);
                sb.append(uu);
            } else {
                sb.append(str.charAt(i));
            }
        }
        return sb.toString();
    }

    private static char toHex(final int nibble) {
        return HEXDIGIT[(nibble & 0xF)];
    }

    private static final char[] HEXDIGIT = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
            'D', 'E', 'F'
    };

    // TODO return type is ok?
    // TODO if expression is larger than 1
    public static final long binToNum(final int... exprs) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < exprs.length; i++) {
            sb.append(String.valueOf(exprs[i]));
        }
        return Long.valueOf(sb.toString(), 2).longValue();
    }

    public static final String compose(final String exp) {
        return Normalizer.normalize(exp, Normalizer.Form.NFC);
    }

    // TODO omit the parentheses
    public static final String dbTimeZone() {
        return new DateTime(DateTimeZone.forTimeZone(TimeZone.getDefault()))
                .toString("ZZ");
    }

    // canonical only
//    public static final String decompose(final String exp) {
//        return Normalizer.normalize(exp, Normalizer.Form.NFD);
//    }

    // TODO support implicit type conversion of date.
    public static final Date lastDay(final Date date) {
        return new LocalDateTime(date.getTime()).dayOfMonth().withMaximumValue().toDate();
    }

    // TODO return type is ok?
    // TODO support implicit type conversion of date.
    public static final double monthsBetween(final Date date1, final Date date2) {
        final Period period = new Period(
                new LocalDateTime(date2.getTime()),
                new LocalDateTime(date1.getTime()),
                PeriodType.yearMonthDay().withYearsRemoved());
        return  new BigDecimal(period.getMonths() + ((double) period.getDays() / 31)).
                setScale(8, BigDecimal.ROUND_FLOOR). // TODO ceil or floor or round ?
                doubleValue();
    }

//    public static final Date newTime(final Date date, final String timeZone1,
//            final String timeZone2) {
//        return new DateTime(new DateTime(date.getTime(),
//                DateTimeZone.forID(timeZone2)).getMillis(),
//                DateTimeZone.forID(timeZone1)).toDate();
//    }

    // TODO if day is illegal, what to do
    // TODO oracle day
    // TODO support implicit type conversion of date.
    public static final Date nextDay(final Date date, final String day) {
        LocalDateTime next = null;
        final LocalDateTime ldt = new LocalDateTime(date.getTime());
        Property dayOfWeek;
        for (int i = 1; i <= 7; i++) {
            next = ldt.plusDays(i);
            dayOfWeek = next.dayOfWeek();
            if (dayOfWeek.getAsShortText().equalsIgnoreCase(day)
                    || dayOfWeek.getAsText().equalsIgnoreCase(day)) {
                break;
            }
        }
        return next.toDate();
    }

    // remainder

    public static final String sessionTimeZone() {
        return dbTimeZone();
    }

    public static final String tranlate(final String expr, final String from, final String to) {
        String ret = expr.replace("''", "'");
        String nfrom = from.replace("''", "'");
        char cf;
        for (int i = 0; i < nfrom.length(); i++) {
            cf = nfrom.charAt(i);
            if (i < to.length()) {
                ret = ret.replace(cf, to.charAt(i));
            } else {
                ret = ret.replace(String.valueOf(cf), "");
            }
        }
        return ret;
    }

//    public static final double trunc(final double n1, final int n2) {
//        double f = Math.pow(10., n2);
//        double g = n1 * f;
//        return (n1 < 0) ? Math.ceil(g) : Math.floor(g) / f;
//    }
//
//    public static final double trunc(final double n1) {
//        return trunc(n1, 0);
//    }
//
//    private static final Date trunc(final Date date, final String fmt) {
//        final String ufmt = fmt.toUpperCase(Locale.ENGLISH);
//        if ("CC".equals(ufmt) || "SCC".equals(ufmt)) {
//            // 4桁の年号の上2桁より1大きい数
//        } else if ("SYYYY".equals(ufmt)
//                || "YYYY".equals(ufmt)
//                || "YEAR".equals(ufmt)
//                || "SYEAR".equals(ufmt)
//                || "YYY".equals(ufmt)
//                || "YY".equals(ufmt)
//                || "Y".equals(ufmt)) {
//            // 年(7月1日に切上げ)
//        } else if ("IYYY".equals(ufmt)
//                || "IY".equals(ufmt)
//                || "IY".equals(ufmt)
//                || "I".equals(ufmt)) {
//            // ISO年
//        } else if ("Q".equals(ufmt)) {
//            // 四半期(その四半期の2番目の月の16日に切上げ)
//        } else if ("MONTH".equals(ufmt)
//                || "MON".equals(ufmt)
//                || "MM".equals(ufmt)
//                || "RM".equals(ufmt)) {
//            // 月(16日に切上げ)
//        } else if ("WW".equals(ufmt)) {
//            // 年の最初の日と同じ曜日
//        } else if ("IW".equals(ufmt)) {
//            // ISO週の最初の日と同じ曜日(月曜日)
//        } else if ("W".equals(ufmt)) {
//            // 月の最初の日と同じ曜日
//        } else if ("DDD".equals(ufmt)
//                || "DD".equals(ufmt)
//                || "J".equals(ufmt)) {
//            // 日
//        } else if ("DAY".equals(ufmt)
//                || "DY".equals(ufmt)
//                || "D".equals(ufmt)) {
//            // 週の開始日
//        } else if ("HH".equals(ufmt)
//                || "HH12".equals(ufmt)
//                || "HH24".equals(ufmt)) {
//            // 時
//        } else if ("MI".equals(ufmt)) {
//            // 分
//        } else {
//        }
//        return null;
//    }
//
//    public static final Date trunc(final Date date) {
//        return trunc(date, "DD");
//    }

    public static final String unistr(final String string) {
        final StringBuilder sb = new StringBuilder();
        char c;
        char[] cs = string.toCharArray();
        int i = 0;
        while (i < cs.length) {
            c = cs[i];
            if (c == '\\') {
                sb.append(hexToStr(Arrays.copyOfRange(cs, i + 1, i + 5)));
                i = i + 5;
            } else {
                sb.append(c);
                i = i + 1;
            }
        }
        return sb.toString();
    }

    private static final String hexToStr(char[] in) {
        char c;
        int value = 0;
        for (int i = 0; i < 4; i++) {
            c = in[i];
            switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                value = (value << 4) + c - '0';
                break;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                value = (value << 4) + 10 + c - 'a';
                break;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                value = (value << 4) + 10 + c - 'A';
                break;
            default:
                throw new IllegalArgumentException(
                        "Malformed \\uxxxx encoding.");
            }
        }
        return String.valueOf((char) value);
    }
}
