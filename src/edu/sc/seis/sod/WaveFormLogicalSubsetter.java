package edu.sc.seis.sod;

import org.w3c.dom.*;

/**
 * WaveFormLogicalSubsetter.java
 *
 *
 * Created: Fri Mar 22 13:50:36 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class WaveFormLogicalSubsetter extends LogicalSubsetter{
    public WaveFormLogicalSubsetter (Element config) throws ConfigurationException{
	
	super(config);

    }

    public String getPackageName() {


	return "edu.sc.seis.sod.subsetter.waveFormArm";

    }
    
}// WaveFormLogicalSubsetter
