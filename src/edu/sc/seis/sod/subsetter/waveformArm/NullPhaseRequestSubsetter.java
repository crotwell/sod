package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


public class NullPhaseRequestSubsetter 
    
    implements PhaseRequestSubsetter {
    
 
    public boolean accept(CookieJar cookies) {
	return true;
    }

}// NullPhaseRequestSubsetter
