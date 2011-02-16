package edu.sc.seis.sod.velocity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.log4j.Level;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.StationImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.util.SQLLoader;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.status.FissuresFormatter;

/**
 * Handles getting stuff in the context and directing output to System.out or a
 * file for the printlineprocess classes
 * 
 * @author groves
 * 
 * Created on May 30, 2005
 */
public class PrintlineVelocitizer {

    /**
     * Evaluates the templates such that errors might be discovered
     */
    public PrintlineVelocitizer(String[] strings) throws ConfigurationException {
        for(int i = 0; i < strings.length; i++) {
            try {
                Level cur = quietLogger();
                Velocity.evaluate(new VelocityContext(),
                                  new StringWriter(),
                                  "PrintlineTest",
                                  strings[i]);
                reinstateLogger(cur);
            } catch(ParseErrorException e) {
                throw new UserConfigurationException("Malformed Velocity '"
                        + strings[i] + "'.  " + e.getMessage());
            } catch(Exception e) {
                throw new ConfigurationException("Exception caused by testing Velocity",
                                                 e);
            }
        }
    }
    
    public static Level quietLogger(){
    	System.err.println("quietLogger not working with slf4j, fix me!");
        /*
    	String prop = (String)Velocity.getProperty(SQLLoader.VELOCITY_LOGGER_NAME);
        Level current = null;
        if(prop != null) {
            current = LoggerFactory.getLogger(prop).getEffectiveLevel();
            LoggerFactory.getLogger(prop).setLevel(Level.ERROR);
        }
        return current;
        */
        return Level.DEBUG;
    }
    
    public static void reinstateLogger(Level level){
    	System.err.println("quietLogger not working with slf4j, fix me!");
    	/*
        String prop = (String)Velocity.getProperty(SQLLoader.VELOCITY_LOGGER_NAME);
        if(prop != null) {
            LoggerFactory.getLogger(prop).setLevel(level);
        }
        */
    }

    public String evaluate(String fileTemplate,
                           String template,
                           NetworkAttr attr) throws IOException {
        return evalulate(fileTemplate,
                         template,
                         ContextWrangler.createContext(attr));
    }

    public String evaluate(String fileTemplate, String template, Channel chan)
            throws IOException {
        return evalulate(fileTemplate,
                         template,
                         ContextWrangler.createContext(chan));
    }

    public String evaluate(String fileTemplate, String template, StationImpl sta)
            throws IOException {
        return evalulate(fileTemplate,
                         template,
                         ContextWrangler.createContext(sta));
    }

    public String evaluate(String fileTemplate, String template, 
                           EventAccessOperations event, StationImpl sta, CookieJar cookieJar)
            throws IOException {
        VelocityContext cntxt = ContextWrangler.createContext(sta);
        ContextWrangler.insertIntoContext(event, cntxt);
        cntxt.put("cookieJar", cookieJar);
        return evalulate(fileTemplate,
                         template,
                         cntxt);
    }

    public String evaluate(String filename,
                           String template,
                           EventAccessOperations event,
                           Channel channel,
                           RequestFilter[] request,
                           CookieJar cookieJar) throws IOException {
        return evaluate(filename,
                        template,
                        event,
                        channel,
                        request,
                        new RequestFilter[0],
                        cookieJar);
    }

    public String evaluate(String filename,
                           String template,
                           EventAccessOperations event,
                           Channel channel,
                           RequestFilter[] original,
                           RequestFilter[] available,
                           CookieJar cookieJar) throws IOException {
        return evaluate(filename,
                        template,
                        event,
                        channel,
                        original,
                        available,
                        new LocalSeismogramImpl[0],
                        cookieJar);
    }

    public String evaluate(String fileTemplate,
                           String template,
                           EventAccessOperations event,
                           Channel channel,
                           RequestFilter[] original,
                           RequestFilter[] available,
                           LocalSeismogramImpl[] seismograms,
                           CookieJar cookieJar) throws IOException {
        return evalulate(fileTemplate,
                         template,
                         ContextWrangler.createContext(event,
                                                       channel,
                                                       original,
                                                       available,
                                                       seismograms,
                                                       cookieJar));
    }

    public String evaluate(String fileTemplate,
                           String template,
                           EventAccessOperations event) throws IOException {
        return evalulate(fileTemplate,
                         template,
                         ContextWrangler.createContext(event));
    }

    public String evalulate(String fileTemplate,
                            String template,
                            VelocityContext ctx) throws IOException {
        String result = simple.evaluate(template, ctx);
        if(fileTemplate.equals("")) {
            System.out.println(result);
        } else {
            appendToFile(fileTemplate, result, ctx);
        }
        return result;
    }

    private void appendToFile(String fileTemplate,
                              String toAppend,
                              VelocityContext ctx) throws IOException {
        String filename = FissuresFormatter.filize(simple.evaluate(fileTemplate,
                                                                   ctx));
        File file = new File(filename);
        file.getAbsoluteFile().getParentFile().mkdirs();
        FileWriter fwriter = new FileWriter(file, true);
        BufferedWriter bwriter = null;
        try {
            bwriter = new BufferedWriter(fwriter);
            bwriter.write(toAppend);
            bwriter.newLine();
        } finally {
            if(bwriter != null) {
                bwriter.close();
            }
        }
    }

    private SimpleVelocitizer simple = new SimpleVelocitizer();
}
