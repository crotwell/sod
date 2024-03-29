package edu.sc.seis.sod.source.event;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.seisFile.SeisFileException;
import edu.sc.seis.seisFile.fdsnws.AbstractFDSNQuerier;
import edu.sc.seis.seisFile.fdsnws.AbstractQueryParams;
import edu.sc.seis.seisFile.fdsnws.FDSNEventCatalogQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNEventQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNEventQueryParams;
import edu.sc.seis.seisFile.fdsnws.FDSNWSException;
import edu.sc.seis.seisFile.fdsnws.quakeml.Event;
import edu.sc.seis.seisFile.fdsnws.quakeml.EventDescription;
import edu.sc.seis.seisFile.fdsnws.quakeml.EventIterator;
import edu.sc.seis.seisFile.fdsnws.quakeml.Magnitude;
import edu.sc.seis.seisFile.fdsnws.quakeml.Origin;
import edu.sc.seis.seisFile.fdsnws.quakeml.Quakeml;
import edu.sc.seis.sod.BuildVersion;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventArm;
import edu.sc.seis.sod.RunProperties;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.source.AbstractSource;
import edu.sc.seis.sod.source.network.AbstractNetworkSource;
import edu.sc.seis.sod.subsetter.DepthRange;
import edu.sc.seis.sod.subsetter.origin.Catalog;
import edu.sc.seis.sod.subsetter.origin.Contributor;
import edu.sc.seis.sod.subsetter.origin.MagnitudeRange;
import edu.sc.seis.sod.subsetter.origin.OriginDepthRange;

public class FdsnEvent extends AbstractEventSource implements EventSource {

    public FdsnEvent(final FDSNEventQueryParams queryParams) {
        super("DefaultFDSNEvent", 2);
        eventTimeRangeSupplier = new MicroSecondTimeRangeSupplier() {

            @Override
            public MicroSecondTimeRange getMSTR() {
                SimpleDateFormat sdf = AbstractQueryParams.createDateFormat();
                try {
                    return new MicroSecondTimeRange(new MicroSecondDate(sdf.parse(queryParams.getParam(FDSNEventQueryParams.STARTTIME))),
                                                    new MicroSecondDate(sdf.parse(queryParams.getParam(FDSNEventQueryParams.ENDTIME))));
                } catch(ParseException e) {
                    throw new RuntimeException("Should not happen", e);
                }
            }
        };
        this.queryParams = queryParams;
    }

    public FdsnEvent(Element config) throws ConfigurationException {
        super(config, "DefaultFDSNEvent");
        queryParams.setIncludeAllMagnitudes(true).setOrderBy(FDSNEventQueryParams.ORDER_TIME_ASC); // fdsnEvent
                                                                                                   // default
                                                                                                   // is
                                                                                                   // reverse
                                                                                                   // time
        int port = SodUtil.loadInt(config, "port", -1);
        if (port > 0) {
            queryParams.setPort(port);
        }
        String host = SodUtil.loadText(config, "host", null);
        if (host != null && host.length() != 0) {
            queryParams.setHost(host);
        }
        String scheme = SodUtil.loadText(config, "scheme", null);
        if (scheme != null && scheme.length() != 0) {
            queryParams.setScheme(scheme);
            // also update port for 80,443 if needed
            if (port == -1) {
                // port not set in config, so set default for scheme
                if (scheme.equalsIgnoreCase("http") ) {
                    queryParams.setPort(80);
                }
                if (scheme.equalsIgnoreCase("https") ) {
                    queryParams.setPort(443);
                }
            }
        }
        // mainly for beta testing
        String fdsnwsPath = SodUtil.loadText(config, "fdsnwsPath", null);
        if (fdsnwsPath != null && fdsnwsPath.length() != 0) {
            queryParams.setFdsnwsPath(fdsnwsPath);
        }
        boolean foundCatalog = false;
        boolean foundContributor = false;
        NodeList childNodes = config.getChildNodes();
        for (int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if (node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if (!tagName.equals(AbstractSource.RETRIES_ELEMENT) && !tagName.equals("fdsnwsPath")
                        && !tagName.equals(AbstractNetworkSource.REFRESH_ELEMENT)
                        && !tagName.equals(AbstractEventSource.EVENT_QUERY_INCREMENT)
                        && !tagName.equals(AbstractEventSource.EVENT_LAG) && !tagName.equals(HOST_ELEMENT)
                        && !tagName.equals(PORT_ELEMENT) && !tagName.equals(AbstractSource.NAME_ELEMENT)) {
                    Object object = SodUtil.load((Element)node, new String[] {"eventArm", "origin"});
                    if (tagName.equals("originTimeRange")) {
                        eventTimeRangeSupplier = ((MicroSecondTimeRangeSupplier)SodUtil.load((Element)node,
                                                                                             new String[] {"eventArm",
                                                                                                           "origin"}));
                    } else if (tagName.equals("originDepthRange")) {
                        DepthRange dr = ((OriginDepthRange)object);
                        if (dr.getMinDepth().getValue(UnitImpl.KILOMETER) > -99999) {
                            queryParams.setMinDepth((float)dr.getMinDepth().getValue(UnitImpl.KILOMETER));
                        }
                        if (dr.getMaxDepth().getValue(UnitImpl.KILOMETER) < 99999) {
                            queryParams.setMaxDepth((float)dr.getMaxDepth().getValue(UnitImpl.KILOMETER));
                        }
                    } else if (tagName.equals("magnitudeRange")) {
                        MagnitudeRange magRange = (MagnitudeRange)object;
                        String[] magTypes = magRange.getSearchTypes();
                        // fdsn web services don't support multiple mag types,
                        // but we append them comma
                        // separated just in case they do one day. Sod will
                        // likely get an error back
                        String magStr = "";
                        for (int i = 0; i < magTypes.length; i++) {
                            magStr += magTypes[i];
                            if (i < magTypes.length - 1) {
                                magStr += ",";
                            }
                        }
                        if (magStr.length() != 0) {
                            queryParams.setMagnitudeType(magStr);
                            // also prepend the magnitudeRange to the
                            // origin subsetters and
                            // do the subsetting locally
                            EventArm eArm = Start.getEventArm();
                            if (eArm != null) {
                                eArm.getSubsetters().add(0, magRange);
                            }
                        } else if (queryParams.getHost().equals(AbstractQueryParams.IRIS_HOST)) {
                            queryParams.setMagnitudeType("preferred");
                        }
                        if (magRange.getMinValue() > -99) {
                            queryParams.setMinMagnitude((float)magRange.getMinValue());
                        }
                        if (magRange.getMaxValue() < 99) {
                            queryParams.setMaxMagnitude((float)magRange.getMaxValue());
                        }
                    } else if (object instanceof edu.iris.Fissures.Area) {
                        edu.iris.Fissures.Area area = (edu.iris.Fissures.Area)object;
                        if (area instanceof GlobalArea) {
                            // nothing needed
                        } else if (area instanceof BoxArea) {
                            BoxArea box = (BoxArea)area;
                            queryParams.area(box.min_latitude, box.max_latitude, box.min_longitude, box.max_longitude);
                        } else if (area instanceof PointDistanceArea) {
                            PointDistanceArea donut = (PointDistanceArea)area;
                            if ((float)((QuantityImpl)donut.min_distance).getValue(UnitImpl.DEGREE) > 0) {
                            queryParams.donut(donut.latitude,
                                              donut.longitude,
                                              (float)((QuantityImpl)donut.min_distance).getValue(UnitImpl.DEGREE),
                                              (float)((QuantityImpl)donut.max_distance).getValue(UnitImpl.DEGREE));
                            } else {
                                // don't send minradius if only care about maxradius
                                queryParams.ring(donut.latitude,
                                              donut.longitude,
                                              (float)((QuantityImpl)donut.max_distance).getValue(UnitImpl.DEGREE));
                            }
                        } else {
                            throw new ConfigurationException("Area of class " + area.getClass().getName()
                                    + " not understood");
                        }
                    } else if (tagName.equals("catalog")) {
                        foundCatalog = true;
                        queryParams.setCatalog(((Catalog)object).getCatalog());
                    } else if (tagName.equals("contributor")) {
                        queryParams.setContributor(((Contributor)object).getContributor());
                        foundContributor = true;
                        for (int c = 0; c < childNodes.getLength(); c++) {
                            Node cnode = childNodes.item(counter);
                            if (cnode instanceof Element && ((Element)cnode).getTagName().equals("catalog")) {
                                foundCatalog = true;
                            }
                        }
                        if (!foundCatalog && queryParams.getHost().equals(AbstractQueryParams.IRIS_HOST)) {
                            fixCatalogForContributor(((Contributor)object).getContributor());
                        }
                    }
                }
            }
        }
        try {
            if (foundContributor) {
                FDSNEventCatalogQuerier querier = new FDSNEventCatalogQuerier(queryParams);
                querier.setUserAgent(getUserAgent());
                logger.info("contributor URL: "+querier.formURI());
                checkAgainstKnownServer("contributor", querier.getContributors());
                return;
            }
            if (foundCatalog) {
                FDSNEventCatalogQuerier querier = new FDSNEventCatalogQuerier(queryParams);
                querier.setUserAgent(getUserAgent());
                logger.info("catalog URL: "+querier.formURI());
                checkAgainstKnownServer("catalog", querier.getCatalogs());
                return;
            }
        } catch(FDSNWSException e) {
            throw new ConfigurationException("Trouble verifying catalog/contributor with server", e);
        } catch(URISyntaxException e) {
            throw new ConfigurationException("Trouble verifying catalog/contributor with server", e);
        }
    }

    private void checkAgainstKnownServer(String askStyle, List<String> contribList) {
        String askCatalog = queryParams.getParam(askStyle);
        if (!contribList.contains(askCatalog)) {
            String knownCatalogs = "";
            for (String c : contribList) {
                knownCatalogs += c + ", ";
            }
            knownCatalogs = knownCatalogs.substring(0, knownCatalogs.length()-2);
            Start.informUserOfBadQueryAndExit("You asked for events from the " + askCatalog + " " + askStyle
                    + " but the server does not think it has that "+askStyle+". Known "+askStyle+"s are: " + knownCatalogs+".", null);
        }
    }

    private void fixCatalogForContributor(String contributor) {
        if (contributor.contains("NEIC")) {
            queryParams.setCatalog("NEIC PDE");
        } else if (contributor.contains("GCMT")) {
            queryParams.setCatalog("GCMT");
        } else if (contributor.contains("ISC")) {
            queryParams.setCatalog("ISC");
        } else if (contributor.contains("ANF")) {
            queryParams.setCatalog("ANF");
        } else if (contributor.equals("University of Washington")) {
            queryParams.setCatalog("UofW");
        }
    }

    @Override
    public boolean hasNext() {
        MicroSecondDate queryEnd = getEventTimeRange().getEndTime();
        MicroSecondDate quitDate = queryEnd.add(lag);
        boolean out = quitDate.equals(ClockUtil.now()) || quitDate.after(ClockUtil.now())
                || !getQueryStart().equals(queryEnd);
        logger.debug(getName() + " Checking if more queries to the event server are in order.  The quit date is "
                + quitDate + " the last query was for " + getQueryStart() + " and we're querying to " + queryEnd
                + " result=" + out);
        return out;
    }

    @Override
    public CacheEvent[] next() {
        MicroSecondTimeRange queryTime = getQueryTime();
        logger.debug(getName() + ".next() called for " + queryTime);
        logger.debug("eventTimeRangeSupplier.getMSTR(): "+eventTimeRangeSupplier.getMSTR());
        int count = 0;
        Exception latest = null;
        while (count == 0 || getRetryStrategy().shouldRetry(latest, this, count)) {
            try {
                count++;
                List<CacheEvent> result = internalNext(queryTime);
                if (count > 1) {
                    getRetryStrategy().serverRecovered(this);
                }
                return result.toArray(new CacheEvent[0]);
            } catch(SeisFileException e) {
                count++;
                latest = e;
                Throwable rootCause = AbstractFDSNQuerier.extractRootCause(e);
                if (rootCause instanceof IOException) {
                    // try again on IOException
                    if (rootCause instanceof java.net.SocketTimeoutException
                            || rootCause instanceof InterruptedIOException) {
                        // timed out, so decrease increment and retry with
                        // smaller time window
                        // also make the increaseThreashold smaller so we are
                        // not so aggressive
                        // about expanding the time window
                        // but only do this if the query is for more than one day
                        // to avoid many tiny requests
                        if (getIncrement().greaterThan(MIN_INCREMENT)) {
                            decreaseQueryTimeWidth();
                            if (increaseThreashold > 2) {
                                increaseThreashold /= 2;
                            }
                        }
                        return new CacheEvent[0];
                    }
                } else if (e instanceof FDSNWSException && ((FDSNWSException)e).getHttpResponseCode() != 200) {
                    latest = e;
                    if (((FDSNWSException)e).getHttpResponseCode() == 400) {
                        // badly formed query, cowardly quit
                        Start.simpleArmFailure(Start.getEventArm(), BAD_PARAM_MESSAGE+" "+((FDSNWSException)e).getMessage()+" on "+((FDSNWSException)e).getTargetURI());
                    }
                } else {
                    throw new RuntimeException(e);
                }
            } catch(XMLStreamException e) {
                count++;
                latest = e;
                Throwable rootCause = AbstractFDSNQuerier.extractRootCause(e);
                if (rootCause instanceof IOException) {
                    if (rootCause instanceof java.net.SocketTimeoutException) {
                        // timed out, so decrease increment and retry with
                        // smaller time window
                        // also make the increaseThreashold smaller so we are
                        // not so aggressive
                        // about expanding the time window
                        // but only do this if the query is for more than one day
                        // to avoid many tiny requests
                        if (getIncrement().greaterThan(MIN_INCREMENT)) {
                        decreaseQueryTimeWidth();
                        if (increaseThreashold > 2) {
                            increaseThreashold /= 2;
                        }
                        }
                        return new CacheEvent[0];
                    }
                } else {
                    throw new RuntimeException(e);
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw new RuntimeException(latest);
    }

    public List<CacheEvent> internalNext(MicroSecondTimeRange queryTime) throws SeisFileException, XMLStreamException {
        try {
            MicroSecondDate now = ClockUtil.now();
            List<CacheEvent> out = new ArrayList<CacheEvent>();
            FDSNEventQuerier querier = getQuakeMLQuerier(setUpQuery(queryTime));
            Quakeml qml = querier.getQuakeML();
            EventIterator it = qml.getEventParameters().getEvents();
            if (!it.hasNext()) {
                logger.debug("No events returned from query.");
            }
            while (it.hasNext()) {
                Event e = it.next();
                out.add(toCacheEvent(e));
            }
            qml.close();
            if (!caughtUpWithRealtime()) {
                if (out.size() < increaseThreashold) {
                    increaseQueryTimeWidth();
                }
                if (out.size() > decreaseThreashold) {
                    decreaseQueryTimeWidth();
                }
            }
            updateQueryEdge(queryTime);
            return out;
        } catch(SeisFileException e) {
            throw e;
        } catch(Exception e) {
            throw new SeisFileException(e);
        }
    }

    @Override
    public MicroSecondTimeRange getEventTimeRange() {
        return eventTimeRangeSupplier.getMSTR();
    }

    @Override
    public String getDescription() {
        try {
            return queryParams.formURI().toString() + " with time range appended later.";
        } catch(URISyntaxException e) {
            throw new RuntimeException("Unable to for URL for description.", e);
        }
    }

    static CacheEvent toCacheEvent(Event e) {
        String desc = "";
        if (e.getDescriptionList().size() > 0) {
            desc = e.getDescriptionList().get(0).getText();
        }
        FlinnEngdahlRegion fe = new FlinnEngdahlRegionImpl(FlinnEngdahlType.GEOGRAPHIC_REGION, 0);
        for (EventDescription eDescription : e.getDescriptionList()) {
            if (eDescription.getIrisFECode() != -1) {
                fe = new FlinnEngdahlRegionImpl(FlinnEngdahlType.GEOGRAPHIC_REGION, eDescription.getIrisFECode());
            }
        }
        List<OriginImpl> out = new ArrayList<OriginImpl>();
        HashMap<String, List<Magnitude>> magsByOriginId = new HashMap<String, List<Magnitude>>();
        List<Magnitude> mList = e.getMagnitudeList();
        Magnitude prefMag = null; // event should always have a prefMag
        List<edu.iris.Fissures.IfEvent.Magnitude> noOriginMagList = new ArrayList<edu.iris.Fissures.IfEvent.Magnitude>();
        for (Magnitude m : mList) {
            if (m.getOriginId() != null) {
                if (!magsByOriginId.containsKey(m.getOriginId())) {
                    magsByOriginId.put(m.getOriginId(), new ArrayList<Magnitude>());
                }
                magsByOriginId.get(m.getOriginId()).add(m);
                if (m.getPublicId().equals(e.getPreferredMagnitudeID())) {
                    prefMag = m;
                }
            } else {
                noOriginMagList.add(toFissuresMagnitude(m));
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
                if (m.getMag() != null) {
                    // usgs web service has some magnitudes with only origin
                    // reference, skip these
                    fisMags.add(toFissuresMagnitude(m));
                }
            }
            if (o.getLatitude() != null && o.getLongitude() != null && o.getTime() != null) {
                // usgs web service has some origins with only a depth, skip
                // these
                QuantityImpl depth = new QuantityImpl(o.getDepth().getValue(), UnitImpl.METER);
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
            } else {
                logger.info("Can't create origin due to NULL: id:" + o.getPublicId() + " lat:" + o.getLatitude()
                        + " long:" + o.getLongitude() + " time:" + o.getTime() + ", skipping.");
            }
        }
        // for convenience add the preferred magnitude as the first magnitude in
        // the preferred origin
        List<edu.iris.Fissures.IfEvent.Magnitude> prefMagList = new ArrayList<edu.iris.Fissures.IfEvent.Magnitude>();
        if (prefMag != null && !e.getPreferredMagnitudeID().equals(e.getPreferredOriginID())) {
            edu.iris.Fissures.IfEvent.Magnitude pm = toFissuresMagnitude(prefMag);
            prefMagList.add(pm);
            for (edu.iris.Fissures.IfEvent.Magnitude m : pref.getMagnitudes()) {
                prefMagList.add(m);
            }
            Iterator<edu.iris.Fissures.IfEvent.Magnitude> it = prefMagList.iterator();
            it.next(); // skip first as it is the preferred
            while (it.hasNext()) {
                edu.iris.Fissures.IfEvent.Magnitude fm = it.next();
                if (fm.type.equals(pm.type) && fm.value == pm.value && fm.contributor.equals(pm.contributor)) {
                    it.remove(); // duplicate of preferred
                }
            }
        } else {
            for (edu.iris.Fissures.IfEvent.Magnitude m : pref.getMagnitudes()) {
                prefMagList.add(m);
            }
        }
        for (String oid : magsByOriginId.keySet()) {
            boolean found = false;
            for (Origin o : e.getOriginList()) {
                if (o.getPublicId().equals(oid)) {
                    found = true;
                    break;
                }
            }
            if ( ! found && pref != null) {
                // did not find origin for mag, so add to pref origin???
                for(Magnitude m : magsByOriginId.get(oid)) {
                    prefMagList.add(toFissuresMagnitude(m));
                }
            }
        }
        prefMagList.addAll(noOriginMagList);
        if (pref != null) {
            pref = new OriginImpl(pref.get_id(),
                    pref.getCatalog(),
                    pref.getContributor(),
                    pref.getOriginTime(),
                    pref.getLocation(),
                    prefMagList.toArray(new edu.iris.Fissures.IfEvent.Magnitude[0]),
                    pref.getParmIds());
        }
        CacheEvent ce = new CacheEvent(new EventAttrImpl(desc, fe), out.toArray(new OriginImpl[0]), pref);
        return ce;
    }

    static edu.iris.Fissures.IfEvent.Magnitude toFissuresMagnitude(Magnitude m) {
        String contributor = "";
        if (m.getCreationInfo() != null && m.getCreationInfo().getAuthor() != null) {
            contributor = m.getCreationInfo().getAuthor();
        }
        String type = "";
        if (m.getType() != null) {
            type = m.getType();
        }
        return new edu.iris.Fissures.IfEvent.Magnitude(type, m.getMag().getValue(), contributor);
    }

    FDSNEventQuerier getQuakeMLQuerier(FDSNEventQueryParams timeWindowQueryParams) throws MalformedURLException, IOException,
            URISyntaxException, XMLStreamException, SeisFileException {
        FDSNEventQuerier querier = new FDSNEventQuerier(timeWindowQueryParams);
        RunProperties runProps = Start.getRunProps();
        if (runProps.getProxyHost() != null) {
          querier.setProxyHost(runProps.getProxyHost());
          querier.setProxyPort(runProps.getProxyPort());
          querier.setProxyScheme(runProps.getProxyScheme());
        }
        querier.setUserAgent(getUserAgent());
        return querier;
    }

    FDSNEventQueryParams setUpQuery(MicroSecondTimeRange queryTime) throws URISyntaxException {
        FDSNEventQueryParams timeWindowQueryParams = queryParams.clone();
        timeWindowQueryParams.setStartTime(queryTime.getBeginTime());
        timeWindowQueryParams.setEndTime(queryTime.getEndTime());
        if (isEverCaughtUpToRealtime() && lastQueryEnd != null) {
            timeWindowQueryParams.setUpdatedAfter(lastQueryEnd.subtract(new TimeInterval(10, UnitImpl.HOUR)));
        }
        logger.debug("Query: " + timeWindowQueryParams.formURI());
        return timeWindowQueryParams;
    }

    @Override
    protected MicroSecondDate resetQueryTimeForLag() {
        MicroSecondDate out = super.resetQueryTimeForLag();
        lastQueryEnd = nextLastQueryEnd;
        nextLastQueryEnd = ClockUtil.now();
        return out;
    }

    FDSNEventQueryParams queryParams = new FDSNEventQueryParams();

    MicroSecondTimeRangeSupplier eventTimeRangeSupplier;

    private MicroSecondDate lastQueryEnd;

    private MicroSecondDate nextLastQueryEnd;

    String url;

    int port = -1;

    URI parsedURL;

    int increaseThreashold = 10;

    int decreaseThreashold = 100;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FdsnEvent.class);

    public static final String URL_ELEMENT = "url";

    public static final String HOST_ELEMENT = "host";

    public static final String PORT_ELEMENT = "port";

    public static final String BAD_PARAM_MESSAGE = "The remote web service just indicated that the query was badly formed. "
            +"This may be because it does not support all of the parameters that SOD uses or it could be a bug in SOD. "
            +"Check your recipe and "
            +"if you cannot figure it out contact the developers at sod@seis.sc.edu. ";

    String userAgent = "SOD/" + BuildVersion.getVersion();

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
