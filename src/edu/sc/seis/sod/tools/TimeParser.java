package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.FlaggedOption;

public class TimeParser extends PatternParser {

    public TimeParser() {
        super("(\\d{4})-(\\d{1,2})-(\\d{1,2})", new String[] {"year",
                                                          "month",
                                                          "day"});
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
