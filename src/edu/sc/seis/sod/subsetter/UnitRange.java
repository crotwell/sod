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
	Node node;
	for(int counter = 0; counter < childNodes.getLength(); counter++) {
									   
	    node = childNodes.item(counter);
	    if(node instanceof Element) {

		String tagName = ((Element)node).getTagName();
		if(tagName.equals("unit")) unit = (edu.sc.seis.sod.subsetter.Unit)SodUtil.load((Element)node,"edu.sc.seis.sod.subsetter");
		else if(tagName.equals("min")) min = Double.parseDouble(SodUtil.getNestedText((Element)node));
		else if(tagName.equals("max")) max = Double.parseDouble(SodUtil.getNestedText((Element)node));
		
	    }
	}
	
    }

    public edu.iris.Fissures.UnitRange  getUnitRange() {

	return new edu.iris.Fissures.model.UnitRangeImpl(min, max, unit.getUnit());

    }

   
    private double min;
    private double max;
    private edu.sc.seis.sod.subsetter.Unit unit = null;

}// UnitRange
