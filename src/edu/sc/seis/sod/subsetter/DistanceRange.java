package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * DistanceRange.java
 *
 *
 * Created: Mon Apr  8 16:09:49 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DistanceRange implements SodElement{

     public DistanceRange (Element config){
	try {
	    processConfig(config);
	} catch(ConfigurationException ce) {

	    System.out.println("Configuration Exception caught in DistanceRange");
	}
	
    }
    
    public void processConfig(Element config) throws ConfigurationException{
	
	NodeList childNodes = config.getChildNodes();
	Element unitRangeElement = null;
	Node node;
	for(int counter = 0; counter < childNodes.getLength(); counter++) {

	    node = childNodes.item(counter);
	    if(node instanceof Element) {
		
		String tagName = ((Element)node).getTagName();
		if(tagName.equals("distanceRange")) unitRangeElement = (Element)node;
		
	    }

	}
	
	unitRange = (edu.iris.Fissures.UnitRange) SodUtil.load(unitRangeElement, "edu.sc.seis.sod.subsetter");
	
    }

    public edu.iris.Fissures.UnitRange  getDistanceRange() {

	return unitRange;

    }
 
    public Quantity getMinDistance() {

	return new QuantityImpl(getDistanceRange().min_value, getDistanceRange().the_units);

    }


    public Quantity getMaxDistance() {
	
	return new QuantityImpl(getDistanceRange().max_value, getDistanceRange().the_units);
    }
    
    private edu.iris.Fissures.UnitRange unitRange = null;
        
}// DistanceRange
