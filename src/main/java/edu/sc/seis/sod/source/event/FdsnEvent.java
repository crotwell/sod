package edu.sc.seis.sod.source.event;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.FlinnEngdahlType;
import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.PointDistanceArea;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.FlinnEngdahlRegionImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.seisFile.SeisFileException;
import edu.sc.seis.seisFile.fdsnws.FDSNEventQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNEventQueryParams;
import edu.sc.seis.seisFile.fdsnws.quakeml.Event;
import edu.sc.seis.seisFile.fdsnws.quakeml.EventDescription;
import edu.sc.seis.seisFile.fdsnws.quakeml.EventIterator;
import edu.sc.seis.seisFile.fdsnws.quakeml.Magnitude;
import edu.sc.seis.seisFile.fdsnws.quakeml.Origin;
import edu.sc.seis.seisFile.fdsnws.quakeml.Quakeml;
import edu.sc.seis.sod.BuildVersion;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.network.AbstractNetworkSource;
import edu.sc.seis.sod.subsetter.DepthRange;
import edu.sc.seis.sod.subsetter.origin.Catalog;
import edu.sc.seis.sod.subsetter.origin.Contributor;
import edu.sc.seis.sod.subsetter.origin.MagnitudeRange;
import edu.sc.seis.sod.subsetter.origin.OriginDepthRange;


public class FdsnEvent extends AbstractEventSource implements EventSource {
    
    public FdsnEvent(Element config) throws ConfigurationException {
        super(config, "DefaultFDSNEvent");
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(!tagName.equals("retries")) {
                    Object object = SodUtil.load((Element)node,
                                                 new String[] {"eventArm",
                                                               "origin"});

                    if(tagName.equals("originTimeRange")) {
                        eventTimeRangeSupplier = ((MicroSecondTimeRangeSupplier) SodUtil.load((Element)node, new String[] {
                            "eventArm", "origin" }));
                        
                    } else if(tagName.equals("originDepthRange")) {
                        DepthRange dr = ((OriginDepthRange)object);
                        if (dr.getMinDepth().getValue(UnitImpl.KILOMETER) > -99999) {
                            queryParams.setMinDepth((float)dr.getMinDepth().getValue(UnitImpl.KILOMETER));
                        }
                        if (dr.getMaxDepth().getValue(UnitImpl.KILOMETER) < 99999) {
                            queryParams.setMaxDepth((float)dr.getMaxDepth().getValue(UnitImpl.KILOMETER));
                        }
                    } else if(tagName.equals("magnitudeRange")) {
                        MagnitudeRange magRange = (MagnitudeRange)object;
                        String[] magTypes = magRange.getSearchTypes();
                        // fdsn web services don't support multiple mag types, but we append them comma 
                        // separated just in case they do one day. Sod will likely get an error back
                        String magStr = "";
                        for (int i = 0; i < magTypes.length; i++) {
                            magStr += magTypes[i];
                            if (i < magTypes.length-1) {
                                magStr += ",";
                            }
                        }
                        if (magStr.length() != 0) {
                            queryParams.setMagnitudeType(magStr);
                        }
                        if (magRange.getMinValue() > -99) {
                            queryParams.setMinMagnitude((float)magRange.getMinValue());
                        }
                        if (magRange.getMaxValue() < 99) {
                            queryParams.setMaxMagnitude((float)magRange.getMaxValue());
                        }
                    } else if(object instanceof edu.iris.Fissures.Area) {
                        edu.iris.Fissures.Area area = (edu.iris.Fissures.Area)object;
                        if (area instanceof GlobalArea) {
                            // nothing needed
                        } else if (area instanceof BoxArea) {
                            BoxArea box = (BoxArea)area;
                            queryParams.area(box.min_latitude, box.max_latitude, box.min_longitude, box.max_longitude);
                        } else if (area instanceof PointDistanceArea) {
                            PointDistanceArea donut = (PointDistanceArea)area;
                            queryParams.donut(donut.latitude,
                                              donut.longitude,
                                              (float)((QuantityImpl)donut.min_distance).getValue(UnitImpl.DEGREE),
                                              (float)((QuantityImpl)donut.max_distance).getValue(UnitImpl.DEGREE));
                        } else {
                            throw new ConfigurationException("Area of class "+area.getClass().getName()+" not understood");
                        }
                    } else if(tagName.equals("catalog")) {
                        queryParams.setCatalog(((Catalog)object).getCatalog());
                    } else if(tagName.equals("contributor")) {
                        queryParams.setContributor(((Contributor)object).getContributor());
                    }
                }
            }
        }
        if(DOMHelper.hasElement(config, AbstractNetworkSource.REFRESH_ELEMENT)) {
            refreshInterval = SodUtil.loadTimeInterval(SodUtil.getElement(config, AbstractNetworkSource.REFRESH_ELEMENT));
        } else {
            refreshInterval = new TimeInterval(1, UnitImpl.FORTNIGHT);
        }
    }

    @Override
    public boolean hasNext() {
        MicroSecondDate queryEnd = getEventTimeRange().getEndTime();
        MicroSecondDate quitDate = queryEnd.add(lag);
        logger
                .debug(getName()+" Checking if more queries to the event server are in order.  The quit date is "
                        + quitDate
                        + " the last query was for "
                        + getQueryStart()
                        + " and we're querying to "
                        + queryEnd);
        return  quitDate.equals(ClockUtil.now())
            || quitDate.after(ClockUtil.now())
            || !getQueryStart().equals(queryEnd);
    }



    @Override
    public CacheEvent[] next() {
        try {
            List<CacheEvent> out = new ArrayList<CacheEvent>();
            EventIterator it = getIterator();
            while (it.hasNext()) {
                Event e = it.next();
                out.add(toCacheEvent(e));
            }
            return out.toArray(new CacheEvent[0]);
        } catch(Exception e) {
            throw new RuntimeException(e); // ToDo: fix this
        }
    }

    @Override
    public TimeInterval getWaitBeforeNext() {
        if (queryTime == null) {
            return new TimeInterval(0, UnitImpl.SECOND);
        } else {
            return queryTime.add(refreshInterval).subtract(ClockUtil.now());
        }
    }

    @Override

    public MicroSecondTimeRange getEventTimeRange() {
        return eventTimeRangeSupplier.getMSTR();
    }
    
    @Override
    public String getDescription() {
        return queryParams.getBaseURI().toString();
    }
    
    EventIterator getIterator() throws SeisFileException {
            try {
                return getQuakeML().getEventParameters().getEvents();
            } catch(SeisFileException e) {
                throw e;
            } catch(Exception e) {
                throw new SeisFileException(e);
            }
    }
    
    CacheEvent toCacheEvent(Event e) {
        String desc = "";
        if (e.getDescriptionList().size() > 0) {
            desc = e.getDescriptionList().get(0).getText();
        }
        FlinnEngdahlRegion fe = new FlinnEngdahlRegionImpl(FlinnEngdahlType.GEOGRAPHIC_REGION,
                                                           -1);
        for (EventDescription eDescription : e.getDescriptionList()) {
            if (eDescription.getIrisFECode() != -1) {
                fe = new FlinnEngdahlRegionImpl(FlinnEngdahlType.GEOGRAPHIC_REGION,
                                                eDescription.getIrisFECode());
            }
        }
        
        
        List<OriginImpl> out = new ArrayList<OriginImpl>();
        HashMap<String, List<Magnitude>> magsByOriginId = new HashMap<String, List<Magnitude>>();
        List<Magnitude> mList = e.getMagnitudeList();
        Magnitude prefMag = null; // event should always have a prefMag
        for (Magnitude m : mList) {
            if ( ! magsByOriginId.containsKey(m.getOriginId())) {
                magsByOriginId.put(m.getOriginId(), new ArrayList<Magnitude>());
            }
            magsByOriginId.get(m.getOriginId()).add(m);
            logger.debug("Mag origin id "+m.getOriginId());
            if (m.getPublicId().equals(e.getPreferredMagnitudeID())) {
                prefMag = m;
            }
        }
        OriginImpl pref = null;
        for (Origin o : e.getOriginList()) {
            List<Magnitude> oMags = magsByOriginId.get(o.getPublicId());
            if (oMags == null) { 
                oMags = new ArrayList<Magnitude>();
            }
            List<edu.iris.Fissures.IfEvent.Magnitude> fisMags = new ArrayList<edu.iris.Fissures.IfEvent.Magnitude>();
            for (Magnitude m : oMags) {
                fisMags.add(new edu.iris.Fissures.IfEvent.Magnitude(m.getType(), 
                                                                    m.getMag().getValue(), 
                                                                    m.getCreationInfo().getAuthor()));
            }
            QuantityImpl depth = new QuantityImpl(o.getDepth().getValue(), UnitImpl.METER);
            if (depth.get_value() > 0 && depth.get_value() < 1000) {
                System.err.println("Warning: QuakeML event depth should be in METERS but looks like KILOMETERS: "+depth.get_value());
                depth = new QuantityImpl(o.getDepth().getValue(), UnitImpl.KILOMETER);
            }
            OriginImpl oImpl = new OriginImpl(o.getPublicId(),
                                              o.getIrisCatalog(),
                                              o.getIrisContributor(),
                                              new MicroSecondDate(o.getTime().getValue()).getFissuresTime(),
                                              new Location(o.getLatitude().getValue(),
                                                           o.getLongitude().getValue(),
                                                           new QuantityImpl(0, UnitImpl.METER),
                                                           depth,
                                                           LocationType.GEOGRAPHIC),
                                              fisMags.toArray(new edu.iris.Fissures.IfEvent.Magnitude[0]),
                                              new ParameterRef[0]);
            out.add(oImpl);
            if (o.getPublicId().equals(e.getPreferredOriginID())) {
                pref = oImpl;
            }
        }
        // for convenience add the preferred magnitude as the first magnitude in the preferred origin
        if (prefMag != null && ! e.getPreferredMagnitudeID().equals(e.getPreferredOriginID())) {
            List<edu.iris.Fissures.IfEvent.Magnitude> newMags = new ArrayList<edu.iris.Fissures.IfEvent.Magnitude>();
            newMags.add(new edu.iris.Fissures.IfEvent.Magnitude(prefMag.getType(), prefMag.getMag().getValue(), prefMag.getCreationInfo().getAuthor()));
            newMags.addAll(pref.getMagnitudeList());
            pref = new OriginImpl(pref.get_id(),
                                  pref.getCatalog(),
                                  pref.getContributor(),
                                  pref.getOriginTime(),
                                  pref.getLocation(),
                                  newMags.toArray(new edu.iris.Fissures.IfEvent.Magnitude[0]),
                                  pref.getParmIds());
        }
        CacheEvent ce = new CacheEvent(new EventAttrImpl(desc, fe),
                                       out.toArray(new OriginImpl[0]),
                                       pref);
        return ce;
    }
    
    Quakeml getQuakeML() throws MalformedURLException, IOException, URISyntaxException, XMLStreamException,
            SeisFileException {
        FDSNEventQueryParams timeWindowQueryParams = queryParams.clone();
        MicroSecondDate now = ClockUtil.now();
        MicroSecondTimeRange queryTime = getQueryTime();
        timeWindowQueryParams.setStartTime(queryTime.getBeginTime());
        timeWindowQueryParams.setEndTime(queryTime.getEndTime());
        if (caughtUpWithRealtime() && lastQueryEnd != null) {
            timeWindowQueryParams.setUpdatedAfter(lastQueryEnd);
        }
        FDSNEventQuerier querier = new FDSNEventQuerier(timeWindowQueryParams);
        querier.setUserAgent("SOD/" + BuildVersion.getVersion());
        if (caughtUpWithRealtime() && hasNext()) {
            sleepUntilTime = now.add(refreshInterval);
            logger.debug("set sleepUntilTime " + sleepUntilTime);
            resetQueryTimeForLag();
            lastQueryEnd = now;
        }
        updateQueryEdge(queryTime);
        return querier.getQuakeML();
    }

    FDSNEventQueryParams queryParams = new FDSNEventQueryParams();

    MicroSecondTimeRangeSupplier eventTimeRangeSupplier;
    
    private MicroSecondDate lastQueryEnd;

    String url;
    
    URI parsedURL;
    
    MicroSecondDate queryTime;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FdsnEvent.class);
    
    public static final String URL_ELEMENT = "url";
}
