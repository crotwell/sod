/**
 * PhaseSignalToNoise.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveformArm;


import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.LongShortTrigger;
import edu.sc.seis.fissuresUtil.bag.PhaseNonExistent;
import edu.sc.seis.fissuresUtil.bag.SimplePhaseStoN;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Calculates triggers, via LongShortSignalToNoise, and checks to see if a
 * trigger exists within +- the time interval for the given phase name. Uses the
 * first phase returned, ignoring later phases, such as triplications.
 *
 * The first trigger within the time window of the phase, if there is one, is
 * added to the cookieJar with key "sod_phaseStoN_"+phaseName for use by later
 * subsetters or later velocity output. */
public class PhaseSignalToNoise  implements LocalSeismogramProcess {

    public PhaseSignalToNoise(Element config) throws ConfigurationException, TauModelException{
        NodeList childNodes = config.getChildNodes();
        Node node;
        TimeInterval shortOffsetBegin=null, shortOffsetEnd=null;
        TimeInterval longOffsetBegin=null, longOffsetEnd=null;
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
        try {
            taupUtil = new TauPUtil(modelName);
        } catch (TauModelException e) {
            throw new ConfigurationException("Couldn't initialize travel time calculator", e);
        }

        phaseStoN = new SimplePhaseStoN(phaseName,
                                        shortOffsetBegin,
                                        shortOffsetEnd,
                                        longOffsetBegin,
                                        longOffsetEnd,
                                        modelName,
                                        taupUtil);
    }


    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar) throws Exception {
        if (seismograms.length == 0 ) {
            return new LocalSeismogramResult(seismograms, new StringTreeLeaf(this, false, "no seismograms"));
        }
        LongShortTrigger trigger = calcTrigger(event, channel, seismograms);
        if (trigger != null && trigger.getValue() > ratio) {
            cookieJar.put("sod_phaseStoN_"+phaseName, trigger);
            return new LocalSeismogramResult(seismograms, new StringTreeLeaf(this, true));
        }
        return new LocalSeismogramResult(seismograms, new StringTreeLeaf(this, false, "trigger="+trigger.getValue()+" < "+ratio));
    }

    /** This method exists to make the trigger available to other subsetters
     * or processors so they don't have to call accept, which adds it to the
     * cookieJar. */
    public LongShortTrigger calcTrigger(EventAccessOperations event,
                                        Channel channel,
                                        LocalSeismogramImpl[] seismograms) throws NoPreferredOrigin, FissuresException, PhaseNonExistent, TauModelException {
        // find the first seismogram with a non-null trigger, probably the first
        // that overlaps the timewindow, and return it.
        for (int i = 0; i < seismograms.length; i++) {
            LongShortTrigger trigger =
                phaseStoN.process(channel.my_site.my_location, event.get_preferred_origin(), seismograms[i]);
            if (trigger != null) { return trigger; }
        }
        return null;
    }

    public String getPhaseName() {
        return phaseName;
    }


    public String toString() {
        return "PhaseSignalToNoise("+getPhaseName()+")";
    }

    protected SimplePhaseStoN phaseStoN;

    protected float ratio = 1.0f;

    protected String phaseName;

    protected String modelName = "prem";

    protected TimeInterval triggerWindow;

    protected TauPUtil taupUtil;

}

