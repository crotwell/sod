package edu.sc.seis.sod.velocity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannel;

/**
 * @author groves Created on May 25, 2005
 */
public class SimpleVelocitizer {

    public String evaluate(String template, Channel chan) {
        try {
            return evaluate("", template, chan);
        } catch(IOException e) {
            throw new RuntimeException("Shouldn't throw an IOException because it shouldn't be doing IO with an empty string fileTemplate");
        }
    }

    public String evaluate(String fileTemplate, String template, Channel chan)
            throws IOException {
        VelocityContext ctx = new VelocityContext();
        new VelocityChannel(chan).insertIntoContext(ctx);
        return evalulate(fileTemplate, template, ctx);
    }

    public String evaluate(String template, EventAccessOperations event) {
        try {
            return evaluate("", template, event);
        } catch(IOException e) {
            throw new RuntimeException("Shouldn't throw an IOException because it shouldn't be doing IO with an empty string fileTemplate");
        }
    }

    public String evaluate(String fileTemplate,
                           String template,
                           EventAccessOperations event) throws IOException {
        VelocityContext ctx = createEventContext(event);
        return evalulate(fileTemplate, template, ctx);
    }

    public String evalulate(String fileTemplate,
                            String template,
                            VelocityContext ctx) throws IOException {
        String result = evaluate(template, ctx);
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
        String filename = evaluate(fileTemplate, ctx);
        File file = new File(filename);
        file.getParentFile().mkdirs();
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

    private VelocityContext createEventContext(EventAccessOperations event) {
        VelocityContext ctx = new VelocityContext();
        if(event instanceof CacheEvent) {
            ctx.put("event", new VelocityEvent((CacheEvent)event));
        } else {
            ctx.put("event", new VelocityEvent(new CacheEvent(event)));
        }
        return ctx;
    }

    public String evaluate(String template,
                           EventAccessOperations event,
                           Channel channel,
                           RequestFilter[] original,
                           RequestFilter[] available,
                           LocalSeismogramImpl[] seismograms,
                           CookieJar cookieJar) {
        try {
            return evaluate(template,
                            "",
                            event,
                            channel,
                            original,
                            available,
                            seismograms,
                            cookieJar);
        } catch(IOException e) {
            throw new RuntimeException("Shouldn't throw an IOException because it shouldn't be doing IO with an empty string fileTemplate");
        }
    }

    public String evaluate(String fileTemplate,
                           String template,
                           EventAccessOperations event,
                           Channel channel,
                           RequestFilter[] original,
                           RequestFilter[] available,
                           LocalSeismogramImpl[] seismograms,
                           CookieJar cookieJar) throws IOException {
        VelocityContext ctx = new WaveformProcessContext(event,
                                                         channel,
                                                         original,
                                                         available,
                                                         seismograms,
                                                         cookieJar);
        return evalulate(fileTemplate, template, ctx);
    }

    private String evaluate(String template, VelocityContext ctx) {
        StringWriter writer = new StringWriter();
        try {
            Velocity.evaluate(ctx, writer, "SimpleVelocitizer", template);
            return writer.toString();
        } catch(Exception e) {
            GlobalExceptionHandler.handle(e);
            return "Unable to evaluate " + template;
        }
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SimpleVelocitizer.class);
    static {
        try {
            Properties props = new Properties();
            props.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                              "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            props.setProperty("runtime.log.logsystem.log4j.category",
                              logger.getName());
            props.setProperty("velocimacro.library", "");
            Velocity.init(props);
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Trouble initializing velocity", e);
        }
    }
}
