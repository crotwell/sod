/**
 * PhaseSignalToNoiseCalculator.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.LongShortTrigger;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.waveformArm.PhaseSignalToNoise;
import org.apache.log4j.Category;
import org.w3c.dom.Element;

public class PhaseSignalToNoiseCalculator  implements LocalSeismogramProcess, ChannelGroupLocalSeismogramProcess {

    /**
     * Creates a new <code>PhaseSignalToNoiseCalculator</code> instance.
     *
     * @param config an <code>Element</code> that contains the configuration
     * for this Processor
     */
    public PhaseSignalToNoiseCalculator (Element config) throws ConfigurationException, TauModelException {
        this.config = config;
        // use existing PhaseRequest class to calculate phase times
        phaseSToN = new PhaseSignalToNoise(config);
    }

    public LocalSeismogramImpl[] process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar) throws Exception {
        // this has the side effect of putting the trigger in the cookiejar
        boolean notUsed = phaseSToN.accept(event, channel, original, available, seismograms, cookieJar);
        return seismograms;
    }

    public LocalSeismogramImpl[][] process(EventAccessOperations event,
                                           ChannelGroup channelGroup,
                                           RequestFilter[][] original,
                                           RequestFilter[][] available,
                                           LocalSeismogramImpl[][] seismograms,
                                           CookieJar cookieJar) throws Exception {
        for (int i = 0; i < channelGroup.getChannels().length; i++) {
            LongShortTrigger trigger = phaseSToN.calcTrigger(event,
                                                             channelGroup.getChannels()[i],
                                                             seismograms[i]);
            if (trigger != null) {
                cookieJar.put("sod_phaseStoN_"+phaseSToN.getPhaseName()+"_"+channelGroup.getChannels()[i].get_code(),
                              trigger);
            }
        }
        return seismograms;
    }

    Element config;

    PhaseSignalToNoise phaseSToN;

    private static Category logger =
        Category.getInstance(PhaseSignalToNoiseCalculator.class.getName());

}

