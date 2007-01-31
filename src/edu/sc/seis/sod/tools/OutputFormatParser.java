package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;

public class OutputFormatParser extends StringParser {

    public OutputFormatParser(String xyFormat, String yxFormat) {
        this.xyFormat = xyFormat;
        this.yxFormat = yxFormat;
    }

    public static FlaggedOption createParam(String xyFormat, String yxFormat) {
        return new FlaggedOption("output",
                                 new OutputFormatParser(xyFormat, yxFormat),
                                 "xy",
                                 true,
                                 'o',
                                 "output",
                                 "The format for output to standard out.");
    }

    public Object parse(String format) throws ParseException {
        if(format.equals("none")) {
            return Boolean.FALSE;
        }
        if(format.equals("xy")) {
            return xyFormat;
        }else if(format.equals("yx")){
            return yxFormat;
        }
        return format;
    }

    private String xyFormat, yxFormat;
}
