package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.log4j.*;
import edu.iris.Fissures.*;

/**
 * TimeRange.java
 *
 *
 * Created: Tue Mar 19 13:27:02 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public abstract class TimeRange implements Subsetter{ 

    /**
     * Creates a new <code>TimeRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public TimeRange (Element config){
	Element childElement = null;
	NodeList children = config.getChildNodes();
	Node node;
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    if(node instanceof Element) {
		String tagName = ((Element)node).getTagName();
		if(tagName.equals("timeRange") ){
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
			if(tagName.equals("startTime")) minElement = (Element)node;
			else if(tagName.equals("endTime")) maxElement = (Element)node;
		}
	}
	if(minElement == null || maxElement == null) System.out.println("one of the min or max ELements is null");
    }

    /**
     * Describe <code>getStartTime</code> method here.
     *
     * @return an <code>edu.iris.Fissures.Time</code> value
     */
    public edu.iris.Fissures.Time getStartTime() {
	if(minElement == null) return null;
	String effectiveTime = SodUtil.getNestedText(minElement);
	edu.iris.Fissures.Time rtnTime = new edu.iris.Fissures.Time(effectiveTime,0);
	System.out.println("The min time is "+effectiveTime);
	return rtnTime;
    }

    /**
     * Describe <code>getEndTime</code> method here.
     *
     * @return an <code>edu.iris.Fissures.Time</code> value
     */
    public edu.iris.Fissures.Time getEndTime() {
	if(maxElement == null) return null;
	String effectiveTime = SodUtil.getNestedText(maxElement);
	edu.iris.Fissures.Time rtnTime = new edu.iris.Fissures.Time(effectiveTime, 0);
	System.out.println("The max time is "+effectiveTime);
	return rtnTime;

    }

    /**
     * Describe <code>getTimeRange</code> method here.
     *
     * @return an <code>edu.iris.Fissures.TimeRange</code> value
     */
    public edu.iris.Fissures.TimeRange getTimeRange() {

	return new edu.iris.Fissures.TimeRange(getStartTime(), getEndTime());

    }

    Element minElement = null;

    Element maxElement = null;

    static Category logger = 
        Category.getInstance(TimeRange.class.getName());

}// TimeRange
