package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;

/**
 * FullCoverage.java
 *
 *
 * Created: Tue Apr  9 10:27:12 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class FullCoverage implements AvailableDataSubsetter{
    public FullCoverage (Element config){
	
    }
    
    public boolean accept(EventAccessOperations eventAccess, Station station, CookieJar cookies) {
	return true;
    }
    
}// FullCoverage
