package edu.sc.seis.sod.source.network;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.validation.XMLValidationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.seisFile.SeisFileException;
import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.AbstractFDSNQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNStationQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNStationQueryParams;
import edu.sc.seis.seisFile.fdsnws.FDSNWSException;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.DataAvailability;
import edu.sc.seis.seisFile.fdsnws.stationxml.FDSNStationXML;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.NetworkIterator;
import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationIterator;
import edu.sc.seis.sod.BuildVersion;
import edu.sc.seis.sod.RunProperties;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.BoxAreaImpl;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.model.station.StationIdUtil;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.event.FdsnEvent;
import edu.sc.seis.sod.subsetter.station.StationPointDistance;
import edu.sc.seis.sod.util.convert.stationxml.ChannelSensitivityBundle;
import edu.sc.seis.sod.util.convert.stationxml.StationXMLToFissures;
import edu.sc.seis.sod.util.time.ClockUtil;

public class FdsnStation extends AbstractNetworkSource {

    public FdsnStation() {
        super("defaultFDSNNetwork", -1);
    }
    
    public FdsnStation(String name, int retries, FDSNStationQueryParams queryParams) {
        super(name, retries);
        this.queryParams = queryParams;
    }

    public FdsnStation(Element config) throws Exception {
        super(config);
        queryParams.clearIncludeRestricted();
        queryParams.clearIncludeAvailability();
        includeAvailability = SodUtil.isTrue(config, "includeAvailability", true);
        validateXML = SodUtil.isTrue(config, "validate", false);
        if (config != null) {
            // otherwise just use defaults
            int port = SodUtil.loadInt(config, "port", -1);
            if (port > 0) {
                queryParams.setPort(port);
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
            NodeList childNodes = config.getChildNodes();
            for (int counter = 0; counter < childNodes.getLength(); counter++) {
                Node node = childNodes.item(counter);
                if (node instanceof Element) {
                    Element element = (Element)node;
                    if (element.getTagName().equals("stationBoxArea")) {
                        BoxAreaImpl a = SodUtil.loadBoxArea(element);
                        queryParams.area(a.min_latitude, a.max_latitude, a.min_longitude, a.max_longitude);
                    } else if (element.getTagName().equals("stationPointDistance")) {
                        StationPointDistance pd = (StationPointDistance)SodUtil.load(element, new String[] {"station"});
                        queryParams.donut(pd.asDonut());
                    } else if (element.getTagName().equals("networkCode")) {
                        queryParams.appendToNetwork(SodUtil.getNestedText(element));
                    } else if (element.getTagName().equals("stationCode")) {
                        queryParams.appendToStation(SodUtil.getNestedText(element));
                    } else if (element.getTagName().equals("siteCode")) {
                        queryParams.appendToLocation(SodUtil.getNestedText(element));
                    } else if (element.getTagName().equals("channelCode")) {
                        queryParams.appendToChannel(SodUtil.getNestedText(element));
                    } else if (element.getTagName().equals("includeRestricted")) {
                        queryParams.setIncludeRestricted(true);
                    } else if (element.getTagName().equals("matchTimeseries")) {
                        logger.debug("Setting matchtimeseries");
                        queryParams.setMatchTimeseries(true);
                    } else if (element.getTagName().equals("host")) {
                        String host = SodUtil.getNestedText(element);
                        queryParams.setHost(host);
                        this.name = host;
                    } else if (element.getTagName().equals("fdsnwsPath")) {
                        // mainly for beta testing
                        String fdsnwsPath = SodUtil.getNestedText(element);
                        if (fdsnwsPath != null && fdsnwsPath.length() != 0) {
                            queryParams.setFdsnwsPath(fdsnwsPath);
                            logger.debug("Set fdsnwsPath: "+fdsnwsPath);
                        }
                    }
                }
            }
        }
    }
    
    public void includeRestricted(boolean val) {
        queryParams.setIncludeRestricted(val);
    }
    
    @Override
    public List<? extends Network> getNetworks() throws SodSourceException {
        List<Network> out = new ArrayList<Network>();
        FDSNStationXML staxml = null;
        try {
            FDSNStationQueryParams staQP = setupQueryParams();
            staQP.setLevel(FDSNStationQueryParams.LEVEL_NETWORK);
            staQP.clearChannel(); // channel constraints make getting networks very slow
            staQP.clearStartAfter().clearStartBefore().clearStartTime(); // start and end times also slow as 
            staQP.clearEndAfter().clearEndBefore().clearEndTime();       // applied to channel not network
            logger.debug("getNetworks "+staQP.formURI());
            staxml = internalGetStationXML(staQP);
            NetworkIterator netIt = staxml.getNetworks();
            while (netIt.hasNext()) {
                edu.sc.seis.seisFile.fdsnws.stationxml.Network n = netIt.next();
                out.add(n);
            }
            return out;
        } catch(URISyntaxException e) {
            // should not happen
            throw new SodSourceException("Problem forming URI", e);
        } catch(SeisFileException e) {
            throw new SodSourceException(e);
        } catch(XMLValidationException e) {
            logger.warn("InvalidXML: getting networks"+ e.getMessage().replace('\n', ' '));
            // debug to get stack trace in log file, but not in warn which goes to stderr
            logger.debug("InvalidXML: getting networks"+ e.getMessage().replace('\n', ' '), e);
            return out;
        } catch(XMLStreamException e) {
            throw new SodSourceException(e);
        } finally {
            if (staxml != null) {
                staxml.closeReader();
            }
        }
    }

    @Override
    public List<? extends Station> getStations(Network net) throws SodSourceException {
        List<Station> out = new ArrayList<Station>();
        FDSNStationXML staxml = null;
        try {
            FDSNStationQueryParams staQP = setupQueryParams();
            // add any "virtual" network codes back to the query as they limit stations
            // in real networks.
            String netString = staQP.getParam(FDSNStationQueryParams.NETWORK);
            if (netString != null) {
                String[] paramNets = netString.split(",");
                staQP.clearNetwork();
                for (int i = 0; i < paramNets.length; i++) {
                    if (paramNets[i].length() > 2) {
                        // assume virtual, so add to query
                        staQP.appendToNetwork(paramNets[i]);
                    }
                }
            }
            staQP.setLevel(FDSNStationQueryParams.LEVEL_STATION);
            // now append the real network code
            staQP.appendToNetwork(net.getNetworkCode());
            if ( ! checkTimeParamsOk(net.getStartDateTime(), net.getEndDateTime(), constraints)) {
                // station does not overlap time window
                logger.info("time window for "+net+" "+net.getStartDateTime()+" "+net.getEndDateTime()+
                        " does not overlap sod constraints: "+constraints.getConstrainingBeginTime()+" "+constraints.getConstrainingEndTime()+", skipping.");
                return out;
            }
            setTimeParams(staQP, net.getStartDateTime(), net.getEndDateTime(), constraints);
            logger.debug("getStations "+staQP.formURI());
            staxml = internalGetStationXML(staQP);
            NetworkIterator netIt = staxml.getNetworks();
            while (netIt.hasNext()) {
                edu.sc.seis.seisFile.fdsnws.stationxml.Network n = netIt.next();
                StationIterator staIt = n.getStations();
                while (staIt.hasNext()) {
                    edu.sc.seis.seisFile.fdsnws.stationxml.Station s = staIt.next();
                    out.add(s);
                }
            }
            return out;
        } catch(URISyntaxException e) {
            // should not happen
            throw new SodSourceException("Problem forming URI", e);
        } catch(SeisFileException e) {
            throw new SodSourceException(e);
        } catch(XMLValidationException e) {
            // debug to get stack trace in log file, but not in warn which goes to stderr
            logger.warn("InvalidXML: "+net.toString()+" "+ e.getMessage().replace('\n', ' '));
            logger.debug("InvalidXML: "+net.toString()+" "+ e.getMessage().replace('\n', ' '), e);
            return out;
        } catch(XMLStreamException e) {
            throw new SodSourceException(e);
        } finally {
            if (staxml != null) {
                staxml.closeReader();
            }
        }
    }

    @Override
    public List<? extends Channel> getChannels(Station station) throws SodSourceException  {
        List<Channel> out = new ArrayList<Channel>();
        FDSNStationXML staxml = null;
        try {
            FDSNStationQueryParams staQP = setupQueryParams();
            staQP.setLevel(FDSNStationQueryParams.LEVEL_CHANNEL);
            staQP.setIncludeAvailability(includeAvailability);
            staQP.clearNetwork()
                    .appendToNetwork(station.getNetworkCode())
                    .clearStation()
                    .appendToStation(station.getStationCode());
            if ( ! checkTimeParamsOk(station.getStartDateTime(), station.getEndDateTime(), constraints)) {
                // station does not overlap time window
                logger.info("time window for "+station+" "+station.getStartDateTime()+" "+station.getEndDateTime()+
                        " does not overlap sod constraints: "+constraints.getConstrainingBeginTime()+" "+constraints.getConstrainingEndTime()+", skipping.");

                return out;
            }setTimeParams(staQP, station.getStartDateTime(), station.getEndDateTime(), constraints);
            logger.info("getChannels "+staQP.formURI());
            staxml = internalGetStationXML(staQP);
            NetworkIterator netIt = staxml.getNetworks();
            while (netIt.hasNext()) {
                edu.sc.seis.seisFile.fdsnws.stationxml.Network n = netIt.next();
                StationIterator staIt = n.getStations();
                while (staIt.hasNext()) {
                    edu.sc.seis.seisFile.fdsnws.stationxml.Station s = staIt.next();
                    for (Channel c : s.getChannelList()) {
                        out.add(c);
                    }
                }
            }
            return out;
        } catch(URISyntaxException e) {
            // should not happen
            throw new SodSourceException("Problem forming URI", e);
        } catch(SeisFileException e) {
            throw new SodSourceException(e);
        } catch(XMLValidationException e) {
            // debug to get stack trace in log file, but not in warn which goes to stderr
            logger.warn("InvalidXML: "+StationIdUtil.toString(station)+" "+ e.getMessage().replace('\n', ' '));
            logger.debug("InvalidXML: "+StationIdUtil.toString(station)+" "+ e.getMessage().replace('\n', ' '), e);
            return out;
        } catch(XMLStreamException e) {
            throw new SodSourceException(e);
        } finally {
            if (staxml != null) {
                staxml.closeReader();
            }
        }
    }

    @Override
    public Response getResponse(Channel chan) throws SodSourceException, ChannelNotFound, InvalidResponse  {
        FDSNStationXML staxml = null;
        try {
            if (chan == null) { throw new IllegalArgumentException("Channel is null");}
            FDSNStationQueryParams staQP = setupQueryParams();
            staQP.setLevel(FDSNStationQueryParams.LEVEL_RESPONSE);
            staQP.clearNetwork()
                    .appendToNetwork(chan.getNetworkCode())
                    .clearStation()
                    .appendToStation(chan.getStationCode())
                    .clearLocation()
                    .appendToLocation(chan.getLocCode())
                    .clearChannel()
                    .appendToChannel(chan.getChannelCode());
            setTimeParamsToGetSingleChan(staQP, chan.getStartDateTime(), chan.getEndDateTime());
            logger.debug("getResponse "+staQP.formURI());
            staxml = internalGetStationXML(staQP);
            NetworkIterator netIt = staxml.getNetworks();
            while (netIt.hasNext()) {
                edu.sc.seis.seisFile.fdsnws.stationxml.Network n = netIt.next();
                StationIterator staIt = n.getStations();
                while (staIt.hasNext()) {
                    edu.sc.seis.seisFile.fdsnws.stationxml.Station s = staIt.next();
                    for (Channel c : s.getChannelList()) {
                        // first one should be right 
                        if (staxml != null) {
                            staxml.closeReader();
                            staxml = null;
                        }
                        return c.getResponse();
                    }
                }
            }
            throw new ChannelNotFound(chan);
        } catch(URISyntaxException e) {
            // should not happen
            throw new SodSourceException("Problem forming URI", e);
        } catch(SeisFileException e) {
            throw new SodSourceException(e);
        } catch(XMLValidationException e) {
            // debug to get stack trace in log file, but not in warn which goes to stderr
            logger.warn("InvalidXML: "+ChannelIdUtil.toString(chan)+" "+ e.getMessage().replace('\n', ' '));
            logger.debug("InvalidXML: "+ChannelIdUtil.toString(chan)+" "+ e.getMessage().replace('\n', ' '), e);
            throw new InvalidResponse(e);
        } catch(XMLStreamException e) {
            throw new SodSourceException(e);
        } finally {
            if (staxml != null) {
                staxml.closeReader();
            }
        }
    }

    FDSNStationQueryParams setupQueryParams() {
        FDSNStationQueryParams cloneQP = queryParams.clone();
        if (constraints != null) {
            for (String netCode : constraints.getConstrainingNetworkCodes()) {
                cloneQP.appendToNetwork(netCode);
            }
            for (String staCode : constraints.getConstrainingStationCodes()) {
                cloneQP.appendToStation(staCode);
            }
            for (String siteCode : constraints.getConstrainingLocationCodes()) {
                cloneQP.appendToLocation(siteCode);
            }
            for (String chanCode : constraints.getConstrainingChannelCodes()) {
                cloneQP.appendToChannel(chanCode);
            }
            if (constraints.getConstrainingBeginTime() != null) {
                cloneQP.setEndAfter(constraints.getConstrainingBeginTime());
            }
            if (constraints.getConstrainingEndTime() != null) {
                cloneQP.setStartBefore(constraints.getConstrainingEndTime());
            }
        }
        return cloneQP;
    }
    
    FDSNStationQuerier setupQuerier(FDSNStationQueryParams queryParams) {
        FDSNStationQuerier querier = new FDSNStationQuerier(queryParams);
        RunProperties runProps = Start.getRunProps();
        if (runProps.getProxyHost() != null) {
          querier.setProxyHost(runProps.getProxyHost());
          querier.setProxyPort(runProps.getProxyPort());
          querier.setProxyProtocol(runProps.getProxyScheme());
        }
        if (validateXML) {
            querier.setValidate(true);
        }
        querier.setUserAgent("SOD/"+BuildVersion.getVersion());
        return querier;
    }
    
    FDSNStationXML internalGetStationXML(FDSNStationQueryParams staQP) {
        int count = 0;
        SeisFileException latest = null;
        FDSNStationXML out = null;
        while (count == 0 || getRetryStrategy().shouldRetry(latest, this, count)) {
            try {
                // querier is closed when the FDSNStationXML is closed internal to it
                FDSNStationQuerier querier = setupQuerier(staQP);
                out = querier.getFDSNStationXML();
                if (count > 0) { getRetryStrategy().serverRecovered(this); }
                return out;
            } catch(SeisFileException e) {
                count++;
                if (out != null) {
                    out.closeReader();
                    out = null;
                }
                latest = e;
                Throwable rootCause = AbstractFDSNQuerier.extractRootCause(e);
                if (rootCause instanceof IOException) {
                    // try again on IOException
                } else if (e instanceof FDSNWSException && ((FDSNWSException)e).getHttpResponseCode() != 200) {
                    latest = e;
                    if (((FDSNWSException)e).getHttpResponseCode() == 400) {
                        // badly formed query, cowardly quit
                        Start.simpleArmFailure(Start.getNetworkArm(), FdsnEvent.BAD_PARAM_MESSAGE+" "+((FDSNWSException)e).getMessage()+" on "+((FDSNWSException)e).getTargetURI());
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
    
    public FDSNStationQueryParams getDefaultQueryParams() {
        return queryParams;
    }

    static void setTimeParamsToGetSingleChan(FDSNStationQueryParams staQP, Instant startTime, Instant endTime) {
        staQP.setStartBefore(startTime.plus(TimeUtils.ONE_SECOND));
        Instant end = endTime;
        if (end != null && end.isBefore(ClockUtil.now())) {
            staQP.setEndAfter(end.minus(TimeUtils.ONE_SECOND));
        }
    }

    /**
     * Checks if the given start and end times (maybe null) overlap the constraints (maybe null)
     * to avoid sending a query to the server that cannot possibly pass in the sod run.
     *
     * @param startTime
     * @param endTime
     * @param constraints
     * @return true if times overlap at all
     */
    static boolean checkTimeParamsOk(Instant startTime, Instant endTime, NetworkQueryConstraints constraints) {
        if (startTime.equals(endTime)) {
            // think this is ok, just ask for point in time
            //throw new SodSourceException("Station start and end times are equal!!! " + station);
        }
        if (endTime != null && startTime.isAfter(endTime)) {
            return false;
        }
        if (constraints.getConstrainingEndTime() != null &&
                constraints.getConstrainingEndTime().isBefore(startTime)) {
            return false;
        }
        if (constraints.getConstrainingBeginTime() != null && (
                endTime != null &&
                        constraints.getConstrainingBeginTime().isAfter(endTime))) {
            return false;
        }
        return true;
    }

    static void setTimeParams(FDSNStationQueryParams staQP, Instant startTime, Instant endTime, NetworkQueryConstraints constraints) {
        Instant earliest = startTime;
        Instant latest = endTime;

        if (constraints != null) {
            if (earliest == null || (
                    constraints.getConstrainingBeginTime() != null
                            && constraints.getConstrainingBeginTime().isAfter(earliest))) {
                earliest = constraints.getConstrainingBeginTime();
            }
            if (latest == null || (
                    constraints.getConstrainingEndTime() != null
                            && constraints.getConstrainingEndTime().isBefore(latest))) {
                latest = constraints.getConstrainingEndTime();
            }
        }
        if (earliest != null) {
            staQP.setEndAfter(earliest);
        }
        if (latest != null && latest.isBefore(ClockUtil.now())) {
            staQP.setStartBefore(latest);
        }
    }
    
    boolean includeAvailability = true;
    
    boolean validateXML = false;

    FDSNStationQueryParams queryParams = new FDSNStationQueryParams();
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FdsnStation.class);
}
