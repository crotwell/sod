package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.fissuresUtil.namingService.*;

import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;
import org.apache.log4j.*;

/**
 * EventFinder.java
 *
 *
 * Created: Tue Mar 19 12:49:48 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EventFinder extends AbstractSource implements SodElement {
    /**
     * Creates a new <code>EventFinder</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public EventFinder (Element config){
	super(config);
	this.config = config;
	CommonAccess commonAccess = CommonAccess.getCommonAccess();
	if(commonAccess == null) System.out.println("THe common Acces is null");
	try {
	    processConfig();
	    fissuresNamingService = commonAccess.getFissuresNamingService();
	} catch(ConfigurationException ce) {
	    System.out.println("Configuration Exception caught in EventFinder");
	} catch(Exception e) {
	    System.out.println("Caught Exception in the constructor of EventFinder");
	}
       
    }
    
    /**
     * Describe <code>processConfig</code> method here.
     *
     * @exception ConfigurationException if an error occurs
     */
    protected void processConfig() throws ConfigurationException{
	
	NodeList childNodes = config.getChildNodes();
	Node node;
	for(int counter = 0; counter < childNodes.getLength(); counter++) {

	    node = childNodes.item(counter);
	    if(node instanceof Element) {

		String tagName = ((Element)node).getTagName();
		if(!tagName.equals("name") && !tagName.equals("dns") && !tagName.equals("catalog") && !tagName.equals("contributor")) {


		    Object object = SodUtil.load((Element)node, "edu.sc.seis.sod.subsetter.eventArm");
		    if(tagName.equals("depthRange")) depthRange = ((DepthRange)object);
		    else if(tagName.equals("eventTimeRange")) eventTimeRange = ((EventTimeRange)object);
		    else if(tagName.equals("magnitudeRange")) magnitudeRange = (MagnitudeRange)object;
		    else if(object instanceof edu.iris.Fissures.Area) area = (edu.iris.Fissures.Area)object;

		}  else if(tagName.equals("catalog")) catalog = SodUtil.getNestedText((Element)node);
		else if(tagName.equals("contributor")) contributor = SodUtil.getNestedText((Element)node);
		  

	    }

	}
	
    }

    /**
     * Describe <code>getEventDC</code> method here.
     *
     * @return an <code>EventDC</code> value
     */
    public EventDC getEventDC() {

	try {
	    return fissuresNamingService.getEventDC(getDNSName(), getSourceName());
	} catch(Exception e) {
	    System.out.println("Exception caught while obtaining the EventDC");
	}
	return null;
    }

    /**
     * Describe <code>getDepthRange</code> method here.
     *
     * @return a <code>DepthRange</code> value
     */
    public DepthRange getDepthRange() {

	return depthRange;

    }

    /**
     * Describe <code>getMagnitudeRange</code> method here.
     *
     * @return a <code>MagnitudeRange</code> value
     */
    public MagnitudeRange getMagnitudeRange() {

	return magnitudeRange;

    }
    
    /**
     * Describe <code>getArea</code> method here.
     *
     * @return an <code>edu.iris.Fissures.Area</code> value
     */
    public edu.iris.Fissures.Area getArea() {


	return area;
	
    }

    /**
     * Describe <code>getEventTimeRange</code> method here.
     *
     * @return an <code>EventTimeRange</code> value
     */
    public EventTimeRange getEventTimeRange() {

	return eventTimeRange;

    }

    /**
     * Describe <code>getCatalogs</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getCatalogs() {

	String[] rtnValues = new String[1];
	rtnValues[0] = catalog;
	return rtnValues;
    }

    /**
     * Describe <code>getContributors</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getContributors() {

	String[] rtnValues = new String[1];
	rtnValues[0] = contributor;
	return rtnValues;
    }


    private FissuresNamingServiceImpl fissuresNamingService = null;
			 
			   
    private Element config = null;

    private String catalog;

    private String contributor;

    private DepthRange depthRange;

    private MagnitudeRange magnitudeRange;

    private EventTimeRange eventTimeRange;
    
    private edu.iris.Fissures.Area area;

    static Category logger = 
        Category.getInstance(EventFinder.class.getName());

}// EventFinder
