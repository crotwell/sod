package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * OriginAND.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class OriginAND 
    extends EventLogicalSubsetter 
    implements OriginSubsetter {
    
    public OriginAND (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(Origin e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	while (it.hasNext()) {
	    OriginSubsetter filter = (OriginSubsetter)it.next();
	    if ( !filter.accept(e, cookies)) {
		return false;
	    }
	}
	return true;
    }

}// OriginAND
