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
    public MagnitudeRange (Element config){
	super(config);
	try {
	    processConfig(config);
	} catch(ConfigurationException ce) {}
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

    public boolean accept(EventAccessOperations event, Origin origin, CookieJar cookies) {
	if(origin.magnitudes[0].value >= getMinMagnitude().value &&
	   origin.magnitudes[0].value <= getMaxMagnitude().value)
	    return true;
	else return false;

    }

    public Magnitude getMinMagnitude() {

	return new Magnitude(magType.getType(), getMinValue(), null);
	
    }
    
    public Magnitude getMaxMagnitude() {

	return new Magnitude(magType.getType(), getMaxValue(), null);

    }

    edu.sc.seis.sod.subsetter.MagType magType = null;
    
}// MagnitudeRange
