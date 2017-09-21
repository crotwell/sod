/**
 * PhaseSignalToNoise.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveform;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.bag.LongShortTrigger;
import edu.sc.seis.sod.bag.PhaseNonExistent;
import edu.sc.seis.sod.bag.SimplePhaseStoN;
import edu.sc.seis.sod.bag.TauPUtil;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.measure.ListMeasurement;
import edu.sc.seis.sod.measure.Measurement;
import edu.sc.seis.sod.measure.ScalarMeasurement;
import edu.sc.seis.sod.measure.TimeMeasurement;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.NoPreferredOrigin;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeLeaf;

/** Calculates triggers, via LongShortSignalToNoise, and checks to see if a
 * trigger exists within +- the time interval for the given phase name. Uses the
 * first phase returned, ignoring later phases, such as triplications.
 *
 * The first trigger within the time window of the phase, if there is one, is
 * added to the cookieJar with key "sod_phaseStoN_"+phaseName for use by later
 * subsetters or later velocity output. */
public class PhaseSignalToNoise  implements WaveformProcess, Threadable {

    
    public PhaseSignalToNoise(Element config) throws ConfigurationException, TauModelException{
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                Element element = (Element)node;
                if(element.getTagName().equals("phaseName")) {
                    phaseName = SodUtil.getNestedText(element);
                } else if(element.getTagName().equals("modelName")) {
                    modelName = SodUtil.getNestedText(element);
                }else if(element.getTagName().equals("ratio")) {
                    ratio = Float.parseFloat(SodUtil.getNestedText(element));
                } else if(element.getTagName().equals("shortOffsetBegin")) {
                    shortOffsetBegin = SodUtil.loadTimeInterval(element);
                } else if(element.getTagName().equals("shortOffsetEnd")) {
                    shortOffsetEnd = SodUtil.loadTimeInterval(element);
                } else if(element.getTagName().equals("longOffsetBegin")) {
                    longOffsetBegin = SodUtil.loadTimeInterval(element);
                } else if(element.getTagName().equals("longOffsetEnd")) {
                    longOffsetEnd = SodUtil.loadTimeInterval(element);
                }
            }
        }
        taupUtil = TauPUtil.getTauPUtil(modelName);

        phaseStoN = new SimplePhaseStoN(phaseName,
                                        shortOffsetBegin,
                                        shortOffsetEnd,
                                        longOffsetBegin,
                                        longOffsetEnd,
                                        taupUtil);
    }
    
    public boolean isThreadSafe(){
        return true;
    }

    public WaveformResult accept(CacheEvent event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         MeasurementStorage cookieJar) throws Exception {
        if (seismograms.length == 0 ) {
            return new WaveformResult(seismograms, new StringTreeLeaf(this, false, "no seismograms"));
        }
        try {
        LongShortTrigger trigger = calcTrigger(event, channel, seismograms);
        if (trigger != null) {
            if (trigger.getValue() > ratio) {
                JSONObject trig = new JSONObject();
                trig.put("when", trigger.getWhen().toString());
                trig.put("value", trigger.getValue());
                trig.put("sta", trigger.getSTA());
                trig.put("lta", trigger.getLTA());
                cookieJar.addMeasurement(getCookieName(), trig);
                return new WaveformResult(seismograms,
                                                 new StringTreeLeaf(this, true));
            }
            return new WaveformResult(seismograms,
                                             new StringTreeLeaf(this, false, phaseName+" trigger="+trigger.getValue()+" < "+ratio));
        } else {
            return new WaveformResult(seismograms,
                                             new StringTreeLeaf(this, false, "trigger is null"));
        }

        } catch (PhaseNonExistent e) {
            // no phase at this distance, just fail
            return new WaveformResult(seismograms,
                                      new StringTreeLeaf(this, false, "Phase does not exist"));
        }
    }

    /** This method exists to make the trigger available to other subsetters
     * or processors so they don't have to call accept, which adds it to the
     * cookieJar. */
    public LongShortTrigger calcTrigger(CacheEvent event,
                                        Channel channel,
                                        LocalSeismogramImpl[] seismograms) throws NoPreferredOrigin, FissuresException, PhaseNonExistent, TauModelException {
        // find the first seismogram with a non-null trigger, probably the first
        // that overlaps the timewindow, and return it.
        for (int i = 0; i < seismograms.length; i++) {
           LongShortTrigger trigger =
                phaseStoN.process(Location.of(channel), event.get_preferred_origin(), seismograms[i]);
            if (trigger != null) { return trigger; }
        }
        return null;
    }
    
    public String getCookieName() {
        return PHASE_STON_PREFIX+getPhaseName();
    }

    public String getPhaseName() {
        return phaseName;
    }


    public String toString() {
        return "PhaseSignalToNoise("+getPhaseName()+")";
    }

    public static final String PHASE_STON_PREFIX = "sod_phaseStoN_";

    protected SimplePhaseStoN phaseStoN;

    protected float ratio = 1.0f;

    protected String phaseName;

    protected Duration shortOffsetBegin, shortOffsetEnd;
    
    protected Duration longOffsetBegin, longOffsetEnd;
    
    protected String modelName = "prem";

    protected Duration triggerWindow;

    protected TauPUtil taupUtil;

}

