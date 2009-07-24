package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.FlaggedOption;

public class ServerParser extends PatternParser {

    public ServerParser() {
        super("(.*)/(\\w+)", new String[] {"dns", "name"});
    }

    public static FlaggedOption createParam(String defaultServer, String helpMsg) {
        return new FlaggedOption("server",
                                 new ServerParser(),
                                 defaultServer,
                                 false,
                                 'S',
                                 "server",
                                 helpMsg);
    }

    public String getErrorMessage(String arg) {
        return "A server is specified as its dns followed by a / then its name like 'edu/iris/dmc/IRIS_NetworkDC' not '"
                + arg + "'";
    }
}