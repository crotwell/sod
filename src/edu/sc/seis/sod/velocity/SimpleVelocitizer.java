package edu.sc.seis.sod.velocity;

import java.io.StringWriter;
import java.util.Properties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.RuntimeConstants;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.util.SQLLoader;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.CookieJar;

/**
 * @author groves
 * 
 * Created on May 25, 2005
 */
public class SimpleVelocitizer {

    public String evaluate(String template, Channel chan) {
        return evaluate(template, ContextWrangler.createContext(chan));
    }

    public String evaluate(String template, EventAccessOperations event) {
        return evaluate(template, ContextWrangler.createContext(event));
    }

    public String evaluate(String template,
                           EventAccessOperations event,
                           Channel channel,
                           RequestFilter[] original,
                           RequestFilter[] available,
                           LocalSeismogramImpl[] seismograms,
                           CookieJar cookieJar) {
        return evaluate(template, ContextWrangler.createContext(event,
                                                                channel,
                                                                original,
                                                                available,
                                                                seismograms,
                                                                cookieJar));
    }

    public String evaluate(String template, VelocityContext ctx) {
        StringWriter writer = new StringWriter();
        try {
            try {
                Velocity.evaluate(ctx, writer, "SimpleVelocitizer", template);
            } catch(ParseErrorException parseError) {
                return ERR_PREFIX + "Invalid Velocity";
            }
            return writer.toString();
        } catch(Exception e) {
            GlobalExceptionHandler.handle(e);
            return "Unable to evaluate " + template;
        }
    }

    public static String cleanUpErrorStringForDisplay(String string) {
        if(string.startsWith(ERR_PREFIX)) {
            return string.substring(ERR_PREFIX.length());
        }
        return string;
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SimpleVelocitizer.class);
    static {
        try {
            Properties props = new Properties();
            SQLLoader.setupVelocityLogger(props, logger);
            props.setProperty("velocimacro.library", "");
            Velocity.init(props);
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Trouble initializing velocity", e);
        }
    }

    public static final String ERR_PREFIX = "#ERROR#";
}
