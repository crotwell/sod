package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * Contributor.java
 *
 *
 * Created: Tue Apr  2 13:34:59 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public class Contributor implements OriginSubsetter{
    /**
     * Creates a new <code>Contributor</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public Contributor (Element config){
	
	this.config = config;
    }
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param origin an <code>Origin</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations event, Origin origin, CookieJar cookies) {
	if(origin.contributor.equals(getContributor())) return true;
	
	return false;
    }


    public String getContributor() {

	return SodUtil.getNestedText(config);
    }

    private Element config;
  
}// Contributor
