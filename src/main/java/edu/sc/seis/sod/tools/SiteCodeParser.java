package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.StringParser;


public class SiteCodeParser extends StringParser{

    public Object parse(String arg) {
        if(arg.equals("__")){
            return "  ";
        }
        return arg;
    }
    
}
