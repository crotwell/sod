package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * This tag is used to specify the value of the catalog.
 *<pre>
 * &lt;catalog&gt;&lt;value&gt;BIGQUAKE&lt;/value&gt;&lt;/catalog&gt;
 *</pre>
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
     * returns true if the catalog for the origin passed is same as the 
     * one specified in the corresponding catalog tag in the configuration file. 
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

    /**
     * Describe <code>getCatalog</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getCatalog() {

	return SodUtil.getNestedText(config);
    }

    private Element config;
  
}// Catalog
