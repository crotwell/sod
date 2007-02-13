package edu.sc.seis.sod.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.velocity.VelocityContext;
import org.xml.sax.InputSource;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Option;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.StringParser;
import com.martiansoftware.jsap.Switch;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.sod.Args;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.Version;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public class CommandLineTool {

    public CommandLineTool(String[] args) throws JSAPException {
        this.args = args;
        addParams();
        String[] segs = getClass().getName().split("\\.");
        commandName = segs[segs.length - 1];
        result = jsap.parse(args);
        if(requiresAtLeastOneArg() && args.length == 0) {
            result.addException("Must use at least one option",
                                new RuntimeException("Must use at least one option"));
        }
    }

    protected boolean requiresAtLeastOneArg() {
        return true;
    }

    protected FlaggedOption createListOption(String id,
                                             char shortFlag,
                                             String longFlag,
                                             String help) {
        return createListOption(id,
                                shortFlag,
                                longFlag,
                                help,
                                null,
                                JSAP.STRING_PARSER);
    }

    protected FlaggedOption createListOption(String id,
                                             char shortFlag,
                                             String longFlag,
                                             String help,
                                             String defaultArg) {
        return createListOption(id,
                                shortFlag,
                                longFlag,
                                help,
                                defaultArg,
                                JSAP.STRING_PARSER);
    }

    protected FlaggedOption createListOption(String id,
                                             char shortFlag,
                                             String longFlag,
                                             String help,
                                             String defaultArg,
                                             StringParser parser) {
        FlaggedOption listOption = new FlaggedOption(id,
                                                     parser,
                                                     defaultArg,
                                                     false,
                                                     shortFlag,
                                                     longFlag,
                                                     help);
        listOption.setList(true);
        listOption.setListSeparator(',');
        return listOption;
    }

    protected void addParams() throws JSAPException {
        add(new Switch("version",
                       'v',
                       "version",
                       "Print SOD's version and exit"));
        add(new Switch("recipe",
                       'r',
                       "recipe",
                       "Print the created recipe to stdout instead of running it"));
        add(new Switch("help", 'h', "help", "Print this message."));
        add(new FlaggedOption("props",
                              JSAP.STRING_PARSER,
                              null,
                              false,
                              'p',
                              "props",
                              "Use an additional props file"));
    }

    protected void add(Parameter param) throws JSAPException {
        jsap.registerParameter(param);
        params.add(param);
    }

    public VelocityContext getContext() {
        VelocityContext vc = new VelocityContext();
        Iterator it = params.iterator();
        while(it.hasNext()) {
            Object cur = it.next();
            if(cur instanceof Switch) {
                Switch sw = (Switch)cur;
                if(result.getBoolean(sw.getID())) {
                    vc.put(sw.getID(), Boolean.TRUE);
                }
            } else {
                Option param = (Option)cur;
                if(param.isList()) {
                    Object[] paramResult = result.getObjectArray(param.getID());
                    if(paramResult.length > 0) {
                        vc.put(param.getID(), paramResult);
                    }
                } else {
                    vc.put(param.getID(), result.getObject(param.getID()));
                }
            }
        }
        return vc;
    }

    protected boolean isSpecified(Parameter p) {
        return result.contains(p.getID());
    }

    public String[] getArgs() {
        return args;
    }

    public boolean shouldPrintHelp() {
        return result.getBoolean("help");
    }

    public boolean shouldPrintRecipe() {
        return result.getBoolean("recipe");
    }

    public boolean shouldPrintVersion() {
        return result.getBoolean("version");
    }

    public boolean isSuccess() {
        return result.success();
    }

    public InputStream getTemplate() throws IOException {
        String className = getClass().getName().replace('.', '/');
        return Start.createInputStream("jar:" + className + ".vm");
    }

    protected boolean requiresStdin = false;

    private List params = new ArrayList();

    protected JSAPResult result;

    private JSAP jsap = new JSAP();

    private String[] args;

    private String commandName;

    public static void run(CommandLineTool ls) throws Exception {
        if(ls.shouldPrintRecipe()) {
            /*
             * Sad, sad, sad, sad, sad hack to get piping system.in to work in
             * java.
             * 
             * Print something out immediately so downstream CommandLineTools
             * starting at the same time will get something from
             * System.in.available() and read in the data. If we don't use
             * System.in.available() there's no way to tell if there's something
             * in the pipe and we block forever on System.in.read()
             */
            System.out.println("<sod>");
            System.out.flush();
        }
        Start.setCommandName(ls.commandName);
        // get some defaults
        Properties props = System.getProperties();
        Initializer.loadProps(Start.createInputStream("jar:edu/sc/seis/sod/tools/simple.props"),
                              props);
        Initializer.loadProperties(ls.getArgs(), props, false);
        PropertyConfigurator.configure(props);
        if(ls.shouldPrintHelp()) {
            System.err.println(Args.makeHelp(ls.commandName, ls.params));
            System.exit(0);
        }
        if(ls.shouldPrintVersion()) {
            System.err.println(ls.commandName + " " + Version.getVersion());
            System.exit(0);
        }
        if(!ls.isSuccess()) {
            Start.exit(Args.makeError(ls.commandName, ls.params, ls.result));
        }
        VelocityContext ctx = ls.getContext();
        SimpleVelocitizer sv = new SimpleVelocitizer();
        Level current = PrintlineVelocitizer.quietLogger();
        // Wait three seconds before checking for input on system in to allow
        // sluggardly pipers to do their work
        try {
            Thread.sleep(3000);
        } catch(InterruptedException ie) {
            // What do you want me to do?
        }
        if(System.in.available() > 0) {
            StringBuffer buff = new StringBuffer();
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader r = new BufferedReader(isr);
            String line;
            boolean inSod = false;
            while((line = r.readLine()) != null) {
                if(!inSod && line.contains("<sod>")) {
                    inSod = true;
                } else if(inSod) {
                    if(line.contains("</sod>")) {
                        inSod = false;
                    } else {
                        buff.append(line);
                        buff.append('\n');
                    }
                }
            }
            if(buff.length() == 0) {
                if(!inSod) {
                    System.err.println("There was input on sysin, but it doesn't look like it was a recipe file");
                }
                System.exit(1);
            }
            ctx.put("additionalArms", buff.toString());
        } else if(ls.requiresStdin) {
            Start.exit("This tool requires that a recipe be piped into it.");
        }
        final String result = sv.evaluate(ls.getTemplate(), ctx);
        PrintlineVelocitizer.reinstateLogger(current);
        if(ls.shouldPrintRecipe()) {
            System.out.println(result);
            System.exit(0);
        }
        try {
            Start s = new Start(new Args(new String[] {"-f", "<stream>"}),
                                new Start.InputSourceCreator() {

                                    public InputSource create() {
                                        return new InputSource(new StringReader("<sod>\n"
                                                + result));
                                    }
                                },
                                props,
                                true);
            s.start();
        } catch(UserConfigurationException e) {
            Start.exit(e.getMessage()
                    + "  SOD will quit now and continue to cowardly quit until this is corrected.");
        }
    }
}
