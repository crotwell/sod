package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * UnitRange.java
 *
 *
 * Created: Tue Apr  2 13:40:14 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public class UnitRange implements SodElement{
    public UnitRange (Element config){
	try {
	    processConfig(config);
	} catch(ConfigurationException ce) {

	    System.out.println("Configuration Exception caught in UnitRange");
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
		if(tagName.equals("unitRange")) unitRangeElement = (Element)node;
		
	    }

	}
	
	unitRange = (edu.iris.Fissures.UnitRange) SodUtil.load(unitRangeElement, "edu.sc.seis.sod.subsetter");
	
    }

    public edu.iris.Fissures.UnitRange  getUnitRange() {

	return unitRange;

    }

 
    private edu.iris.Fissures.UnitRange unitRange = null;
  
}// UnitRange
