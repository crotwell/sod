package edu.sc.seis.sod.process.waveform;

import java.io.IOException;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public class PrintlineSeismogramProcess implements WaveformProcess {

    public PrintlineSeismogramProcess(Element config) {
        filename = DOMHelper.extractText(config, "filename", "");
        template = DOMHelper.extractText(config, "template", DEFAULT_TEMPLATE);
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws IOException {
        velocitizer.evaluate(filename,
                             template,
                             event,
                             channel,
                             original,
                             available,
                             seismograms,
                             cookieJar);
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    private SimpleVelocitizer velocitizer = new SimpleVelocitizer();

    private String template, filename;

    private static final String DEFAULT_TEMPLATE = "Got $seismograms.size() seismograms for $channel for $event";
}// PrintlineWaveformProcessor
