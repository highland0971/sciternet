package controllers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Created by vivia on 2016/4/17.
 */

public class TimeUtil {

    static LocalDate past = LocalDate.of(1,1,1);

    public static long toOrdinal(LocalDate target){
        /**
         * Return the proleptic Gregorian ordinal of the date, where January 1 of year 1 has ordinal 1.
         * Like Python date.toordinal().
         */
        return past.until(target, ChronoUnit.DAYS);
    }

    public static LocalDate fromOrdinal(long date)
    {
        return past.plusDays(date);
    }
}
