package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * NetworkIDOR.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class NetworkIDOR 
    extends  NetworkLogicalSubsetter 
    implements NetworkIdSubsetter {
    
    public NetworkIDOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(NetworkId e,  CookieJar cookies) {
	System.out.println("THe networkID to be checked in NetworkIDOR is "+e.network_code);
	System.out.println("The size of the list is "+ filterList.size());
	Iterator it = filterList.iterator();
	while(it.hasNext()) {
	    System.out.println("In while loop in accept method of NetworkIDOR");
	    NetworkIdSubsetter filter = (NetworkIdSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return true;
	    }
	}
	return false;
    }

}// NetworkIDOR
