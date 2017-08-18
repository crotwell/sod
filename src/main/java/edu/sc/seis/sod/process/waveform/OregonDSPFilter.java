package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oregondsp.signalProcessing.filter.iir.Butterworth;
import com.oregondsp.signalProcessing.filter.iir.ChebyshevI;
import com.oregondsp.signalProcessing.filter.iir.ChebyshevII;
import com.oregondsp.signalProcessing.filter.iir.IIRFilter;
import com.oregondsp.signalProcessing.filter.iir.PassbandType;

import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.time.RangeTool;

public class OregonDSPFilter implements WaveformProcess {

    public OregonDSPFilter(String filterName,
                           PassbandType passband,
                           double epsilon,
                           QuantityImpl lowFreqCorner,
                           QuantityImpl highFreqCorner,
                           int numPoles,
                           int filterType) {
        super();
        this.filterName = filterName;
        this.passband = passband;
        this.epsilon = epsilon;
        setLowFreqCorner(lowFreqCorner);
        setHighFreqCorner(highFreqCorner);
        this.numPoles = numPoles;
        this.filterType = filterType;
    }

    public OregonDSPFilter(Element config) throws ConfigurationException {
        this.config = config;
        if (config != null) {
            // otherwise just use defaults
            NodeList childNodes = config.getChildNodes();
            for (int counter = 0; counter < childNodes.getLength(); counter++) {
                Node node = childNodes.item(counter);
                if (node instanceof Element) {
                    Element element = (Element)node;
                    if (element.getTagName().equals("lowFreqCorner")) {
                        setLowFreqCorner(SodUtil.loadQuantity(element));
                    } else if (element.getTagName().equals("highFreqCorner")) {
                        setHighFreqCorner(SodUtil.loadQuantity(element));
                    } else if (element.getTagName().equals("numPoles")) {
                        numPoles = Integer.parseInt(XMLUtil.getText(element));
                    } else if (element.getTagName().equals("epsilon")) {
                        epsilon = Double.parseDouble(XMLUtil.getText(element));
                    } else if (element.getTagName().equals("filterType")) {
                        if (XMLUtil.getText(element).equals("CAUSAL")) {
                            filterType = CAUSAL;
                        } else {
                            filterType = NONCAUSAL;
                            throw new ConfigurationException("Noncausal filter not yet implemented");
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
                        passband = PassbandType.LOWPASS;
                    } else if (element.getTagName().equals("highpass")) {
                        passband = PassbandType.HIGHPASS;
                    }
                }
            }
        }
    }
    
    /** for use in Decimate where we know passband is lowpass and corner freq is new nyquist but want to configure other
     * settings.
     * @param config
     * @param passband
     * @param lowFreqCorner
     * @param highFreqCorner
     * @throws ConfigurationException
     */
    public OregonDSPFilter(Element config, PassbandType passband, QuantityImpl lowFreqCorner, QuantityImpl highFreqCorner) throws ConfigurationException {
        this(config);
        this.passband = passband;
        this.lowFreqCorner = lowFreqCorner;
        this.highFreqCorner = highFreqCorner;
    }

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult accept(CacheEvent event,
                                 Channel channel,
                                 RequestFilter[] original,
                                 RequestFilter[] available,
                                 LocalSeismogramImpl[] seismograms,
                                 CookieJar cookieJar) throws Exception {
        return apply(seismograms);
    }

    public WaveformResult apply(LocalSeismogramImpl[] seismograms) throws FissuresException {
        IIRFilter filter = null;
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for (int i = 0; i < seismograms.length; i++) {
            if (filter == null) {
                filter = createFilter(seismograms[0].getSampling().getPeriod().getValue(UnitImpl.SECOND));
            }
            float[] data = seismograms[i].get_as_floats();
            filter.filter(data);
            out[i] = new LocalSeismogramImpl(seismograms[i], data);
            if (i < seismograms.length - 1 && !RangeTool.areContiguous(seismograms[i], seismograms[i + 1])) {
                filter = null; // filter preserves state for filtering
                               // contiguous waveforms, reset if a gap
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

    public String getFilterName() {
        return filterName;
    }

    public PassbandType getPassband() {
        return passband;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public QuantityImpl getLowFreqCorner() {
        return lowFreqCorner;
    }
    
    protected void setLowFreqCorner(QuantityImpl lowFreqCorner) {
        if (lowFreqCorner != null && lowFreqCorner.get_unit().isConvertableTo(UnitImpl.SECOND)) {
            this.lowFreqCorner = lowFreqCorner.inverse();
        } else if (lowFreqCorner != null && lowFreqCorner.get_unit().isConvertableTo(UnitImpl.HERTZ)) {
            this.lowFreqCorner = lowFreqCorner;
        } else {
            throw new IllegalArgumentException("Corner freq must be convertible to SECONDS or HERTZ, but was: "+lowFreqCorner.get_unit());
        }
    }

    public QuantityImpl getHighFreqCorner() {
        return highFreqCorner;
    }
    
    protected void setHighFreqCorner(QuantityImpl highFreqCorner) {
        if (highFreqCorner != null && highFreqCorner.get_unit().isConvertableTo(UnitImpl.SECOND)) {
            this.highFreqCorner = highFreqCorner.inverse();
        } else if (highFreqCorner != null && highFreqCorner.get_unit().isConvertableTo(UnitImpl.HERTZ)) {
            this.highFreqCorner = highFreqCorner;
        } else {
            throw new IllegalArgumentException("Corner freq must be convertible to SECONDS or HERTZ, but was: "+highFreqCorner.get_unit());
        }
    }

    public int getNumPoles() {
        return numPoles;
    }

    public int getFilterType() {
        return filterType;
    }

    Element config;

    String filterName = "butterworth";

    PassbandType passband = PassbandType.BANDPASS;

    double epsilon = 1;

    QuantityImpl lowFreqCorner = new QuantityImpl(1e-99, UnitImpl.HERTZ);

    QuantityImpl highFreqCorner = new QuantityImpl(1e99, UnitImpl.HERTZ);

    int numPoles = 2;

    int filterType = NONCAUSAL;
    
    public static final int CAUSAL = 0;
    
    public static final int NONCAUSAL = 1;

}
