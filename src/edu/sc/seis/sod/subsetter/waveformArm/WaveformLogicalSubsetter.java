package edu.sc.seis.sod.subsetter.waveformArm;
import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import org.w3c.dom.Element;

public class WaveformLogicalSubsetter extends LogicalSubsetter{
    public WaveformLogicalSubsetter (Element config) throws ConfigurationException{
        super(config);
    }
    
    public String getArmName() { return "waveformArm"; }
    
}// WaveFormLogicalSubsetter
