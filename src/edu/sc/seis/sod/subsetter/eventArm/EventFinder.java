package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventDCOperations;
import edu.sc.seis.fissuresUtil.cache.NSEventDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.AbstractSource;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    public EventFinder (Element config) throws Exception{
        super(config);
        processConfig(config);
        CommonAccess commonAccess = CommonAccess.getCommonAccess();
        fisName = commonAccess.getFissuresNamingService();
    }

    protected void processConfig(Element config) throws ConfigurationException{
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(!tagName.equals("name") && !tagName.equals("dns")) {
                    Object object = SodUtil.load((Element)node, "eventArm");
                    if(tagName.equals("originDepthRange")) {
                        depthRange = ((OriginDepthRange)object);
                    }else if(tagName.equals("eventTimeRange")) {
                        eventTimeRange = ((OriginTimeRange)object);
                    }else if(tagName.equals("magnitudeRange")) {
                        magnitudeRange = (MagnitudeRange)object;
                    }else if(object instanceof edu.iris.Fissures.Area){
                        area = (edu.iris.Fissures.Area)object;
                    }else if(tagName.equals("catalog")) {
                        catalogs.add(((Catalog)object).getCatalog());
                    }else if(tagName.equals("contributor")) {
                        contributors.add(((Contributor)object).getContributor());
                    }
                }
            }
        }
    }

    public EventDCOperations forceGetEventDC(){
        eventDC = getEventDC();
        eventDC.reset();
        return eventDC;
    }

    public NSEventDC getEventDC(){
        if (eventDC == null) {
            eventDC = new NSEventDC(getDNSName(), getSourceName(), fisName);
        }
        return eventDC;
    }

    public OriginDepthRange getDepthRange() { return depthRange; }

    public MagnitudeRange getMagnitudeRange() {  return magnitudeRange; }

    public edu.iris.Fissures.Area getArea() { return area; }

    public OriginTimeRange getEventTimeRange() {  return eventTimeRange; }

    public String[] getCatalogs() {
        return  (String[]) catalogs.toArray(new String[catalogs.size()]);
    }

    public String[] getContributors() {
        return  (String[])contributors.toArray(new String[contributors.size()]);
    }

    private FissuresNamingService fisName;

    private NSEventDC eventDC;

    private List catalogs = new ArrayList();

    private List contributors = new ArrayList();

    private OriginDepthRange depthRange;

    private MagnitudeRange magnitudeRange;

    private OriginTimeRange eventTimeRange;

    private edu.iris.Fissures.Area area;

    private static Logger logger = Logger.getLogger(EventFinder.class);
}// EventFinder
