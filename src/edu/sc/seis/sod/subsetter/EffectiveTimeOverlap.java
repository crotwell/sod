package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.log4j.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.model.*;

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

    public EffectiveTimeOverlap(edu.iris.Fissures.TimeRange range) {
        minDate = 
            new MicroSecondDate(range.start_time);
        maxDate = 
            new MicroSecondDate(range.end_time);

    }

    /**
     * Creates a new <code>EffectiveTimeOverlap</code> instance.
     *
     * @param config an <code>Element</code> value
     */
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

	}
	children = childElement.getChildNodes(); 
	for(int  i = 0; i < children.getLength(); i ++) {
		node = children.item(i); 
		if(node instanceof Element) {
			String tagName = ((Element)node).getTagName();
			if(tagName.equals("min")) {
			    minElement = (Element)node;
			    minDate = 
				new MicroSecondDate(getMinEffectiveTime());
			} else if (tagName.equals("max")) {
			    maxElement = (Element)node;
			    maxDate = 
				new MicroSecondDate(getMaxEffectiveTime());
			}
		}
	}
	
    }

    /**
     * Describe <code>getMinEffectiveTime</code> method here.
     *
     * @return an <code>edu.iris.Fissures.Time</code> value
     */
    public edu.iris.Fissures.Time getMinEffectiveTime() {
	if(minElement == null) return null;
	String effectiveTime = SodUtil.getNestedText(minElement);
	edu.iris.Fissures.Time rtnTime = new edu.iris.Fissures.Time(effectiveTime,0);
	return rtnTime;
    }

    /**
     * Describe <code>getMaxEffectiveTime</code> method here.
     *
     * @return an <code>edu.iris.Fissures.Time</code> value
     */
    public edu.iris.Fissures.Time getMaxEffectiveTime() {
	if(maxElement == null) return null;
	String effectiveTime = SodUtil.getNestedText(maxElement);
	edu.iris.Fissures.Time rtnTime = new edu.iris.Fissures.Time(effectiveTime, 0);
	return rtnTime;

    }

    public boolean overlaps(edu.iris.Fissures.TimeRange range) {
	MicroSecondDate rangeStartDate = 
	    new MicroSecondDate(range.start_time);
	MicroSecondDate rangeEndDate;

	if (range.end_time.date_time.equals(edu.iris.Fissures.TIME_UNKNOWN.value)) {
	    rangeEndDate = new MicroSecondDate(TimeUtils.future);
	} else {
	    rangeEndDate = 
		new MicroSecondDate(range.end_time);
	} // end of else
	
	if (maxDate == null && minDate == null) {
	    return true;
	} else if (maxDate == null && minDate.before(rangeEndDate)) {
	    return true;
	} else if (minDate == null && maxDate.after(rangeStartDate)) {
	    return true;
	} else if(rangeStartDate.after(maxDate) 
		  || rangeEndDate.before(minDate) ) {
	    logger.debug("EffectiveTimeOverlap false "+
			       rangeStartDate+" "+rangeEndDate+" "+minDate+" "+maxDate+"  "+range.end_time.date_time);
	    return false;
	} else {
	    //logger.debug(range.start_time.date_time+" "+range.end_time.date_time+" overlaps "+ minDate+" "+maxDate);
	    return true;
	}
    }

    Element minElement = null;
    MicroSecondDate minDate = null;

    Element maxElement = null;
    MicroSecondDate maxDate = null;

    static Category logger = 
        Category.getInstance(EffectiveTimeOverlap.class.getName());

}// EffectiveTimeOverlap
