package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oregondsp.signalProcessing.filter.iir.Butterworth;
import com.oregondsp.signalProcessing.filter.iir.ChebyshevI;
import com.oregondsp.signalProcessing.filter.iir.ChebyshevII;
import com.oregondsp.signalProcessing.filter.iir.IIRFilter;
import com.oregondsp.signalProcessing.filter.iir.PassbandType;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.ButterworthFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.time.RangeTool;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class OregonDSPFilter implements WaveformProcess {

    public OregonDSPFilter(Element config) throws ConfigurationException {
        this.config = config;
        NodeList childNodes = config.getChildNodes();
        for (int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if (node instanceof Element) {
                Element element = (Element)node;
                if (element.getTagName().equals("lowFreqCorner")) {
                    lowFreqCorner = SodUtil.loadQuantity(element);
                } else if (element.getTagName().equals("highFreqCorner")) {
                    highFreqCorner = SodUtil.loadQuantity(element);
                } else if (element.getTagName().equals("numPoles")) {
                    numPoles = Integer.parseInt(XMLUtil.getText(element));
                } else if (element.getTagName().equals("epsilon")) {
                    epsilon = Double.parseDouble(XMLUtil.getText(element));
                } else if (element.getTagName().equals("filterType")) {
                    if (XMLUtil.getText(element).equals("CAUSAL")) {
                        filterType = ButterworthFilter.CAUSAL;
                    } else {
                        filterType = ButterworthFilter.NONCAUSAL;
                    }
                } else if (element.getTagName().equals("butterworth")) {
                    filterName = element.getTagName();
                } else if (element.getTagName().equals("chebyshevI")) {
                    filterName = element.getTagName();
                } else if (element.getTagName().equals("chebyshevII")) {
                    filterName = element.getTagName();
                } else if (element.getTagName().equals("bandpass")) {
                    passband = PassbandType.BANDPASS;
                } else if (element.getTagName().equals("lowpass")) {
                    filterName = element.getTagName();
                } else if (element.getTagName().equals("highpass")) {
                    passband = PassbandType.HIGHPASS;
                }
            }
        }
        if (lowFreqCorner.get_unit().isConvertableTo(UnitImpl.SECOND)) {
            lowFreqCorner = lowFreqCorner.inverse();
        }
        if (highFreqCorner.get_unit().isConvertableTo(UnitImpl.SECOND)) {
            highFreqCorner = highFreqCorner.inverse();
        }
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
        IIRFilter filter = null;
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for (int i = 0; i < seismograms.length; i++) {
            if (filter == null) {
                filter = createFilter(seismograms[0].getSampling().getPeriod().getValue(UnitImpl.SECOND));
            }
            float[] data = seismograms[i].get_as_floats();
            filter.filter(data);
            out[i] = new LocalSeismogramImpl(seismograms[i], data);
            if (i < seismograms.length-1 && ! RangeTool.areContiguous(seismograms[i], seismograms[i+1])) {
                filter = null; // filter preserves state for filtering contiguous waveforms, reset if a gap 
            }
        }
        return new WaveformResult(out, new StringTreeLeaf(this, true));
    }

    IIRFilter createFilter(double delta) {
        IIRFilter filter = null;
        if (filterName.equals("chebyshevI")) {
            filter = new ChebyshevI(numPoles,
                                    epsilon,
                                     passband,
                                     lowFreqCorner.getValue(UnitImpl.HERTZ),
                                     highFreqCorner.getValue(UnitImpl.HERTZ),
                                     delta);
        } else if (filterName.equals("chebyshevII")) {
            filter = new ChebyshevII(numPoles,
                                     epsilon,
                                     passband,
                                     lowFreqCorner.getValue(UnitImpl.HERTZ),
                                     highFreqCorner.getValue(UnitImpl.HERTZ),
                                     delta);
        } else {
            // butterworth is default
            filter = new Butterworth(numPoles,
                                     passband,
                                     lowFreqCorner.getValue(UnitImpl.HERTZ),
                                     highFreqCorner.getValue(UnitImpl.HERTZ),
                                     delta);
        }
        return filter;
    }

    Element config;

    String filterName = "butterworth";

    PassbandType passband = PassbandType.BANDPASS;
    
    double epsilon = 1;

    QuantityImpl lowFreqCorner;

    QuantityImpl highFreqCorner;

    int numPoles = 2;

    int filterType = ButterworthFilter.NONCAUSAL;

    edu.sc.seis.fissuresUtil.bag.RMean rmean;
}
