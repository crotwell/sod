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
	catalogs = new ArrayList();
	contributors = new ArrayList();
	for(int counter = 0; counter < childNodes.getLength(); counter++) {

	    node = childNodes.item(counter);
	    if(node instanceof Element) {

		String tagName = ((Element)node).getTagName();
		if(!tagName.equals("name") && !tagName.equals("dns")) {


		    Object object = SodUtil.load((Element)node, "edu.sc.seis.sod.subsetter.eventArm");
		    if(tagName.equals("originDepthRange")) depthRange = ((OriginDepthRange)object);
		    else if(tagName.equals("eventTimeRange")) eventTimeRange = ((EventTimeRange)object);
		    else if(tagName.equals("magnitudeRange")) magnitudeRange = (MagnitudeRange)object;
		    else if(object instanceof edu.iris.Fissures.Area) area = (edu.iris.Fissures.Area)object;
		    else if(tagName.equals("catalog")) {
			catalog = (Catalog)object;
			catalogs.add(catalog.getCatalog());
		    }
		    else if(tagName.equals("contributor")) {
			contributor = (Contributor)object;
			contributors.add(contributor.getContributor());
		    }
		} 
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
    public OriginDepthRange getDepthRange() {
	if(depthRange == null) System.out.println("Depth range is NULL");
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

	System.out.println("returning Area");
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

	String[] rtnValues = new String[catalogs.size()];
	rtnValues = (String[]) catalogs.toArray(rtnValues);
	return rtnValues;
    }

    /**
     * Describe <code>getContributors</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getContributors() {

	String[] rtnValues = new String[contributors.size()];
	rtnValues = (String[])contributors.toArray(rtnValues);
	return rtnValues;
    }


    private FissuresNamingServiceImpl fissuresNamingService = null;
			 
			   
    private Element config = null;

    private Catalog catalog;

    private Contributor contributor;

    private ArrayList catalogs;

    private ArrayList contributors;

    private OriginDepthRange depthRange;

    private MagnitudeRange magnitudeRange;

    private EventTimeRange eventTimeRange;
    
    private edu.iris.Fissures.Area area;

    static Category logger = 
        Category.getInstance(EventFinder.class.getName());

}// EventFinder
