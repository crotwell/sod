package edu.sc.seis.sod.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;

import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.ISOTime;
import edu.sc.seis.sod.util.time.ClockUtil;

public class TimeParser extends StringParser {
    
    /**
     * @param ceiling should unspecified fields be floored or ceilinged.
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
        // look for relative time, like -1d or -3m
        Matcher m = relativeTimePattern.matcher(arg);
        if (m.matches()) {
            String s = "<earlier><value>"+m.group(1)+"</value>";
            if ("h".equals(m.group(2))) {
                s += "<unit>HOUR</unit>";
            } else if ("d".equals(m.group(2))) {
                s += "<unit>DAY</unit>";
            } else if ("m".equals(m.group(2))) {
                s += "<unit>MONTH</unit>";
            } else if ("y".equals(m.group(2))) {
                s += "<unit>YEAR</unit>";
            } else {
                throw new ParseException("I don't understand "+arg+", should be like -2h, -1d or -3m or -1y");
            }
            return s+"</earlier>";
        }
        return parseDate(arg);
    }
    
    public String parseDate(String arg) throws ParseException {
        return format(getMicroSecondDate(arg));
    }
    
    public static String format(Instant d) {
        DateFormat passcalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        passcalFormat.setTimeZone(ISOTime.UTC);
        return passcalFormat.format(d);
    }
    
    public static String formatForParsing(Instant d) {
        DateFormat passcalFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        passcalFormat.setTimeZone(ISOTime.UTC);
        return passcalFormat.format(d);
    }
    
    public Instant getMicroSecondDate(String arg) throws ParseException {
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
        return cal.getTime();
    }

    private int extract(Matcher m, int i) {
        if(m.group(i) == null) {
            return -1;
        }
        return Integer.parseInt(m.group(i));
    }

    private Pattern datePattern = Pattern.compile("(\\-?\\d{4})-?(\\d{2})?-?(\\d{2})?-?(\\d{2})?-?(\\d{2})?-?(\\d{2})?");

    private Pattern relativeTimePattern = Pattern.compile("-(\\d+)([hdmy])");
    
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

    public static final String FIRST_SEISMOGRAM = "1889-04-17";

    private static final String PREVIOUS_DAY = new SimpleDateFormat("yyyy-MM-dd").format(ClockUtil.now()
            .minus(ClockUtil.ONE_DAY));

    private static String PREVIOUS_DAY_BEGIN = "the previous day, "
            + PREVIOUS_DAY;
}
