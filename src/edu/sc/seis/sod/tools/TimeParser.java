package edu.sc.seis.sod.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.ParseException;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

public class TimeParser extends PatternParser {

    public TimeParser() {
        super("(\\d{4})-(\\d{1,2})-(\\d{1,2})", new String[] {"year",
                                                              "month",
                                                              "day"});
    }

    public Object parse(String arg) throws ParseException {
        if(arg.equals("now")) {
            Map result = new HashMap();
            result.put("now", Boolean.TRUE);
            return result;
        } else if(arg.equals(PREVIOUS_DAY_BEGIN)) {
            arg = PREVIOUS_DAY;
        }
        return super.parse(arg);
    }

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

    public String getErrorMessage(String arg) {
        return "A time must be formatted as YYYY-MM-DD like 2006-11-19, not '"
                + arg + "'";
    }

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    private static final String PREVIOUS_DAY = df.format(ClockUtil.now()
            .subtract(new TimeInterval(1, UnitImpl.DAY)));

    private static String PREVIOUS_DAY_BEGIN = "the previous day, "
            + PREVIOUS_DAY;
}
