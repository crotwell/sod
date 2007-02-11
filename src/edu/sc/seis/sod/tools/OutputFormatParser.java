package edu.sc.seis.sod.tools;

import java.util.HashMap;
import java.util.Map;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;

public class OutputFormatParser extends StringParser {

    public OutputFormatParser(Map shortcutFormats) {
        shortcutFormats.put("none", Boolean.FALSE);
        this.shortcutFormats = shortcutFormats;
    }

    public static FlaggedOption createParam(String xyFormat, String yxFormat) {
        Map shortcuts = new HashMap();
        shortcuts.put("xy", xyFormat);
        shortcuts.put("yx", yxFormat);
        return createParam(shortcuts, "xy");
    }

    public static FlaggedOption createParam(Map shortcutFormats,
                                            String defaultFormat) {
        return new FlaggedOption("output",
                                 new OutputFormatParser(shortcutFormats),
                                 defaultFormat,
                                 true,
                                 'o',
                                 "output",
                                 "The format for output to standard out.");
    }

    public Object parse(String format) throws ParseException {
        if(shortcutFormats.containsKey(format)) {
            return shortcutFormats.get(format);
        }
        return format;
    }

    private Map shortcutFormats;
}
