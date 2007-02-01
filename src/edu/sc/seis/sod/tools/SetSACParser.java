package edu.sc.seis.sod.tools;


public class SetSACParser extends PatternParser {

    public SetSACParser() {
        super("(\\w+)-([a0-9])", new String[]{"phase", "header"});
    }

    public String getErrorMessage(String arg) {
        return ("A SAC phase header specifier requires a phase name, a dash, '-', and a header name like 'a-ttp', not '"+ arg +"'");
    }
}
