package edu.sc.seis.sod.tools;


public class ServerParser extends PatternParser {

    public ServerParser() {
        super("(.*)/(\\w+)", new String[] {"dns", "name"});
    }

    public String getErrorMessage(String arg) {
        return "The argument should be the server's dns followed by a / then its name like 'edu/iris/dmc/IRIS_NetworkDC' not '"
                + arg + "'";
    }
}