/**
 * Filter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.ButterworthFilter;
import edu.sc.seis.fissuresUtil.freq.SeisGramText;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Filter implements LocalSeismogramProcess {

    /**
     * Creates a new <code>Filter</code> instance.
     *
     * @param config an <code>Element</code> that contains the configuration
     * for this Processor
     */
    public Filter (Element config) throws ConfigurationException {
        this.config = config;
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                Element element = (Element)node;
                if(element.getTagName().equals("lowFreqCorner")) {
                    lowFreqCorner = SodUtil.loadQuantity(element);
                } else if(element.getTagName().equals("highFreqCorner")) {
                    highFreqCorner = SodUtil.loadQuantity(element);
                } else if (element.getTagName().equals("numPoles")) {
                    numPoles = Integer.parseInt(XMLUtil.getText(element));
                } else if (element.getTagName().equals("filterType")) {
                    if (XMLUtil.getText(element).equals("CAUSAL")) {
                        filterType = ButterworthFilter.CAUSAL;
                    } else {
                        filterType = ButterworthFilter.NONCAUSAL;
                    }
                }
            }
        }
        if (lowFreqCorner.get_unit().isConvertableTo(UnitImpl.SECOND)) {
            lowFreqCorner = lowFreqCorner.inverse();
        }
        if (highFreqCorner.get_unit().isConvertableTo(UnitImpl.SECOND)) {
            highFreqCorner = highFreqCorner.inverse();
        }
        filter = new ButterworthFilter(new SeisGramText(),
                                       lowFreqCorner.convertTo(UnitImpl.HERTZ).getValue(),
                                       highFreqCorner.convertTo(UnitImpl.HERTZ).getValue(),
                                       numPoles,
                                       filterType);
    }

    /**
     * Filters the seismograms.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param seismograms a <code>LocalSeismogram[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @exception Exception if an error occurs
     */
    public LocalSeismogramImpl[] process(EventAccessOperations event,
                                         NetworkAccess network,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookies) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for (int i=0; i<seismograms.length; i++) {
            out[i] = filter.apply(seismograms[i]);
        } // end of for (int i=0; i<seismograms.length; i++)
        return out;
    }

    Element config;

    ButterworthFilter filter;

    QuantityImpl lowFreqCorner;

    QuantityImpl highFreqCorner;

    int numPoles = 2;

    int filterType = ButterworthFilter.NONCAUSAL;

    edu.sc.seis.fissuresUtil.bag.RMean rmean;

}

