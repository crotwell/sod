package edu.sc.seis.sod.subsetter.origin;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventDCOperations;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;
import edu.sc.seis.fissuresUtil.cache.ProxyEventDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.AbstractSource;

public class EventFinder extends AbstractSource implements SodElement {

    public EventFinder(Element config) throws Exception {
        super(config);
        processConfig(config);
        CommonAccess commonAccess = CommonAccess.getCommonAccess();
        fisName = commonAccess.getFissuresNamingService();
    }

    protected void processConfig(Element config) throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(!tagName.equals("name") && !tagName.equals("dns")) {
                    Object object = SodUtil.load((Element)node,
                                                 new String[] {"eventArm",
                                                               "origin"});
                    if(tagName.equals("originDepthRange")) {
                        depthRange = ((OriginDepthRange)object);
                    } else if(tagName.equals("originTimeRange")) {
                        eventTimeRange = ((OriginTimeRange)object);
                    } else if(tagName.equals("magnitudeRange")) {
                        magnitudeRange = (MagnitudeRange)object;
                    } else if(object instanceof edu.iris.Fissures.Area) {
                        area = (edu.iris.Fissures.Area)object;
                    } else if(tagName.equals("catalog")) {
                        catalogs.add(((Catalog)object).getCatalog());
                    } else if(tagName.equals("contributor")) {
                        contributors.add(((Contributor)object).getContributor());
                    }
                }
            }
        }
    }

    public EventDCOperations forceGetEventDC() {
        getEventDC().reset();
        return eventDC;
    }

    public ProxyEventDC getEventDC() {
        if(eventDC == null) {
            eventDC = BulletproofVestFactory.vestEventDC(getDNSName(),
                                                         getSourceName(),
                                                         fisName);
        }
        return eventDC;
    }

    public OriginDepthRange getDepthRange() {
        return depthRange;
    }

    public MagnitudeRange getMagnitudeRange() {
        return magnitudeRange;
    }

    public edu.iris.Fissures.Area getArea() {
        return area;
    }

    public OriginTimeRange getEventTimeRange() {
        return eventTimeRange;
    }

    public String[] getCatalogs() {
        return (String[])catalogs.toArray(new String[catalogs.size()]);
    }

    public String[] getContributors() {
        return (String[])contributors.toArray(new String[contributors.size()]);
    }

    private FissuresNamingService fisName;

    private ProxyEventDC eventDC;

    private List catalogs = new ArrayList();

    private List contributors = new ArrayList();

    private OriginDepthRange depthRange;

    private MagnitudeRange magnitudeRange;

    private OriginTimeRange eventTimeRange;

    private edu.iris.Fissures.Area area;

    private static Logger logger = Logger.getLogger(EventFinder.class);
}// EventFinder
