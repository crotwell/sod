/**
 * Filter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.ButterworthFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class Filter implements WaveformProcess, Threadable {

    @Deprecated
    public Filter(Element config) throws ConfigurationException {
        System.err.println("WARNING: <filter> is deprecated because of excessive memory and cpu usage, please switch to <oregonDSPFilter>");
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
                } else if(element.getTagName().equals("numPoles")) {
                    numPoles = Integer.parseInt(XMLUtil.getText(element));
                } else if(element.getTagName().equals("filterType")) {
                    if(XMLUtil.getText(element).equals("CAUSAL")) {
                        filterType = ButterworthFilter.CAUSAL;
                    } else {
                        filterType = ButterworthFilter.NONCAUSAL;
                    }
                }
            }
        }
        if(lowFreqCorner.get_unit().isConvertableTo(UnitImpl.SECOND)) {
            lowFreqCorner = lowFreqCorner.inverse();
        }
        if(highFreqCorner.get_unit().isConvertableTo(UnitImpl.SECOND)) {
            highFreqCorner = highFreqCorner.inverse();
        }
        filter = new ButterworthFilter(lowFreqCorner.convertTo(UnitImpl.HERTZ)
                                               .getValue(),
                                       highFreqCorner.convertTo(UnitImpl.HERTZ)
                                               .getValue(),
                                       numPoles,
                                       filterType);
    }

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult accept(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for(int i = 0; i < seismograms.length; i++) {
            out[i] = filter.apply(seismograms[i]);
        } // end of for (int i=0; i<seismograms.length; i++)
        return new WaveformResult(out, new StringTreeLeaf(this, true));
    }

    Element config;

    ButterworthFilter filter;

    QuantityImpl lowFreqCorner;

    QuantityImpl highFreqCorner;

    int numPoles = 2;

    int filterType = ButterworthFilter.NONCAUSAL;

    edu.sc.seis.fissuresUtil.bag.RMean rmean;
}
