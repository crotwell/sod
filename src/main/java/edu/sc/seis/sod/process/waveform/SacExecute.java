package edu.sc.seis.sod.process.waveform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.w3c.dom.Element;

import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.util.streampump.GenericCommandExecute;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

/**
 * @author crotwell Created on May 20, 2005
 */
public class SacExecute implements WaveformProcess {

    public SacExecute(Element el) throws Exception {
        application = DOMHelper.extractText(el, "application", "sac");
        commands = DOMHelper.extractText(el, "commands", "");
        prefix = DOMHelper.extractText(el, "prefix", "");
        writer = new PipedWriter(new PipedReader());
        stdout = new PipedWriter(new PipedReader());
        stderr = new PipedWriter(new PipedReader());
        Thread t = new Thread(new Runnable() {

            public void run() {
                int exitValue;
                try {
                    sacAlive = true;
                    exitValue = GenericCommandExecute.execute(application,
                                                              reader,
                                                              stdout,
                                                              stderr);
                    sacAlive = false;
                    logger.info("SacExecute exit value is: " + exitValue);
                } catch(Throwable e) {
                    GlobalExceptionHandler.handle(e);
                }
            }
        }, "SacExecute");
        t.setDaemon(true);
        t.start();
        expect("SAC>");
        ve = new VelocityEngine();
        Properties props = new Properties();
        SimpleVelocitizer.setupVelocityLogger(props, logger);
        ve.init(props);
    }

    /**
     * 
     */
    public WaveformResult accept(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        String files = "";
        for(int i = 0; i < seismograms.length; i++) {
            files += " "
                    + (String)cookieJar.get(AbstractSeismogramWriter.getCookieName(prefix,
                                                                               channel.get_id(),
                                                                               i));
        } // end of for (int i=0; i<seismograms.length; i++)
        VelocityContext context = new VelocityContext();
        context.put("files", files);
        StringWriter buffer = new StringWriter();
        ve.evaluate(context, buffer, "SacExecute", commands);
        BufferedReader readBuffer = new BufferedReader(new StringReader(buffer.getBuffer()
                .toString()));
        String line;
        while((line = readBuffer.readLine()) != null) {
            send(line.trim());
            expect("SAC>");
        }
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    private void expect(String response) throws IOException {
        System.err.println("Expecting: " + response);
        int index = stdoutBuffer.indexOf(response);
        char[] cbuf = new char[100];
        String buffer;
        PipedReader inReader;
        while(index == -1) {
            System.out.println("Waiting on read...");
            if(!sacAlive) {
                throw new IOException("Application " + application
                        + " has exited");
            }
            if(stdOutReader.ready()) {
                int numRead = stdOutReader.read(cbuf);
                stdoutBuffer += new String(cbuf, 0, numRead);
                System.err.println("Does '" + stdoutBuffer + "' match '"
                        + response + "'?");
                index = stdoutBuffer.indexOf(response);
                String prior = stdoutBuffer.substring(0, index
                        + response.length());
                System.out.println("Match: " + prior);
                stdoutBuffer = stdoutBuffer.substring(index + response.length());
                System.out.println("Buffer: " + stdoutBuffer);
            } else if(stdErrReader.ready()) {
                int numRead = stdErrReader.read(cbuf);
                stderrBuffer += new String(cbuf, 0, numRead);
                System.err.println("Does stderr '" + stderrBuffer + "' match '"
                        + response + "'?");
                index = stderrBuffer.indexOf(response);
                String prior = stderrBuffer.substring(0, index
                        + response.length());
                System.out.println("Match: " + prior);
                stderrBuffer = stderrBuffer.substring(index + response.length());
                System.out.println("Buffer: " + stderrBuffer);
            }
            if(index != -1) {
                System.err.println("Yes.");
            } else {
                System.err.println("No.");
            }
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {}
        }
    }

    private void send(String cmd) throws IOException {
        System.err.println("Sending: " + cmd);
        writer.write(cmd + "\n");
    }

    protected void finalize() throws Throwable {
        send("q");
        super.finalize();
    }

    protected VelocityEngine ve;

    private String application, commands, prefix;

    private GenericCommandExecute externalApp;

    private boolean sacAlive = false;

    PipedReader reader;

    PipedWriter writer;

    PipedWriter stdout, stderr;

    PipedReader stdOutReader;

    PipedReader stdErrReader;

    String stdoutBuffer = "";

    String stderrBuffer = "";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SacExecute.class);
}
