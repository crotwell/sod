package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;

public class OutputFormatParser extends StringParser {

    public OutputFormatParser(String xyFormat) {
        this.xyFormat = xyFormat;
    }

    public static FlaggedOption createParam(String xyFormat, String templateURL) {
        return new FlaggedOption("output",
                                 new OutputFormatParser(xyFormat),
                                 "xy",
                                 true,
                                 'o',
                                 "output",
                                 "The format for output.  Can be none, xy or a Velocity template as described on "
                                         + templateURL);
    }

    public Object parse(String format) throws ParseException {
        if(format.equals("none")) {
            return Boolean.FALSE;
        }
        if(format.equals("xy")) {
            return xyFormat;
        }
        return format;
    }

    private String xyFormat;
}
