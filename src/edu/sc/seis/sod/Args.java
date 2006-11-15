package edu.sc.seis.sod;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;
import com.martiansoftware.jsap.Switch;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.sod.editor.SimpleGUIEditor;

public class Args {

    public Args(String[] args) throws JSAPException {
        this.args = args;
        List toParse = new ArrayList(args.length);
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-conf")) {
                args[i] = "-f";
            }
            for(int j = 0; j < Initializer.POSSIBLE_PROP_OPTION_NAMES.length; j++) {
                if(args[i].equals(Initializer.POSSIBLE_PROP_OPTION_NAMES[j])) {
                    args[i] = "-p";
                }
            }
            if(args[i].equals("-demo") || args[i].equals("--demo")) {
                // replace demo with a like -f command
                toParse.add("-f");
                toParse.add(SimpleGUIEditor.TUTORIAL_LOC);
            } else if(args[i].equals("-hsql")) {
                // Skip over -hsql and its argument
                i++;
            } else {
                toParse.add(args[i]);
            }
        }
        add(new Switch("help", 'h', "help", "Print out this message"));
        add(new Switch("event-arm", 'e', "event-arm", "Only run the event arm"));
        add(new Switch("network-arm",
                       'n',
                       "network-arm",
                       "Only run the network arm"));
        add(new Switch("validate",
                       'v',
                       "validate",
                       "Validate the recipe and exit"));
        add(new Switch("invalid",
                       'i',
                       "invalid",
                       "The recipe is known to be invalid so skip the 10 second wait"));
        add(new Switch("replace-recipe",
                       'r',
                       "replace-recipe",
                       "Replace the recipe in the db with the one specified for this run"));
        Switch version = new Switch("version");
        version.setLongFlag("version");
        version.setHelp("Print SOD's version and exit");
        add(version);
        add(new FlaggedOption("props",
                              new FileParser(),
                              null,
                              false,
                              'p',
                              "props",
                              "A properties file to configure SOD"));
        add(new FlaggedOption("recipe",
                              JSAP.STRING_PARSER,
                              null,
                              true,
                              'f',
                              "recipe",
                              "The recipe to run"));
        StringBuffer builder = new StringBuffer("Usage: sod [-");
        Iterator it = parameters.iterator();
        while(it.hasNext()){
           Parameter p = (Parameter)it.next();
           if(p instanceof Switch){
               builder.append(((Switch)p).getShortFlagCharacter());
           }
        }   
        builder.append("] ");
        it = parameters.iterator();
        while(it.hasNext()){
            Parameter p = (Parameter)it.next();
            if(p instanceof FlaggedOption){
                FlaggedOption fo = (FlaggedOption)p;
                if(!fo.required()){
                    builder.append('[');
                }
                builder.append("-" + ((FlaggedOption)p).getShortFlagCharacter() + " <" + p.getID() + ">");
                if(!fo.required()){
                    builder.append(']');
                }
                builder.append(' ');
            }
         }   
        jsap.setUsage(builder.toString());
        result = jsap.parse(args);
        if(result.getBoolean("version")) {
            System.out.println("SOD " + Version.getVersion());
            System.exit(0);
        }
        if(result.getBoolean("help")) {
            System.out.println(jsap.getUsage());
            System.out.println(jsap.getHelp());
            System.exit(0);
        }
        if(result.userSpecified("event-arm")
                && result.userSpecified("network-arm")) {
            result.addException("General",
                                new RuntimeException("Only one of -e and -n may be specified"));
        }
        if(!result.success()) {
            System.out.println(jsap.getUsage());
            it = result.getErrorMessageIterator();
            while(it.hasNext()) {
                System.err.println(it.next());
            }
            System.exit(1);
        }
    }

    private void add(Parameter p) throws JSAPException {
        jsap.registerParameter(p);
        parameters.add(p);
    }

    public boolean hasProps() {
        return result.contains("props");
    }

    public InputStream getProps() {
        try {
            return new BufferedInputStream(new FileInputStream(result.getFile("props")));
        } catch(FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getInitialArgs() {
        return args;
    }

    public String getRecipe() {
        return result.getString("recipe");
    }

    public boolean onlyValidate() {
        return result.getBoolean("validate");
    }

    public boolean doEventArm() {
        return result.getBoolean("event-arm")
                || !result.getBoolean("network-arm");
    }

    public boolean doNetArm() {
        return !result.getBoolean("event-arm")
                || result.getBoolean("network-arm");
    }

    public boolean doWaveformArm() {
        return !result.getBoolean("event-arm")
                && !result.getBoolean("network-arm");
    }

    public boolean waitOnError() {
        return !onlyValidate() && !result.getBoolean("invalid");
    }

    public boolean replaceDBConfig() {
        return result.getBoolean("replace-recipe");
    }

    private JSAPResult result;

    private JSAP jsap = new JSAP();

    private List parameters = new ArrayList();

    private String[] args;

    private static class FileParser extends StringParser {

        public Object parse(String arg) throws ParseException {
            File f = new File(arg);
            if(!f.exists()) {
                throw new ParseException("'" + arg + "' doesn't exist");
            }
            return f;
        }
    }
}
