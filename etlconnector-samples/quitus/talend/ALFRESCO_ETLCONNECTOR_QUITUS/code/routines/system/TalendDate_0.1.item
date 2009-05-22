package routines;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import routines.system.FastDateParser;
import routines.system.LocaleProvider;

public class TalendDate {

    /**
     * Formats a Date into a date/time string.
     * 
     * @param pattern the pattern to format.
     * @param date the time value to be formatted into a time string.
     * @return the formatted time string.
     * 
     * {talendTypes} String
     * 
     * {Category} TalendDate
     * 
     * {param} string("yyyy-MM-dd HH:mm:ss") pattern : the pattern to format
     * 
     * {param} date(myDate) date : the time value to be formatted into a time string
     * 
     * {example} formatDate("yyyy-MM-dd", new Date()) #
     */
    public synchronized static String formatDate(String pattern, java.util.Date date) {
        return FastDateParser.getInstance(pattern).format(date);
    }

    /**
     * Formats a Date into a date/time string using the given pattern and the default date format symbols for the given
     * locale.
     * 
     * @param pattern the pattern to format.
     * @param date the time value to be formatted into a time string.
     * @param locale the locale whose date format symbols should be used.
     * @return the formatted time string.
     * 
     * {talendTypes} String
     * 
     * {Category} TalendDate
     * 
     * {param} string("yyyy-MM-dd HH:mm:ss") pattern : the pattern to format
     * 
     * {param} date(myDate) date : the time value to be formatted into a time string
     * 
     * {param} string("EN") languageOrCountyCode : the language or country whose date format symbols should be used, in
     * lower or upper case
     * 
     * {example} formatDateLocale("yyyy-MM-dd", new Date(), "en") #
     */
    public synchronized static String formatDateLocale(String pattern, java.util.Date date, String languageOrCountyCode) {
        return FastDateParser.getInstance(pattern, LocaleProvider.getLocale(languageOrCountyCode)).format(date);
    }

    /**
     * Parses text from the beginning of the given string to produce a date using the given pattern and the default date
     * format symbols for the given locale. The method may not use the entire text of the given string.
     * <p>
     * 
     * @param pattern the pattern to parse.
     * @param stringDate A <code>String</code> whose beginning should be parsed.
     * @return A <code>Date</code> parsed from the string.
     * @throws ParseException
     * @exception ParseException if the beginning of the specified string cannot be parsed.
     * 
     * {talendTypes} Date
     * 
     * {Category} TalendDate
     * 
     * {param} string("yyyy-MM-dd HH:mm:ss") pattern : the pattern to parse
     * 
     * {param} string("") stringDate : A <code>String</code> whose beginning should be parsed
     * 
     * {example} parseDate("yyyy-MMM-dd HH:mm:ss", "23-Mar-1979 23:59:59") #
     */
    public synchronized static Date parseDate(String pattern, String stringDate) {
        try {
            return FastDateParser.getInstance(pattern).parse(stringDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses text from the beginning of the given string to produce a date. The method may not use the entire text of
     * the given string.
     * <p>
     * 
     * @param pattern the pattern to parse.
     * @param stringDate A <code>String</code> whose beginning should be parsed.
     * @param locale the locale whose date format symbols should be used.
     * @return A <code>Date</code> parsed from the string.
     * @throws ParseException
     * @exception ParseException if the beginning of the specified string cannot be parsed.
     * 
     * {talendTypes} Date
     * 
     * {Category} TalendDate
     * 
     * {param} string("yyyy-MM-dd HH:mm:ss") pattern : the pattern to parse
     * 
     * {param} string("") stringDate : A <code>String</code> whose beginning should be parsed
     * 
     * {param} string("EN") languageOrCountyCode : the language or country whose date format symbols should be used, in
     * lower or upper case
     * 
     * {example} parseDateLocale("yyyy-MMM-dd", "23-Mar-1979", "en") #
     */
    public synchronized static Date parseDateLocale(String pattern, String stringDate, String languageOrCountyCode) {
        try {
            return FastDateParser.getInstance(pattern, LocaleProvider.getLocale(languageOrCountyCode))
                    .parse(stringDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * getDate : return the current datetime with the given display format format : (optional) string representing the
     * wished format of the date. This string contains fixed strings and variables related to the date. By default, the
     * format string is DD/MM/CCYY. Here is the list of date variables:
     * 
     * 
     * {talendTypes} String
     * 
     * {Category} TalendDate
     * 
     * {param} string("CCYY-MM-DD hh:mm:ss") pattern : date pattern + CC for century + YY for year + MM for month + DD
     * for day + hh for hour + mm for minute + ss for second
     * 
     * {example} getDate("CCYY-MM-DD hh:mm:ss") #
     */
    public static String getDate(String pattern) {
        StringBuffer result = new StringBuffer();

        pattern = pattern.replace("CC", "yy");
        pattern = pattern.replace("YY", "yy");
        pattern = pattern.replace("MM", "MM");
        pattern = pattern.replace("DD", "dd");
        pattern = pattern.replace("hh", "HH");

        // not needed
        // pattern.replace("mm", "mm");
        // pattern.replace("ss", "ss");

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.format(Calendar.getInstance().getTime(), result, new FieldPosition(0));
        return result.toString();
    }

    /**
     * getDate : return the current date
     * 
     * 
     * 
     * {talendTypes} Date
     * 
     * {Category} TalendDate
     * 
     * {example} getCurrentDate()
     */
    public static Date getCurrentDate() {
        return Calendar.getInstance().getTime();
    }

    /**
     * return an ISO formatted random date
     * 
     * 
     * {talendTypes} Date
     * 
     * {Category} TalendDate
     * 
     * {param} string("2007-01-01") min : minimum date
     * 
     * {param} string("2008-12-31") max : maximum date (superior to min)
     * 
     * {example} getRandomDate("1981-01-18", "2005-07-24") {example} getRandomDate("1980-12-08", "2007-02-26")
     */
    public static Date getRandomDate(String minDate, String maxDate) {
        int minYear = Integer.parseInt(minDate.substring(0, 4));
        int minMonth = Integer.parseInt(minDate.substring(5, 7));
        int minDay = Integer.parseInt(minDate.substring(8, 10));

        int maxYear = Integer.parseInt(maxDate.substring(0, 4));
        int maxMonth = Integer.parseInt(maxDate.substring(5, 7));
        int maxDay = Integer.parseInt(maxDate.substring(8, 10));

        Calendar minCal = Calendar.getInstance();
        minCal.set(Calendar.YEAR, minYear);
        minCal.set(Calendar.MONTH, minMonth - 1);
        minCal.set(Calendar.DAY_OF_MONTH, minDay);

        Calendar maxCal = Calendar.getInstance();
        maxCal.set(Calendar.YEAR, maxYear);
        maxCal.set(Calendar.MONTH, maxMonth - 1);
        maxCal.set(Calendar.DAY_OF_MONTH, maxDay);

        long random = minCal.getTimeInMillis()
                + (long) ((maxCal.getTimeInMillis() - minCal.getTimeInMillis() + 1) * Math.random());
        return new Date(random);
    }

    /**
     * 
     * Method used for tests only.
     * 
     * @param args
     */
    public static void main(String[] args) {
        final int LOOPS = 100000;
        final String dateTimeRef_Test1 = "1979-03-23 mars 12:30";
        Thread test1 = new Thread() {

            @Override
            public void run() {
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.set(1979, 2, 23, 12, 30, 40);
                Date dateCalendar = calendar.getTime();
                for (int i = 0; i < LOOPS; i++) {
                    String date = TalendDate.formatDate("yyyy-MM-dd MMM HH:mm", dateCalendar);
                    // System.out.println("Test1:" + date + " # " + dateTimeRef_Test1);
                    if (!dateTimeRef_Test1.equals(date)) {
                        throw new IllegalStateException("Test1: Date ref : '" + dateTimeRef_Test1
                                + "' is different of '" + date + "'");
                    }
                }
                System.out.println("test1 ok");
            }
        };
        final String dateTimeRef_Test2 = "1980-03-23 mars 12:30";
        Thread test2 = new Thread() {

            @Override
            public void run() {
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.set(1980, 2, 23, 12, 30, 40);
                Date dateCalendar = calendar.getTime();
                for (int i = 0; i < LOOPS; i++) {
                    String date = TalendDate.formatDate("yyyy-MM-dd MMM HH:mm", dateCalendar);
                    // System.out.println("Test2:" + date + " # " + dateTimeRef_Test2);
                    if (!dateTimeRef_Test2.equals(date)) {
                        throw new IllegalStateException("Test2: Date ref : '" + dateTimeRef_Test2
                                + "' is different of '" + date + "'");
                    }
                }
                System.out.println("test2 ok");
            }
        };

        final String dateTimeRef_Test3 = "1979-03-23 mars 12:30";
        Thread test3 = new Thread() {

            @Override
            public void run() {
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.set(1979, 2, 23, 12, 30, 40);
                Date dateCalendar = calendar.getTime();
                for (int i = 0; i < LOOPS; i++) {
                    String date = TalendDate.formatDateLocale("yyyy-MM-dd MMM HH:mm", dateCalendar, "FR");
                    // System.out.println("Test3:" + date + " # " + dateTimeRef_Test3);
                    if (!dateTimeRef_Test3.equals(date)) {
                        throw new IllegalStateException("Test3: Date ref : '" + dateTimeRef_Test3
                                + "' is different of '" + date + "'");
                    }
                }
                System.out.println("test3 ok");
            }
        };
        final String dateTimeRef_Test4 = "1980-03-23 Mar 12:30";
        Thread test4 = new Thread() {

            @Override
            public void run() {
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.set(1980, 2, 23, 12, 30, 40);
                Date dateCalendar = calendar.getTime();
                for (int i = 0; i < LOOPS; i++) {
                    String date = TalendDate.formatDateLocale("yyyy-MM-dd MMM HH:mm", dateCalendar, "EN");
                    // System.out.println("Test4:" + date + " # " + dateTimeRef_Test4);
                    if (!dateTimeRef_Test4.equals(date)) {
                        throw new IllegalStateException("Test4: Date ref : '" + dateTimeRef_Test4
                                + "' is different of '" + date + "'");
                    }
                }
                System.out.println("test4 ok");
            }
        };

        final String dateTimeRef_Test5 = "1979-03-23";
        Thread test5 = new Thread() {

            @Override
            public void run() {
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.set(1979, 2, 23, 12, 30, 40);
                Date dateCalendar = calendar.getTime();
                for (int i = 0; i < LOOPS; i++) {
                    String date = TalendDate.formatDate("yyyy-MM-dd", dateCalendar);
                    // System.out.println("Test5:" + date + " # " + dateTimeRef_Test5);
                    if (!dateTimeRef_Test5.equals(date)) {
                        throw new IllegalStateException("Test5: Date ref : '" + dateTimeRef_Test5
                                + "' is different of '" + date + "'");
                    }

                }
                System.out.println("test5 ok");
            }
        };

        test1.start();
        test2.start();
        test3.start();
        test4.start();
        test5.start();
    }
}
