package edu.sc.seis.sod.subsetter.eventArm;


import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * MagnitudeRange.java
 *
 *
 * Created: Tue Apr  2 15:08:05 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public class MagnitudeRange extends RangeSubsetter implements OriginSubsetter{
    /**
     * Creates a new <code>MagnitudeRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public MagnitudeRange (Element config) throws ConfigurationException{
	super(config);
	    processConfig(config);
    }
    
    private void processConfig(Element config) throws ConfigurationException {
	
	NodeList childNodes = config.getChildNodes();
	Node node;
	for(int counter  = 0; counter < childNodes.getLength(); counter++) {
	    node = childNodes.item(counter);
	    if(node instanceof Element) {

		String tagName = ((Element)node).getTagName();
		if(tagName.equals("magType")) magType = (MagType)SodUtil.load((Element)node, "edu.sc.seis.sod.subsetter");
			
	    }
	}
	
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
	if(origin.magnitudes[0].value >= getMinMagnitude().value &&
	   origin.magnitudes[0].value <= getMaxMagnitude().value)
	    return true;
	else return false;

    }

    /**
     * Describe <code>getMinMagnitude</code> method here.
     *
     * @return a <code>Magnitude</code> value
     */
    public Magnitude getMinMagnitude() {

	return new Magnitude(magType.getType(), getMinValue(), null);
	
    }
    
    /**
     * Describe <code>getMaxMagnitude</code> method here.
     *
     * @return a <code>Magnitude</code> value
     */
    public Magnitude getMaxMagnitude() {

	return new Magnitude(magType.getType(), getMaxValue(), null);

    }

    edu.sc.seis.sod.subsetter.MagType magType = null;
    
}// MagnitudeRange
