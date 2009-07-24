package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;

public class ResponseFormatParser extends StringParser {

    public Object parse(String arg) throws ParseException {
        if(arg.equals("polezero") || arg.equals("resp")) {
            return arg;
        }
        throw new ParseException("The repsonse format can be either polezero or resp, not '"
                + arg + "'");
    }
}
