package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.FlaggedOption;

public class BoxAreaParser extends PatternParser {

    public BoxAreaParser() {
        super("(-?\\d+\\.?\\d*)/(-?\\d+\\.?\\d*)/(-?\\d+\\.?\\d*)/(-?\\d+\\.?\\d*)",
              new String[] {"west", "east", "south", "north"});
    }

    public static FlaggedOption createParam(String helpMessage) {
        return new FlaggedOption("box",
                                 new BoxAreaParser(),
                                 null,
                                 false,
                                 'R',
                                 "box-area",
                                 helpMessage);
    }

    public String getErrorMessage(String arg) {
        return "A box area is specified as its edges separated by slashes, west/east/south/north, not '"
                + arg + "'";
    }
}