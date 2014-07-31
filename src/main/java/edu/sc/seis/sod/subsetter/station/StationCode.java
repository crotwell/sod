package edu.sc.seis.sod.subsetter.station;

import java.util.regex.Pattern;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class StationCode implements StationSubsetter {

    public StationCode(Element config) throws ConfigurationException { 
        code = SodUtil.getNestedText(config);
        Pattern simpleCode = Pattern.compile("[A-Z0-9]+");
        if (simpleCode.matcher(code).matches() && code.trim().length() > 5) {
            throw new ConfigurationException("Station codes are limited to 5 characters, not "+code.trim().length()+" as in '"+code.trim()+"'");
        }
        pattern  = Pattern.compile(createRegexFromGlob(code));
    }

    public StringTree accept(StationImpl station, NetworkSource network) {
        if(code.equalsIgnoreCase(station.get_code())) {
            return new Pass(this);
        } else if(pattern.matcher(station.get_code()).matches()) {
            return new Pass(this);
        } else {
            return new Fail(this);
        }
    }
    
    String code;
    
    Pattern pattern;

    public String getCode() {
        return code;
    }
    
    /*
     * from http://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns
     */
    public static String createRegexFromGlob(String glob)
    {
        String out = "^";
        for(int i = 0; i < glob.length(); ++i)
        {
            final char c = glob.charAt(i);
            switch(c)
            {
            case '*': out += ".*"; break;
            case '?': out += '.'; break;
            case '.': out += "\\."; break;
            case '\\': out += "\\\\"; break;
            default: out += c;
            }
        }
        out += '$';
        return out;
    }
}
