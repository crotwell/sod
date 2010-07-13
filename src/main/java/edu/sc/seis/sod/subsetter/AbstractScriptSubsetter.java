package edu.sc.seis.sod.subsetter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.w3c.dom.Element;

import edu.sc.seis.bag.Bag;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class AbstractScriptSubsetter implements Subsetter {

    public AbstractScriptSubsetter(Element config) {
        this.config = config;
        scriptType = config.getAttribute("type");
        engine = factory.getEngineByName(scriptType);
        script = cleanScript(SodUtil.getNestedText(config));
    }

    protected Object preeval() throws Exception {
        try {
            if (scriptType.equals("jython") || scriptType.equals("python")) {
                engine.eval("import sys");
                engine.eval("sys.path.append('" + Bag.formatForJythonSysPath(Bag.class, "edu/sc/seis/bag/jython")
                        + "')");
                engine.eval("from bag import *");
            }
            engine.put("result", new Pass(this));
            engine.put("util", new ScriptUtil(this));
            Object result = engine.eval(script);
            return result;// might be null
        } catch(ScriptException e) {
            logger.error("Problem in script: \n-------------|\n" + script + "|-------", e);
            Start.exit("Problem in script: \n-------------|\n" + script + "|-------\n\n" + e);
            // never get here...
            throw new RuntimeException();
        }
    }

    protected Object pullResult(Object result) throws Exception {
        if (result == null) {
            // try getting variable named result from engine
            result = engine.get("result");
        }
        if (result == null) {
            // assume all well and return a Pass
            return new Pass(this);
        }
        if (result instanceof StringTree) {
            return (StringTree)result;
        } else if (result instanceof Boolean) {
            return new StringTreeLeaf(this, ((Boolean)result).booleanValue());
        } else {
            throw new UnknownScriptResult("Script returns unknown results type, should be boolean or StringTree: " + result.toString());
        }
    }

    protected StringTree eval() throws Exception {
        return (StringTree)pullResult(preeval());
    }

    public static String cleanScript(String script) {
        if (script.indexOf("\n") == -1) {
            // one line
            return script.trim();
        }
        String trimBegEnd = script;
        // clean blank lines from beginning
        Matcher matcher = Pattern.compile("(?: *\\n)+(.*)", Pattern.DOTALL).matcher(trimBegEnd);
        if (matcher.matches()) {
            trimBegEnd = matcher.group(1);
        }
        // clean blank lines from end
        matcher = Pattern.compile("((?:(?: *\\n)*(?: *\\S[^\\n]*\\n)+)+)(?: *\\n?)*").matcher(trimBegEnd);
        if (matcher.matches()) {
            trimBegEnd = matcher.group(1);
        }
        String out = "";
        String[] lines = trimBegEnd.split("[\\r?\\n]");
        matcher = Pattern.compile("( *).*").matcher(lines[0]);
        if (matcher.matches()) {
            int numSpaces = matcher.group(1).length();
            Pattern spaceTrimmer = Pattern.compile(" {0," + numSpaces + "}(.*)");
            for (int i = 0; i < lines.length; i++) {
                matcher = spaceTrimmer.matcher(lines[i]);
                if (matcher.matches()) {
                    out += matcher.group(1) + "\n";
                } else {
                    throw new RuntimeException("How can this not match? pat=\"" + spaceTrimmer.pattern() + "\"  |"
                            + lines[i] + "|");
                }
            }
        } else {
            throw new RuntimeException("Didn't match:" + lines[0] + "|");
        }
        return out;
    }

    protected String script;

    protected Element config;

    protected String scriptType;;

    protected ScriptEngine engine;

    protected static ScriptEngineManager factory = new ScriptEngineManager();

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbstractScriptSubsetter.class);
}
