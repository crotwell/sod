package edu.sc.seis.sod.subsetter.waveform;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.waveform.vector.VectorWaveformSubsetter;
import org.w3c.dom.Element;

/**
 * PassLocalSeismogram.java Created: Fri Apr 12 13:41:05 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version
 */
public class PassWaveform implements WaveformSubsetter,
        VectorWaveformSubsetter {

    public PassWaveform() {}

    public PassWaveform(Element config) {}

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          LocalSeismogramImpl[] seismograms,
                          CookieJar cookieJar) throws Exception {
        return true;
    }

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channelGroup,
                          RequestFilter[][] original,
                          RequestFilter[][] available,
                          LocalSeismogramImpl[][] seismograms,
                          CookieJar cookieJar) throws Exception {
        return true;
    }
}// LocalSeismogram
