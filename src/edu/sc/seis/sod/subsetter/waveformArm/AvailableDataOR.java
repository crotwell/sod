package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


public class AvailableDataOR 
    extends  WaveFormLogicalSubsetter 
    implements AvailableDataSubsetter {
    
    public AvailableDataOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(EventAccessOperations o, Station station,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	while (it.hasNext()) {
	    AvailableDataSubsetter filter = (AvailableDataSubsetter)it.next();
	    if (filter.accept(o, station, cookies)) {
		return true;
	    }
	}
	return false;
    }

}// AvailableDataOR

