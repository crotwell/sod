package edu.sc.seis.sod.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;

public class SetSACParser extends StringParser {
    
    public SetSACParser(){
        for(int i = 0; i < 10; i++) {
            availableHeaders.add("" + i);
        }
        availableHeaders.add("a");
    }

    public Object parse(String arg) throws ParseException {
        Matcher m = re.matcher(arg);
        if(!m.matches()) {
            throw new ParseException("A SAC phase header specifier requires a phase name followed by an optional dash, '-', and header name like 'a-ttp', not '"
                                     + arg + "'");
        }
        Map results = new HashMap();
        results.put("phase", m.group(1));
        if(m.group(2) != null){
            results.put("header", m.group(2));
            availableHeaders.remove(m.group(2));
        }else{
            results.put("header", availableHeaders.remove(0));
        }
        return results;
    }

    private Pattern re = Pattern.compile("(\\w+)-?([a0-9])?");
    
    private List availableHeaders = new ArrayList();
        

}
