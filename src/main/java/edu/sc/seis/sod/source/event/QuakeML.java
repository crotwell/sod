package edu.sc.seis.sod.source.event;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Element;

import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.FlinnEngdahlType;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
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
import edu.sc.seis.seisFile.quakeml.Event;
import edu.sc.seis.seisFile.quakeml.EventDescription;
import edu.sc.seis.seisFile.quakeml.EventIterator;
import edu.sc.seis.seisFile.quakeml.Magnitude;
import edu.sc.seis.seisFile.quakeml.Origin;
import edu.sc.seis.seisFile.quakeml.Quakeml;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.source.network.AbstractNetworkSource;


public class QuakeML implements EventSource {


    public QuakeML() {
        url = "http://www.iris.edu/ws/event/query?";
        refreshInterval = Start.getRunProps().getEventRefreshInterval();
        lag = Start.getRunProps().getEventLag();
        increment = Start.getRunProps().getEventQueryIncrement();
    }
    
    public QuakeML(Element config) throws ConfigurationException {
        this();
        if (DOMHelper.hasElement(config, URL_ELEMENT)) {
            url = SodUtil.getNestedText(SodUtil.getElement(config, URL_ELEMENT));
            try {
                parsedURL = new URI(url);
                List<String> split = new ArrayList<String>();
                if (parsedURL.getQuery() != null) {
                    String[] splitArray = parsedURL.getQuery().split("&");
                    for (String s : splitArray) {
                        String[] nvSplit = s.split("=");
                        if (!nvSplit[0].equals("level")) {
                            // zap level as we do that ourselves
                            split.add(s);
                        }
                        if (nvSplit[0].equals("starttime")) {
                            start = new MicroSecondDate(nvSplit[1]);
                        } else if (nvSplit[0].equals("endtime")) {
                            end = new MicroSecondDate(nvSplit[1]);
                        }
                    }
                    String newQuery = "";
                    boolean first = true;
                    for (String s : split) {
                        if (!first) {
                            newQuery += "&";
                        }
                        newQuery += s;
                        first = false;
                    }
                    parsedURL = new URI(parsedURL.getScheme(),
                                        parsedURL.getUserInfo(),
                                        parsedURL.getHost(),
                                        parsedURL.getPort(),
                                        parsedURL.getPath(),
                                        newQuery,
                                        parsedURL.getFragment());
                    url = parsedURL.toURL().toString();
                }
            } catch(URISyntaxException e) {
                throw new ConfigurationException("Invalid <url> element found.", e);
            } catch(MalformedURLException e) {
                throw new ConfigurationException("Bad URL", e);
            }
        } else {
            throw new ConfigurationException("No <url> element found");
        }
        if(DOMHelper.hasElement(config, AbstractNetworkSource.REFRESH_ELEMENT)) {
            refreshInterval = SodUtil.loadTimeInterval(SodUtil.getElement(config, AbstractNetworkSource.REFRESH_ELEMENT));
        } else {
            refreshInterval = new TimeInterval(1, UnitImpl.FORTNIGHT);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return (end == null && ! url.startsWith("file:")) || getIterator().hasNext();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
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
            throw new RuntimeException(e); //ToDo: fix this
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
        if (start != null && end != null) {
            return new MicroSecondTimeRange(start, end);
        } else if (start != null) {
            return new MicroSecondTimeRange(start, ClockUtil.wayFuture());
        } else if (end != null) {
            return new MicroSecondTimeRange( ClockUtil.wayPast(), end);
        } else {
            return new MicroSecondTimeRange(ClockUtil.wayPast(), ClockUtil.wayFuture());
        }
    }

    @Override
    public String getDescription() {
        return url;
    }
    
    EventIterator getIterator() throws SeisFileException {
        if (it == null) {
            try {
                it = getQuakeML().getEventParameters().getEvents();
            } catch(SeisFileException e) {
                throw e;
            } catch(Exception e) {
                throw new SeisFileException(e);
            }
        }
        return it;
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
    
    Quakeml getQuakeML() throws MalformedURLException, IOException, URISyntaxException, XMLStreamException, SeisFileException {
        if (quakeml == null) {
            InputStream in  = new BufferedInputStream(new URI(url).toURL().openStream());
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader r = factory.createXMLEventReader(in);
            XMLEvent e = r.peek();
            while(! e.isStartElement()) {
                e = r.nextEvent(); // eat this one
                e = r.peek();  // peek at the next
            }
            quakeml = new Quakeml(r);
        }
        return quakeml;
    }
    
    Quakeml quakeml;
    
    EventIterator it;
    
    MicroSecondDate start;
    
    MicroSecondDate end;
    
    String url;
    
    URI parsedURL;
    
    MicroSecondDate queryTime;

    protected TimeInterval increment, lag;
    
    protected TimeInterval refreshInterval = new TimeInterval(10, UnitImpl.MINUTE);
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(QuakeML.class);
    
    public static final String URL_ELEMENT = "url";
}
