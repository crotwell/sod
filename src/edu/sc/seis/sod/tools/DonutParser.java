package edu.sc.seis.sod.tools;


public class DonutParser extends PatternParser {

    public DonutParser() {
        super("(-?\\d+)/(-?\\d+)/(\\d+)/(\\d+)", new String[] {"lat",
                                                               "lon",
                                                               "min",
                                                               "max"});
    }

    public String getErrorMessage(String arg) {
        return "The argument should be the donut specified as centerLat/centerLon/minRadiusDegrees/maxRadiusDegrees not '"
                + arg + "'";
    }
}
