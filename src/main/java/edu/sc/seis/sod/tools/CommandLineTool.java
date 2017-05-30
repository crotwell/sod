package edu.sc.seis.sod.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.apache.velocity.VelocityContext;
import org.xml.sax.InputSource;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.Option;
import com.martiansoftware.jsap.Switch;

import edu.sc.seis.seisFile.client.AbstractClient;
import edu.sc.seis.sod.Args;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.hibernate.Initializer;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public class CommandLineTool extends AbstractClient {

    public CommandLineTool(String[] args) throws JSAPException {
        super(args);
    }
    
    protected void addParams() throws JSAPException {
        super.addParams();
        add(new Switch("recipe",
                       'r',
                       "recipe",
                       "Print the created recipe to stdout instead of running it"));
        add(new FlaggedOption("server",
                              JSAP.STRING_PARSER,
                              null,
                              false,
                              'S',
                              "server",
                              "The server to use, default is 'service.iris.edu'"));
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
                } else if (result.getObject(param.getID()) != null) {
                    vc.put(param.getID(), result.getObject(param.getID()));
                }
            }
        }
        return vc;
    }

    public boolean shouldPrintRecipe() {
        return result.getBoolean("recipe");
    }

    public InputStream getTemplate() throws IOException {
        String className = getClass().getName().replace('.', '/');
        return Start.createInputStream("jar:" + className + ".vm");
    }

    protected boolean requiresStdin = false;

    public static void run(CommandLineTool ls) throws Exception {
        Start.checkGCJ();
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
            System.err.println(ls.commandName + " " + Version.current().getVersion());
            System.exit(0);
        }
        if(!ls.isSuccess()) {
            Start.exit(Args.makeError(ls.commandName, ls.params, ls.result));
        }
        VelocityContext ctx = ls.getContext();
        SimpleVelocitizer sv = new SimpleVelocitizer();
        // Wait two seconds before checking for input on system in to allow
        // sluggardly pipers to do their work
        try {
            Thread.sleep(2000);
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
                if(!inSod && line.indexOf("<sod>") != -1) {
                    inSod = true;
                } else if(inSod) {
                    if(line.indexOf("</sod>") != -1) {
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
        } else {
            ctx.put("additionalArms", "");
        }
        final String result = sv.evaluate(ls.getTemplate(), ctx);
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
