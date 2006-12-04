package edu.sc.seis.sod.tools;

import java.util.HashMap;
import java.util.Map;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.ParseException;

public class TimeParser extends PatternParser {

    public TimeParser() {
        super("(\\d{4})-(\\d{1,2})-(\\d{1,2})", new String[] {"year",
                                                          "month",
                                                          "day"});
    }

    public Object parse(String arg) throws ParseException {
        if(arg.equals("now")){
            Map result = new HashMap();
            result.put("now", Boolean.TRUE);
            return result;
        }
        return super.parse(arg);
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
}
