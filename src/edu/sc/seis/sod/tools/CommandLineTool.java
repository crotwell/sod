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
import com.martiansoftware.jsap.Switch;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.sod.Args;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Version;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public class CommandLineTool {

    public CommandLineTool(String[] args) throws JSAPException {
        addParams();
        String[] segs = getClass().getName().split("\\.");
        jsap.setUsage(Args.makeUsage(segs[segs.length - 1], params));
        result = jsap.parse(args);
        if(args.length == 0) {
            result.addException("Must use at least one option",
                                new RuntimeException("Must use at least one option"));
        }
    }

    protected FlaggedOption createListOption(String id,
                                             char shortFlag,
                                             String longFlag,
                                             String help) {
        FlaggedOption listOption = new FlaggedOption(id,
                                                     JSAP.STRING_PARSER,
                                                     null,
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
                       "Print SOD's version and exit."));
        add(new Switch("recipe",
                       'r',
                       "recipe",
                       "Print the created recipe to stdout instead of running it."));
        add(new Switch("help", 'h', "help", "Print this message."));
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
            if(!(cur instanceof Option)) {
                continue;
            }
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
        return vc;
    }

    protected boolean isSpecified(Parameter p) {
        return result.contains(p.getID());
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
    
    public String getErrorMessage() {
        StringBuffer buff = new StringBuffer();
        buff.append(jsap.getUsage() + "\n");
        Iterator it = result.getErrorMessageIterator();
        while(it.hasNext()) {
            buff.append(it.next() + "\n");
        }
        return buff.toString();
    }

    public String getHelpMessage() {
        StringBuffer buff = new StringBuffer();
        buff.append(jsap.getUsage() + '\n');
        buff.append(jsap.getHelp());
        return buff.toString();
    }

    public InputStream getTemplate() throws IOException {
        String className = getClass().getName().replace('.', '/');
        return Start.createInputStream("jar:" + className + ".vm");
    }
    
    protected boolean requiresStdin = false;

    private List params = new ArrayList();

    protected JSAPResult result;

    private JSAP jsap = new JSAP();

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
        // get some defaults
        Properties props = System.getProperties();
        Initializer.loadProps(Start.createInputStream("jar:edu/sc/seis/sod/tools/simple.props"),
                              props);
        PropertyConfigurator.configure(props);
        if(ls.shouldPrintHelp()) {
            System.err.println(ls.getHelpMessage());
            System.exit(0);
        }
        if(ls.shouldPrintVersion()) {
            System.err.println(VERSION_STRING);
            System.exit(0);
        }
        if(!ls.isSuccess()) {
            System.err.println(ls.getErrorMessage());
            System.exit(1);
        }
        VelocityContext ctx = ls.getContext();
        SimpleVelocitizer sv = new SimpleVelocitizer();
        Level current = PrintlineVelocitizer.quietLogger();
        if(System.in.available() > 0) {
            StringBuffer buff = new StringBuffer();
            BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
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
        }else if(ls.requiresStdin){
            System.err.println("This tool requires that a recipe be piped into it");
            System.exit(1);
        }
        final String result = sv.evaluate(ls.getTemplate(), ctx);
        PrintlineVelocitizer.reinstateLogger(current);
        if(ls.shouldPrintRecipe()) {
            System.out.println(result);
            System.exit(0);
        }
        Start s = new Start(new Args(new String[] {"-f", "<stream>"}),
                            new Start.InputSourceCreator() {

                                public InputSource create() {
                                    return new InputSource(new StringReader("<sod>\n"
                                            + result));
                                }
                            },
                            props);
        s.start();
    }

    private static final String VERSION_STRING = "SOD Command Line Tools: s"
            + Version.getVersion();
}
