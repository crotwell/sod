package edu.sc.seis.sod;

import org.w3c.dom.Element;

public class WaveFormLogicalSubsetter extends LogicalSubsetter{
    public WaveFormLogicalSubsetter (Element config) throws ConfigurationException{
        super(config);
    }
    
    public String getArmName() { return "waveFormArm"; }
    
}// WaveFormLogicalSubsetter
