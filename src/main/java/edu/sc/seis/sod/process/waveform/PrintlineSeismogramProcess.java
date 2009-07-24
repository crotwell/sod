package edu.sc.seis.sod.process.waveform;

import java.io.IOException;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

public class PrintlineSeismogramProcess implements WaveformProcess {

    public PrintlineSeismogramProcess(Element config) throws ConfigurationException {
        filename = DOMHelper.extractText(config, "filename", "");
        template = DOMHelper.extractText(config, "template", DEFAULT_TEMPLATE);
        velocitizer = new PrintlineVelocitizer(new String[] {filename, template});
    }

    public WaveformResult process(CacheEvent event,
                                  ChannelImpl channel,
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

    private PrintlineVelocitizer velocitizer;

    private String template, filename;

    public static final String DEFAULT_TEMPLATE = "Got $seismograms.size() seismograms for $channel.toString() for eq on $event.time";
}// PrintlineWaveformProcessor
