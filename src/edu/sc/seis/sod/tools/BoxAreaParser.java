package edu.sc.seis.sod.tools;

public class BoxAreaParser extends PatternParser {

    public BoxAreaParser() {
        super("(-?\\d+)/(-?\\d+)/(-?\\d+)/(-?\\d+)", new String[] {"west",
                                                                   "east",
                                                                   "south",
                                                                   "north"});
    }

    public String getErrorMessage(String arg) {
        return "The argument should be the boxes edges specified as west/east/south/north not '"
                + arg + "'";
    }
}