package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * Catalog.java
 *
 *
 * Created: Tue Apr  2 13:34:59 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public class Catalog implements OriginSubsetter{
    /**
     * Creates a new <code>Catalog</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public Catalog (Element config){
	
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
	if(origin.catalog.equals(getCatalog())) return true;


	return false;
    }

    public String getCatalog() {

	return SodUtil.getNestedText(config);
    }

    private Element config;
  
}// Catalog
