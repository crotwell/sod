package edu.sc.seis.sod.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

public class TimeParser extends StringParser {

    public Object parse(String arg) throws ParseException {
        if(arg.equals("now")) {
            return "<now/>";
        } else if(arg.equals(PREVIOUS_DAY_BEGIN)) {
            arg = PREVIOUS_DAY;
        }
        Matcher m = datePattern.matcher(arg);
        if(!m.matches()) {
            throw new ParseException("A time must be formatted as YYYY[[[[[-MM]-DD]-hh]-mm]-ss] like 2006-11-19, not '"
                    + arg + "'");
        }
        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.clear();
        for(int i = 0; i < calendarFieldForGroup.length; i++) {
            if(m.group(i + 1) == null) {
                cal.set(calendarFieldForGroup[i], defaultForGroup[i]);
            } else {
                int val = Integer.parseInt(m.group(i + 1));
                // Subtract one from the value in the string because the
                // bloody Calendar API counts months from 0 unlike civilized
                // folk.
                if(calendarFieldForGroup[i] == Calendar.MONTH) {
                    val -= 1;
                }
                cal.set(calendarFieldForGroup[i], val);
            }
        }
        DateFormat passcalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'");
        passcalFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return passcalFormat.format(cal.getTime());
    }

    private Pattern datePattern = Pattern.compile("(\\d{4})-?(\\d{2})?-?(\\d{2})?-?(\\d{2})?-?(\\d{2})?-?(\\d{2})?");

    private int[] defaultForGroup = new int[] {-1, 0, 1, 0, 0, 0};

    private int[] calendarFieldForGroup = new int[] {Calendar.YEAR,
                                                     Calendar.MONTH,
                                                     Calendar.DAY_OF_MONTH,
                                                     Calendar.HOUR_OF_DAY,
                                                     Calendar.MINUTE,
                                                     Calendar.SECOND};

    public static FlaggedOption createYesterdayParam(String name,
                                                     String helpMessage) {
        return createParam(name, PREVIOUS_DAY_BEGIN, helpMessage);
    }

    public static FlaggedOption createParam(String name,
                                            String defaultTime,
                                            String helpMessage) {
        return new FlaggedOption(name,
                                 new TimeParser(),
                                 defaultTime,
                                 false,
                                 name.charAt(0),
                                 name,
                                 helpMessage);
    }

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    private static final String PREVIOUS_DAY = df.format(ClockUtil.now()
            .subtract(new TimeInterval(1, UnitImpl.DAY)));

    private static String PREVIOUS_DAY_BEGIN = "the previous day, "
            + PREVIOUS_DAY;
}
