package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.log4j.*;
import edu.iris.Fissures.*;

/**
 * EffectiveTimeOverlap.java
 *
 *
 * Created: Tue Mar 19 13:27:02 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public abstract class EffectiveTimeOverlap implements Subsetter{ 

    public EffectiveTimeOverlap() {

    }
    public EffectiveTimeOverlap (Element config){
	Element childElement = null;
	NodeList children = config.getChildNodes();
	Node node;
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    if(node instanceof Element) {
		String tagName = ((Element)node).getTagName();
		if(tagName.equals("effectiveTimeOverlap") ){
			childElement =(Element)node;
		}
	    }

	    logger.debug(node.getNodeName());
	}
	children = childElement.getChildNodes(); 
	for(int  i = 0; i < children.getLength(); i ++) {
		node = children.item(i); 
		if(node instanceof Element) {
			String tagName = ((Element)node).getTagName();
			if(tagName.equals("min")) minElement = (Element)node;
			else if(tagName.equals("max")) maxElement = (Element)node;
		}
	}
	if(minElement == null || maxElement == null) System.out.println("one of the min or max ELements is null");
    }

    public edu.iris.Fissures.Time getMinEffectiveTime() {
	if(minElement == null) return null;
	String effectiveTime = SodUtil.getNestedText(minElement);
	edu.iris.Fissures.Time rtnTime = new edu.iris.Fissures.Time(effectiveTime,0);
	System.out.println("The min time is "+effectiveTime);
	return rtnTime;
    }

    public edu.iris.Fissures.Time getMaxEffectiveTime() {
	if(maxElement == null) return null;
	String effectiveTime = SodUtil.getNestedText(maxElement);
	edu.iris.Fissures.Time rtnTime = new edu.iris.Fissures.Time(effectiveTime, 0);
	System.out.println("The max time is "+effectiveTime);
	return rtnTime;

    }

    Element minElement = null;

    Element maxElement = null;

    static Category logger = 
        Category.getInstance(EffectiveTimeOverlap.class.getName());

}// EffectiveTimeOverlap
