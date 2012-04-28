package org.guess880.h2_oracle_funcs;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Date;
import java.util.TimeZone;

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
        final StringBuilder buffer = new StringBuilder();
        double value = (double) TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3600000;
//        double value = TimeZone.getDefault().getRawOffset() / 3600000; TODO which?
        double absval = value < 0 ? -value : value;
        String hour = Integer.toString((int) (absval * 10 / 10));
        String minute = Integer.toString((int) (absval % 1 * 60));
        buffer.append(value >= 0 ? "+" : "-");
        if (hour.length() == 1) {
            buffer.append("0");
        }
        buffer.append(hour);
        buffer.append(":");
        if (minute.length() == 1) {
            buffer.append("0");
        }
        buffer.append(minute);
        return buffer.toString();
    }

    // canonical only
//    public static final String decompose(final String exp) {
//        return Normalizer.normalize(exp, Normalizer.Form.NFD);
//    }

    public static final Date lastDay(final Date date) {
        return new LocalDateTime(date.getTime()).dayOfMonth().withMaximumValue().toDate();
    }

    // return type is ok?
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

}
