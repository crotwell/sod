package edu.sc.seis.sod.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.SodUtil;

public class TimeParser extends StringParser {
    
    /**
     * @param  - should unspecified fields be floored or ceilinged.
     */
    public TimeParser(boolean ceiling){
        this.ceiling = ceiling;
    }

    public Object parse(String arg) throws ParseException {
        if(arg.equals("now")) {
            return "<now/>";
        } else if(arg.equals(PREVIOUS_DAY_BEGIN)) {
            arg = PREVIOUS_DAY;
        }else if(arg.equals("network")){
            if(ceiling){
                return "<networkEndTime/>";
            }else{
                return "<networkStartTime/>";
            }
        }
        return parseDate(arg);
    }
    
    public String parseDate(String arg) throws ParseException {
        return format(getMicroSecondDate(arg));
    }
    
    public static String format(MicroSecondDate d) {
        DateFormat passcalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        passcalFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return passcalFormat.format(d);
    }
    
    public static String formatForParsing(MicroSecondDate d) {
        DateFormat passcalFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        passcalFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return passcalFormat.format(d);
    }
    
    public MicroSecondDate getMicroSecondDate(String arg) throws ParseException {
        if (arg.equals("now")) {
            return ClockUtil.now();
        }
        Matcher m = datePattern.matcher(arg);
        if(!m.matches()) {
            throw new ParseException("A time must be formatted as YYYY[[[[[-MM]-DD]-hh]-mm]-ss] like 2006-11-19, not '"
                    + arg + "'");
        }
        Calendar cal = SodUtil.createCalendar(Integer.parseInt(m.group(1)),
                                              extract(m, 2),
                                              extract(m, 3),
                                              extract(m, 4),
                                              extract(m, 5),
                                              extract(m, 6),
                                              ceiling);
        return new MicroSecondDate(cal.getTime());
    }

    private int extract(Matcher m, int i) {
        if(m.group(i) == null) {
            return -1;
        }
        return Integer.parseInt(m.group(i));
    }

    private Pattern datePattern = Pattern.compile("(\\-?\\d{4})-?(\\d{2})?-?(\\d{2})?-?(\\d{2})?-?(\\d{2})?-?(\\d{2})?");

    public static FlaggedOption createYesterdayParam(String name,
                                                     String helpMessage,
                                                     boolean ceiling) {
        return createParam(name, PREVIOUS_DAY_BEGIN, helpMessage, ceiling);
    }

    public static FlaggedOption createParam(String name,
                                            String defaultTime,
                                            String helpMessage,
                                            boolean ceiling) {
        return new FlaggedOption(name,
                                 new TimeParser(ceiling),
                                 defaultTime,
                                 false,
                                 name.charAt(0),
                                 name,
                                 helpMessage);
    }

    private boolean ceiling;

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public static final String FIRST_SEISMOGRAM = "1889-04-17";

    private static final String PREVIOUS_DAY = df.format(ClockUtil.now()
            .subtract(new TimeInterval(1, UnitImpl.DAY)));

    private static String PREVIOUS_DAY_BEGIN = "the previous day, "
            + PREVIOUS_DAY;
}
