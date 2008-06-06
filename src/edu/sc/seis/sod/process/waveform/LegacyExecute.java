/**
 * LegacyExecute.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.process.waveform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.StreamPump;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class LegacyExecute implements WaveformProcess {

    public LegacyExecute(Element config) {
        Element prefixElement = SodUtil.getElement(config, "prefix");
        if(prefixElement != null) {
            String dssPrefix = SodUtil.getText(prefixElement);
            if(dssPrefix != null && dssPrefix.length() != 0) {
                prefix = dssPrefix;
            } // end of if (dataDirectory.exits())
        }
        Element cmdElement = SodUtil.getElement(config, "command");
        if(cmdElement != null) {
            String cmdString = SodUtil.getText(cmdElement);
            if(cmdString != null && cmdString.length() != 0) {
                command = cmdString;
            } // end of if (dataDirectory.exits())
        }
        Element workElement = SodUtil.getElement(config, "workingDirectory");
        if(workElement != null) {
            String workingDirectoryStr = SodUtil.getText(workElement);
            if(workingDirectoryStr != null && workingDirectoryStr.length() != 0) {
                workingDirectory = new File(workingDirectoryStr);
            } // end of if (dataDirectory.exits())
        }
    }

    /**
     * Removes the mean from the seismograms.
     */
    public WaveformResult process(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        System.arraycopy(seismograms, 0, out, 0, out.length);
        String args = command;
        for(int i = 0; i < seismograms.length; i++) {
            args += " "
                    + (String)cookieJar.get(SaveSeismogramToFile.getCookieName(prefix,
                                                                               channel.get_id(),
                                                                               i));
        } // end of for (int i=0; i<seismograms.length; i++)
        int exitValue = process(args);
        return new WaveformResult(out, new StringTreeLeaf(this,
                                                          exitValue == 0,
                                                          "exit value="
                                                                  + exitValue));
    }

    protected int process(String args) throws InterruptedException, IOException {
        if(workingDirectory != null) {
            workingDirectory.mkdirs();
        }
        Process process = Runtime.getRuntime().exec(args,
                                                    null,
                                                    workingDirectory);
        StreamPump outPump = new StreamPump(process.getInputStream(),
                                            new BufferedWriter(new PrintWriter(System.out)));
        StreamPump errPump = new StreamPump(process.getErrorStream(),
                                            new BufferedWriter(new PrintWriter(System.err)));
        outPump.start();
        errPump.start();
        int exitValue = process.waitFor();
        return exitValue;
    }

    protected String command;

    protected String prefix = "";

    protected File workingDirectory = null;
}