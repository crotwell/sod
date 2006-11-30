package edu.sc.seis.sod.tools;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.PropertyConfigurator;
import org.apache.velocity.VelocityContext;
import org.xml.sax.InputSource;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Option;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;
import com.martiansoftware.jsap.Switch;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.sod.Args;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public class LocateStations {

    public LocateStations() throws JSAPException {
        this(new String[0]);
    }

    public LocateStations(String[] args) throws JSAPException {
        add(new FlaggedOption("server",
                              new ServerParser(),
                              "edu/iris/dmc/IRIS_NetworkDC",
                              false,
                              'y',
                              "server",
                              "The network server to use.  The default is edu/iris/dmc/IRIS_NetworkDC"));
        add(new Switch("recipe",
                       'r',
                       "recipe",
                       "Print the created recipe to stdout instead of running it."));
        add(createListOption("stations",
                             's',
                             "stations",
                             "The codes of stations to retrieve.  If unspecified, all stations for retrieved networks will be retrieved"));
        add(createListOption("networks",
                             'n',
                             "networks",
                             "The codes of networks to retrieve.  If unspecified, all networks will be retrieved"));
        result = jsap.parse(args);
    }

    private FlaggedOption createListOption(String id,
                                           char shortFlag,
                                           String longFlag,
                                           String help) {
        FlaggedOption listOption = new FlaggedOption(id,
                                                     JSAP.STRING_PARSER,
                                                     "",
                                                     false,
                                                     shortFlag,
                                                     longFlag,
                                                     help);
        listOption.setList(true);
        listOption.setListSeparator(',');
        return listOption;
    }

    private void add(Parameter param) throws JSAPException {
        jsap.registerParameter(param);
        if(param instanceof Option){
        options.add(param);
        }
    }

    public VelocityContext getContext() {
        VelocityContext vc = new VelocityContext();
        Iterator it = options.iterator();
        while(it.hasNext()) {
            Option param = (Option)it.next();
            if(param.isList()) {
                vc.put(param.getID(), result.getObjectArray(param.getID()));
            } else {
                vc.put(param.getID(), result.getObject(param.getID()));
            }
        }
        return vc;
    }

    private boolean printRecipe() {
        return result.getBoolean("recipe");
    }

    private List options = new ArrayList();

    private JSAPResult result;

    private JSAP jsap = new JSAP();

    public static class ServerParser extends StringParser {

        public Object parse(String arg) throws ParseException {
            Matcher m = nameDNS.matcher(arg);
            if(!m.matches()) {
                throw new ParseException("The argument should be the server's dns followed by a / then its name like 'edu/iris/dmc/IRIS_NetworkDC' not '"
                        + arg + "'");
            }
            Map server = new HashMap();
            server.put("dns", m.group(1));
            server.put("name", m.group(2));
            return server;
        }

        private static final Pattern nameDNS = Pattern.compile("(.*)/(\\w+)");
    }

    public static void main(String[] args) throws Exception {
        // get some defaults
        Properties props = System.getProperties();
        Initializer.loadProps(Start.createInputStream("jar:edu/sc/seis/sod/tools/simple.props"),
                              props);
        PropertyConfigurator.configure(props);
        SimpleVelocitizer sv = new SimpleVelocitizer();
        LocateStations ls = new LocateStations(args);
        final String result = sv.evaluate(Start.createInputStream("jar:edu/sc/seis/sod/tools/locate_stations.vm"),
                                          ls.getContext());
        if(ls.printRecipe()){
            System.out.println(result);
            System.exit(0);
        }
        Start s = new Start(new Args(new String[] {"-f", "<stream>"}),
                            new Start.InputSourceCreator() {

                                public InputSource create() {
                                    return new InputSource(new StringReader(result));
                                }
                            },
                            props);
        s.start();
    }
}
