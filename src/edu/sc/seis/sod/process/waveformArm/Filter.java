/**
 * Filter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.ButterworthFilter;
import edu.sc.seis.fissuresUtil.freq.SeisGramText;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Filter implements LocalSeismogramProcess {

    public Filter (Element config) throws ConfigurationException {
        this.config = config;
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
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

    public LocalSeismogramImpl[] process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms)
        throws Exception {
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
