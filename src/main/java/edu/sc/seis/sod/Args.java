package edu.sc.seis.sod;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.martiansoftware.jsap.Flagged;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.StringParser;
import com.martiansoftware.jsap.Switch;

import edu.sc.seis.fissuresUtil.simple.Initializer;

public class Args {

    private static final String COMMAND_NAME = "sod";

    public Args(String[] args) throws JSAPException {
        this.args = args;
        List<String> toParse = new ArrayList<String>(args.length);
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
                toParse.add(Start.TUTORIAL_LOC);
            } else if(args[i].equals("-hsql")) {
                // Skip over -hsql and its argument
                i++;
            } else {
                toParse.add(args[i]);
            }
        }
        add(new Switch("debug", JSAP.NO_SHORTFLAG, "debug", "extra debug output to log file"));
        add(new Switch("help", 'h', "help", "Print out this message"));
        add(new Switch("event-arm", 'e', "event-arm", "Only run the event arm"));
        add(new Switch("network-arm",
                       'n',
                       "network-arm",
                       "Only run the network arm"));
        add(new Switch("validate",
                       'V',
                       "validate",
                       "Validate the recipe and exit"));
        add(new Switch(CLEAN_SWITCH,
                       'c',
                       CLEAN_SWITCH,
                       "Clean the database before starting, any existing results are lost."));
        add(new Switch("quick",
                       'q',
                       "quick",
                       "Run quick and dirty, the database is not saved to disk, hence no crash recovery"));
        add(new Switch("invalid",
                       'i',
                       "invalid",
                       "The recipe is known to be invalid so skip the 10 second wait"));
        add(new Switch(PRINT_RECIPE_SWITCH,
                       'r',
                       PRINT_RECIPE_SWITCH,
                       "Print the created recipe to stdout instead of running it"));
        add(new Switch("replace-recipe",
                       JSAP.NO_SHORTFLAG,
                       "replace-recipe",
                       "Replace the recipe in the db with this one"));
        add(new Switch("version",
                       'v',
                       "version",
                       "Print SOD's version and exit"));
        add(new Switch(QUIT_ON_ERROR_SWITCH,
                       JSAP.NO_SHORTFLAG,
                       QUIT_ON_ERROR_SWITCH,
                       "Exit on the first error instead of retrying"));
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
        add(new Switch("demo",
                       JSAP.NO_SHORTFLAG,
                       "demo",
                       "Run an demo recipe. Use with -r to see the demo recipe."));
        result = jsap.parse((String[])toParse.toArray(new String[0]));
        if(result.getBoolean("version")) {
            System.out.println("SOD " + Version.current().getVersion());
            System.exit(0);
        }
        if(result.getBoolean("help")) {
            System.err.println(makeHelp(COMMAND_NAME, parameters));
            System.exit(0);
        }
        if(result.userSpecified("event-arm")
                && result.userSpecified("network-arm")) {
            result.addException("General",
                                new RuntimeException("Only one of -e and -n may be specified"));
        }
        if(!result.success()) {
            Start.exit(makeError(COMMAND_NAME, parameters, result));
        }
    }

    public static String makeUsage(String command, List params) {
        StringBuffer builder = new StringBuffer("Usage: " + command + " [-");
        Iterator it = params.iterator();
        while(it.hasNext()) {
            Parameter p = (Parameter)it.next();
            if(p instanceof Switch
                    && ((Switch)p).getShortFlagCharacter() != null) {
                builder.append(((Switch)p).getShortFlagCharacter());
            }
        }
        builder.append("] ");
        it = params.iterator();
        while(it.hasNext()) {
            Parameter p = (Parameter)it.next();
            if(p instanceof FlaggedOption) {
                FlaggedOption fo = (FlaggedOption)p;
                if(!fo.required()) {
                    builder.append('[');
                }
                if(((FlaggedOption)p).getShortFlag() != JSAP.NO_SHORTFLAG) {
                    builder.append('-');
                    builder.append(((FlaggedOption)p).getShortFlag());
                } else {
                    builder.append("--");
                    builder.append(((FlaggedOption)p).getLongFlag());
                }
                builder.append(" <");
                builder.append(p.getID());
                builder.append('>');
                if(!fo.required()) {
                    builder.append(']');
                }
                builder.append(' ');
            }
        }
        builder.append('\n');
        return builder.toString();
    }

    public static String makeHelp(String command, List params) {
        StringBuffer buff = new StringBuffer();
        buff.append(makeUsage(command, params));
        buff.append('\n');
        Iterator it = params.iterator();
        while(it.hasNext()) {
            Parameter sw = (Parameter)it.next();
            buff.append("  ");
            if(((Flagged)sw).getShortFlag() != JSAP.NO_SHORTFLAG) {
                buff.append('-');
                buff.append(((Flagged)sw).getShortFlag());
                buff.append('/');
            }
            buff.append("--");
            buff.append(((Flagged)sw).getLongFlag());
            buff.append(' ');
            if(sw instanceof FlaggedOption) {
                FlaggedOption fo = (FlaggedOption)sw;
                if(fo.isList()) {
                    buff.append(fo.getShortFlag());
                    buff.append("1,");
                    buff.append(fo.getShortFlag());
                    buff.append("2,...,");
                    buff.append(fo.getShortFlag());
                    buff.append('n');
                } else {
                    buff.append('<');
                    buff.append(sw.getID());
                    buff.append('>');
                }
            }
            buff.append("  ");
            buff.append(sw.getHelp());
            buff.append('\n');
        }
        return buff.toString();
    }

    public static String makeError(String command,
                                   List params,
                                   JSAPResult result) {
        StringBuffer buff = new StringBuffer();
        buff.append(Args.makeUsage(command, params));
        buff.append('\n');
        Iterator it = result.getErrorMessageIterator();
        while(it.hasNext()) {
            buff.append(it.next());
            buff.append('\n');
        }
        return buff.toString();
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

    public boolean isQuickAndDirty() {
        return result.getBoolean("quick");
    }

    public boolean isQuitOnError() {
        return result.getBoolean(QUIT_ON_ERROR_SWITCH);
    }

    public boolean isClean() {
        return result.getBoolean(CLEAN_SWITCH);
    }
    
    public boolean isPrintRecipe() {
        return result.getBoolean(PRINT_RECIPE_SWITCH);
    }

    public boolean isDebug() {
        return result.getBoolean("debug");
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
    
    public String toString() {
        String out = "";
        for (int i = 0; i < args.length; i++) {
            out += " "+args[i];
        }
        return out;
    }

    public static final String CLEAN_SWITCH = "clean";
    public static final String QUIT_ON_ERROR_SWITCH = "quitOnError";
    public static final String PRINT_RECIPE_SWITCH = "print-recipe";
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Args.class);
}
