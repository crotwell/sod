/**
 * PhaseSignalToNoise.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.LongShortTrigger;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

/** Calculates triggers, via LongShortSignalToNoise, and checks to see if a
 * trigger exists within +- the time interval for the given phase name. Uses the
 * first phase returned, ignoring later phases, such as triplications.
 *
 * The first trigger within the time window of the phase, if there is one, is
 * added to the cookieJar with key "sod_phaseStoN_"+phaseName for use by later
 * subsetters or later velocity output. */
public class PhaseSignalToNoise extends LongShortSignalToNoise {

    public PhaseSignalToNoise(Element config) throws ConfigurationException{
        super(config);
        Element element = SodUtil.getElement(config,"modelName");
        if(element != null) modelName = SodUtil.getNestedText(element);
        phaseName = SodUtil.getNestedText(SodUtil.getElement(config, "phaseName"));
        triggerWindow = SodUtil.loadTimeInterval(SodUtil.getElement(config, "triggerWindow"));
        triggerWindow = (TimeInterval)triggerWindow.convertTo(UnitImpl.SECOND);
        try {
            taupUtil = new TauPUtil(modelName);
        } catch (TauModelException e) {
            throw new ConfigurationException("Couldn't initialize travel time calculator", e);
        }
    }

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          LocalSeismogramImpl[] seismograms,
                          CookieJar cookieJar) throws Exception {
        LongShortTrigger[] triggers = calcTriggers(seismograms);
        if (triggers.length != 0) {
            // don't waste time on time calc if no triggers
            Origin origin = event.get_preferred_origin();
            MicroSecondDate originTime = new MicroSecondDate(origin.origin_time);
            Arrival[] arrivals  = taupUtil.calcTravelTimes(channel.my_site.my_station, origin, new String[] {phaseName});
            if (arrivals.length == 0) { return false; }
            LongShortTrigger bestTrigger = null;
            for (int i = 0; i < triggers.length; i++) {
                double triggerSeconds =
                    triggers[i].getWhen().subtract(originTime).convertTo(UnitImpl.SECOND).get_value();
                if ( Math.abs(arrivals[0].getTime() - triggerSeconds) < triggerWindow.get_value() &&
                        (bestTrigger == null || triggers[i].getValue() > bestTrigger.getValue())) {
                    bestTrigger = triggers[i];
                }
                if (bestTrigger != null) {
                    cookieJar.put("sod_phaseStoN_"+phaseName, bestTrigger);
                    return true;
                }
            }
        }
        return false;
    }

    protected String phaseName;

    protected String modelName;

    protected TimeInterval triggerWindow;

    protected TauPUtil taupUtil;

}

