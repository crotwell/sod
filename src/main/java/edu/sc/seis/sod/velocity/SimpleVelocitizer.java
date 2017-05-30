package edu.sc.seis.sod.velocity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.ISOTime;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;

/**
 * @author groves
 * 
 * Created on May 25, 2005
 */
public class SimpleVelocitizer {

    public static String format(MicroSecondDate date, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(ISOTime.UTC);
        return dateFormat.format(date);
    }

    public String evaluate(String template, ChannelImpl chan) {
        return evaluate(template, ContextWrangler.createContext(chan));
    }

    public String evaluate(String template, CacheEvent event) {
        return evaluate(template, ContextWrangler.createContext(event));
    }

    public String evaluate(String template,
                           CacheEvent event,
                           ChannelImpl channel,
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

    public String evaluate(InputStream template, VelocityContext ctx) {
        StringWriter writer = new StringWriter();
        try {
            try {
                evaluate(template, ctx, writer);
            } catch(ParseErrorException parseError) {
                return ERR_PREFIX + "Invalid Velocity";
            }
        } catch(Exception e) {
            GlobalExceptionHandler.handle(e);
            return "Unable to evaluate " + template;
        }
        return writer.toString();
    }

    public void evaluate(InputStream template,
                         VelocityContext ctx,
                         Writer writer) throws ParseErrorException, Exception {
        Velocity.evaluate(ctx,
                          writer,
                          "SimpleVelocitizer",
                          new InputStreamReader(template));
    }

    public static String cleanUpErrorStringForDisplay(String string) {
        if(string.startsWith(ERR_PREFIX)) {
            return string.substring(ERR_PREFIX.length());
        }
        return string;
    }

    public static final String VELOCITY_LOGGER_NAME = "runtime.log.logsystem.log4j.logger";
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SimpleVelocitizer.class);

    public static void setupVelocityLogger(Properties velocityProps,  Logger velocityLogger) {
        velocityProps.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                          "org.apache.velocity.runtime.log.Log4JLogChute");
        velocityProps.setProperty(VELOCITY_LOGGER_NAME,
                          logger.getName());
    }
    
    static {
        try {
            Properties props = new Properties();
            setupVelocityLogger(props, logger);
            props.setProperty("velocimacro.library", "");
            Velocity.init(props);
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Trouble initializing velocity", e);
        }
    }

    public static final String ERR_PREFIX = "#ERROR#";
}
