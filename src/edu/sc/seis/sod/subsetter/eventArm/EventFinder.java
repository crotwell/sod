package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventDCOperations;
import edu.iris.Fissures.IfEvent.NotFound;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.AbstractSource;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import java.util.ArrayList;
import org.apache.log4j.Category;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.sc.seis.fissuresUtil.cache.NSEventDC;

/**
 * This subsetter specifies the source of eventDC and the parameters required to query for events.
 *<pre>
 *  &lt;eventFinder&gt;
 *      &lt;name&gt;ANHINGAEVENTDC&lt;/name&gt;
 *      &lt;dns&gt;edu/sc/seis&lt;/dns&gt;
 *      &lt;globalArea/&gt;
 *      &lt;originDepthRange&gt;
 *          &lt;unitRange&gt;
 *              &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *              &lt;min&gt;0&lt;/min&gt;
 *              &lt;max&gt;1000&lt;/max&gt;
 *          &lt;/unitRange&gt;
 *      &lt;/originDepthRange&gt;
 *      &lt;eventTimeRange&gt;
 *      &lt;timeRange&gt;
 *          &lt;startTime&gt;1999-01-01T00:00:00Z&lt;/startTime&gt;
 *          &lt;endTime&gt;2003-01-01T00:00:00Z&lt;/endTime&gt;
 *      &lt;/timeRange&gt;
 *      &lt;/eventTimeRange&gt;
 *      &lt;catalog&gt;&lt;value&gt;BIGQUAKE&lt;/value&gt;&lt;/catalog&gt;
 *      &lt;contributor&gt;&lt;value&gt;NEIC&lt;/value&gt;&lt;/contributor&gt;
 * &lt;/eventFinder&gt;
 *
 *                      (or)
 *
 *  &lt;eventFinder&gt;
 *      &lt;name&gt;ANHINGAEVENTDC&lt;/name&gt;
 *      &lt;dns&gt;edu/sc/seis&lt;/dns&gt;
 *      &lt;globalArea/&gt;
 *      &lt;eventTimeRange&gt;
 *      &lt;timeRange&gt;
 *          &lt;startTime&gt;1999-01-01T00:00:00Z&lt;/startTime&gt;
 *          &lt;endTime&gt;2003-01-01T00:00:00Z&lt;/endTime&gt;
 *      &lt;/timeRange&gt;
 *      &lt;/eventTimeRange&gt;
 *      &lt;catalog&gt;&lt;value&gt;BIGQUAKE&lt;/value&gt;&lt;/catalog&gt;
 *      &lt;contributor&gt;&lt;value&gt;NEIC&lt;/value&gt;&lt;/contributor&gt;
 *  &lt;/eventFinder&gt;
 *
 *                                  (or)
 *
 *  &lt;eventFinder&gt;
 *      &lt;name&gt;ANHINGAEVENTDC&lt;/name&gt;
 *      &lt;dns&gt;edu/sc/seis&lt;/dns&gt;
 *      &lt;globalArea/&gt;
 *      &lt;originDepthRange&gt;
 *          &lt;unitRange&gt;
 *              &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *              &lt;min&gt;0&lt;/min&gt;
 *              &lt;max&gt;1000&lt;/max&gt;
 *          &lt;/unitRange&gt;
 *      &lt;/originDepthRange&gt;
 *      &lt;eventTimeRange&gt;
 *      &lt;timeRange&gt;
 *          &lt;startTime&gt;1999-01-01T00:00:00Z&lt;/startTime&gt;
 *          &lt;endTime&gt;2003-01-01T00:00:00Z&lt;/endTime&gt;
 *      &lt;/timeRange&gt;
 *      &lt;/eventTimeRange&gt;
 *  &lt;/eventFinder&gt;
 *</pre>
 */

public class EventFinder extends AbstractSource implements SodElement {
    /**
     * Creates a new <code>EventFinder</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public EventFinder (Element config) throws Exception{
        super(config);
        this.config = config;
        CommonAccess commonAccess = CommonAccess.getCommonAccess();
        processConfig();
        fissuresNamingService = commonAccess.getFissuresNamingService();
        
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
                    Object object = SodUtil.load((Element)node, "eventArm");
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
    public EventDCOperations forceGetEventDC()
        throws org.omg.CosNaming.NamingContextPackage.NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {
        eventDC = getEventDC();
        ((NSEventDC)eventDC).reset();
        return eventDC;
    }
    
    public EventDCOperations getEventDC()
        throws org.omg.CosNaming.NamingContextPackage.NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {
        if (eventDC == null) {
            eventDC = new NSEventDC(getDNSName(), getSourceName(), fissuresNamingService);
        }
        return eventDC;
    }
    
    
    /**
     * Describe <code>getDepthRange</code> method here.
     *
     * @return a <code>DepthRange</code> value
     */
    public OriginDepthRange getDepthRange() {
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
    
    
    private FissuresNamingService fissuresNamingService = null;
    
    private EventDCOperations eventDC = null;
    
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
