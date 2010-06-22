package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.CompoundPhaseStoN;
import edu.sc.seis.fissuresUtil.bag.SimplePhaseStoN;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;


public class CompoundPhaseSignalToNoise extends PhaseSignalToNoise {

    public CompoundPhaseSignalToNoise(Element config) throws ConfigurationException, TauModelException {
        super(config);
        longPhaseName = SodUtil.getNestedText(SodUtil.getElement(config, "longPhaseName"));
        phaseStoN = new CompoundPhaseStoN(phaseName,
                                        shortOffsetBegin,
                                        shortOffsetEnd,
                                        longPhaseName,
                                        longOffsetBegin,
                                        longOffsetEnd,
                                        taupUtil);
    }
    
    public String getCookieName() {
        return super.getCookieName()+"_"+getLongPhaseName();
    }
    
    
    public String getLongPhaseName() {
        return longPhaseName;
    }

    protected String longPhaseName;
}
