package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * EventAttrName.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EventAttrName implements EventAttrSubsetter {
    
    /**
     * Creates a new <code>EventAttrName</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public EventAttrName (Element config) throws ConfigurationException {
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param e an <code>EventAttr</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAttr e,  CookieJar cookies) {

	return true;
    }

}// EventAttrName
