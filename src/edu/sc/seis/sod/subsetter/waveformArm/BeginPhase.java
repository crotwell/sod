package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

import org.w3c.dom.*;

/**
 * BeginPhase.java
 *
 *
 * Created: Tue Apr  9 10:19:32 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class BeginPhase implements PhaseRequestSubsetter{
    public BeginPhase (Element config){
	this.config = config;
    }

    public boolean accept(CookieJar cookies) {


	return true;
    }
    
    private Element config;
}// BeginPhase
