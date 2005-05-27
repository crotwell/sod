package edu.sc.seis.sod.process.waveform;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public class PrintlineSeismogramProcess implements WaveformProcess {

    public PrintlineSeismogramProcess(Element config) {
        filename = SodUtil.getNestedText(config);
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws IOException {
        String result = velocitizer.evaluate(template,
                                             event,
                                             channel,
                                             original,
                                             available,
                                             seismograms,
                                             cookieJar);
        if(filename != null && filename.length() != 0) {
            FileWriter fwriter = new FileWriter(filename, true);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            try {
                bwriter.write(result, 0, result.length());
                bwriter.newLine();
            } finally {
                bwriter.close();
            }
        } else {
            System.out.println(result);
        } // end of else
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    private SimpleVelocitizer velocitizer = new SimpleVelocitizer();

    private String template = "Got $seismograms.size() seismograms for $channel for $event";

    private String filename;
}// PrintlineWaveformProcessor
