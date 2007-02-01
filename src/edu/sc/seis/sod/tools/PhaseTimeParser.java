package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.FlaggedOption;


public class PhaseTimeParser extends PatternParser {

    public PhaseTimeParser() {
        super("\\+?" + BoxAreaParser.DECIMAL_NUMBER_RE + "([A-Za-z]+)", new String[] {"offset", "name"});
    }

    public static FlaggedOption createParam(String name,
                                            String defaultPhase,
                                            String helpMessage) {
        return new FlaggedOption(name,
                                 new PhaseTimeParser(),
                                 defaultPhase,
                                 false,
                                 name.toUpperCase().charAt(0),
                                 name,
                                 helpMessage);
    }


    public String getErrorMessage(String arg) {
        return "A phase time is specified as a time of offset in minutes and a phase name like 12ttp or -3s not '"
                + arg + "'";
    }
}
