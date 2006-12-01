package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.FlaggedOption;

public class RangeParser extends PatternParser {

    public RangeParser() {
        super("(\\d+\\.?\\d*)-(\\d+\\.?\\d*)", new String[] {"min", "max"});
    }

    public static FlaggedOption createParam(String name,
                                            String defaultRange,
                                            String helpMessage) {
        return createParam(name, defaultRange, helpMessage, name.charAt(0));
    }

    public static FlaggedOption createParam(String name,
                                            String defaultRange,
                                            String helpMessage,
                                            char shortFlag) {
        return new FlaggedOption(name,
                                 new RangeParser(),
                                 defaultRange,
                                 false,
                                 shortFlag,
                                 name,
                                 helpMessage);
    }

    public String getErrorMessage(String arg) {
        return "A range is formatted like 2.0-7.1, not '" + arg + "'";
    }
}
