package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * originXOR contains a sequence of originSubsetters. The minimum value of the sequence is 2 and
 * the max value of the sequence is 2.
 *      &lt;originXOR&gt;
 *		&lt;eventArea&gt;
 *		   &lt;boxArea&gt;
 *			&lt;latitudeRange&gt;
 *				&lt;min&gt;32&lt;/min&gt;
 *				&lt;max&gt;35.5&lt;/max&gt;
 *			&lt;/latitudeRange&gt;
 *			&lt;longitudeRange&gt;
 *				&lt;min&gt;-83.5&lt;/min&gt;
 *				&lt;max&gt;-78.5&lt;/max&gt;
 *			&lt;/longitudeRange&gt;
 *	           &lt;/boxArea&gt;
 *		&lt;/eventArea&gt;
 *		&lt;magnitudeRange&gt;
 *			&lt;magType&gt;mb&lt;/magType&gt;
 *			&lt;min&gt;3.5&lt;/min&gt;
 *		&lt;/magnitudeRange&gt;
 *      &lt;/originNOT&gt;
 */

/**
 * OriginXOR.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class OriginXOR
    extends EventLogicalSubsetter 
    implements OriginSubsetter {
    
    /**
     * Creates a new <code>OriginXOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public OriginXOR (Element config) throws ConfigurationException {
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param e an <code>Origin</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations event, Origin e,  CookieJar cookies) throws Exception{
	Iterator it = filterList.iterator();
	while (it.hasNext()) {
	    OriginSubsetter filter = (OriginSubsetter)it.next();
	    if ( !filter.accept(event, e, cookies)) {
		return false;
	    }
	}
	return true;
    }

}// OriginXOR
