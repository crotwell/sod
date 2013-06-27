package edu.sc.seis.sod.source.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.InstrumentationImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.fissuresUtil.stationxml.ChannelSensitivityBundle;
import edu.sc.seis.fissuresUtil.stationxml.StationXMLToFissures;
import edu.sc.seis.seisFile.SeisFileException;
import edu.sc.seis.seisFile.fdsnws.FDSNStationQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNStationQueryParams;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.FDSNStationXML;
import edu.sc.seis.seisFile.fdsnws.stationxml.NetworkIterator;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationIterator;
import edu.sc.seis.sod.BuildVersion;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.subsetter.station.StationPointDistance;

public class FdsnStation extends AbstractNetworkSource {

    public FdsnStation() {
        super("default", -1);
    }
    
    public FdsnStation(String name, int retries, FDSNStationQueryParams queryParams) {
        super(name, retries);
        this.queryParams = queryParams;
    }

    public FdsnStation(Element config) throws Exception {
        super(config);
        queryParams.setIncludeRestricted(false);
        if (config != null) {
            // otherwise just use defaults
            int port = SodUtil.loadInt(config, "port", -1);
            if (port > 0) {
                queryParams.setPort(port);
            }
            NodeList childNodes = config.getChildNodes();
            for (int counter = 0; counter < childNodes.getLength(); counter++) {
                Node node = childNodes.item(counter);
                if (node instanceof Element) {
                    Element element = (Element)node;
                    if (element.getTagName().equals("stationBoxArea")) {
                        BoxArea a = SodUtil.loadBoxArea(element);
                        queryParams.area(a.min_latitude, a.max_latitude, a.min_longitude, a.max_longitude);
                    } else if (element.getTagName().equals("stationPointDistance")) {
                        StationPointDistance pd = (StationPointDistance)SodUtil.load(element, new String[] {"station"});
                        queryParams.donut((float)pd.getLatitude(), (float)pd.getLongitude(), (float)pd.getMin()
                                .getValue(UnitImpl.DEGREE), (float)pd.getMax().getValue(UnitImpl.DEGREE));
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
                    } else if (element.getTagName().equals("host")) {
                        String host = SodUtil.getNestedText(element);
                        queryParams.setHost(host);
                        this.name = host;
                    }
                }
            }
        }
    }

    @Override
    public CacheNetworkAccess getNetwork(NetworkAttrImpl attr) {
        return new CacheNetworkAccess(null, attr);
    }

    @Override
    public List<? extends CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound {
        throw new NetworkNotFound();
    }
    
    @Override
    public List<? extends NetworkAttrImpl> getNetworks() throws SodSourceException {
        try {
            FDSNStationQueryParams staQP = setupQueryParams();
            staQP.setLevel(FDSNStationQueryParams.LEVEL_NETWORK);
            staQP.clearChannel(); // channel constraints make getting networks very slow
            staQP.clearStartAfter().clearStartBefore().clearStartTime(); // start and end times also slow as 
            staQP.clearEndAfter().clearEndBefore().clearEndTime();       // applied to channel not network
            FDSNStationQuerier querier = setupQuerier(staQP);
            FDSNStationXML staxml = querier.getFDSNStationXML();
            List<NetworkAttrImpl> out = new ArrayList<NetworkAttrImpl>();
            NetworkIterator netIt = staxml.getNetworks();
            while (netIt.hasNext()) {
                edu.sc.seis.seisFile.fdsnws.stationxml.Network n = netIt.next();
                out.add(StationXMLToFissures.convert(n));
            }
            return out;
        } catch(SeisFileException e) {
            throw new SodSourceException(e);
        } catch(XMLStreamException e) {
            throw new SodSourceException(e);
        }
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkAttrImpl net) throws SodSourceException {
        try {
            FDSNStationQueryParams staQP = setupQueryParams();
            staQP.setLevel(FDSNStationQueryParams.LEVEL_STATION);
            staQP.clearNetwork();
            staQP.appendToNetwork(net.getId().network_code);
            staQP.setStartAfter(new MicroSecondDate(net.getBeginTime()));
            MicroSecondDate end = new MicroSecondDate(net.getEndTime());
            if (end.before(ClockUtil.now())) {
                staQP.setEndBefore(end);
            }
            FDSNStationQuerier querier = setupQuerier(staQP);
            FDSNStationXML staxml = querier.getFDSNStationXML();
            List<StationImpl> out = new ArrayList<StationImpl>();
            NetworkIterator netIt = staxml.getNetworks();
            while (netIt.hasNext()) {
                edu.sc.seis.seisFile.fdsnws.stationxml.Network n = netIt.next();
                NetworkAttrImpl netAttr = StationXMLToFissures.convert(n);
                StationIterator staIt = n.getStations();
                while (staIt.hasNext()) {
                    edu.sc.seis.seisFile.fdsnws.stationxml.Station s = staIt.next();
                    out.add(StationXMLToFissures.convert(s, netAttr));
                }
            }
            return out;
        } catch(SeisFileException e) {
            throw new SodSourceException(e);
        } catch(XMLStreamException e) {
            throw new SodSourceException(e);
        }
    }

    @Override
    public List<? extends ChannelImpl> getChannels(StationImpl station) throws SodSourceException  {
        try {
            FDSNStationQueryParams staQP = setupQueryParams();
            staQP.setLevel(FDSNStationQueryParams.LEVEL_CHANNEL);
            staQP.clearNetwork()
                    .appendToNetwork(station.getId().network_id.network_code)
                    .clearStation()
                    .appendToStation(station.getId().station_code);
            FDSNStationQuerier querier = setupQuerier(staQP);
            FDSNStationXML staxml = querier.getFDSNStationXML();
            List<ChannelImpl> out = new ArrayList<ChannelImpl>();
            NetworkIterator netIt = staxml.getNetworks();
            while (netIt.hasNext()) {
                edu.sc.seis.seisFile.fdsnws.stationxml.Network n = netIt.next();
                NetworkAttrImpl netAttr = StationXMLToFissures.convert(n);
                StationIterator staIt = n.getStations();
                while (staIt.hasNext()) {
                    edu.sc.seis.seisFile.fdsnws.stationxml.Station s = staIt.next();
                    StationImpl sImpl = StationXMLToFissures.convert(s, netAttr);
                    for (Channel c : s.getChannelList()) {
                        ChannelSensitivityBundle csb = StationXMLToFissures.convert(c, sImpl);
                        out.add(csb.getChan());
                        chanSensitivityMap.put(ChannelIdUtil.toString(csb.getChan().get_id()), csb.getSensitivity());
                    }
                }
            }
            return out;
        } catch(SeisFileException e) {
            throw new SodSourceException(e);
        } catch(XMLStreamException e) {
            throw new SodSourceException(e);
        }
    }

    @Override
    public QuantityImpl getSensitivity(ChannelImpl chan) throws ChannelNotFound, InvalidResponse, SodSourceException {
        String key = ChannelIdUtil.toString(chan.getId());
        if (!chanSensitivityMap.containsKey(key)) {
            getChannels(chan.getStationImpl());
        }
        if (!chanSensitivityMap.containsKey(key)) {
            throw new ChannelNotFound(chan.getId());
        }
        return chanSensitivityMap.get(key);
    }

    @Override
    public Instrumentation getInstrumentation(ChannelImpl chan) throws SodSourceException, ChannelNotFound, InvalidResponse  {
        try {
            FDSNStationQueryParams staQP = setupQueryParams();
            staQP.setLevel(FDSNStationQueryParams.LEVEL_RESPONSE);
            staQP.clearNetwork()
                    .appendToNetwork(chan.getId().network_id.network_code)
                    .clearStation()
                    .appendToStation(chan.getId().station_code)
                    .clearLocation()
                    .appendToLocation(chan.getId().site_code)
                    .clearChannel()
                    .appendToChannel(chan.getId().channel_code)
                    .setEndAfter(new MicroSecondDate(chan.getId().begin_time))
                    // ends after
                    .setEndTime(new MicroSecondDate(chan.getId().begin_time)); // starts
                                                                         // before
                                                                         // or
                                                                         // on
            FDSNStationQuerier querier = setupQuerier(staQP);
            FDSNStationXML staxml = querier.getFDSNStationXML();
            NetworkIterator netIt = staxml.getNetworks();
            while (netIt.hasNext()) {
                edu.sc.seis.seisFile.fdsnws.stationxml.Network n = netIt.next();
                NetworkAttrImpl netAttr = StationXMLToFissures.convert(n);
                StationIterator staIt = n.getStations();
                while (staIt.hasNext()) {
                    edu.sc.seis.seisFile.fdsnws.stationxml.Station s = staIt.next();
                    StationImpl sImpl = StationXMLToFissures.convert(s, netAttr);
                    for (Channel c : s.getChannelList()) {
                        ChannelSensitivityBundle csb = StationXMLToFissures.convert(c, sImpl);
                        chanSensitivityMap.put(ChannelIdUtil.toString(csb.getChan().get_id()), csb.getSensitivity());
                        InstrumentationImpl out = StationXMLToFissures.convertInstrumentation(c); // first
                                                                               // one
                                                                               // should
                                                                               // be
                                                                               // right
                        staxml.closeReader();
                        try {
                            querier.getInputStream().close();
                        } catch(IOException e) {
                            // oh well
                        }
                        return out;
                    }
                }
            }
            throw new ChannelNotFound();
        } catch(SeisFileException e) {
            throw new SodSourceException(e);
        } catch(XMLStreamException e) {
            throw new SodSourceException(e);
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
        querier.setUserAgent("SOD/"+BuildVersion.getVersion());
        return querier;
    }
    
    HashMap<String, QuantityImpl> chanSensitivityMap = new HashMap<String, QuantityImpl>();

    FDSNStationQueryParams queryParams = new FDSNStationQueryParams();
    
    int port = -1;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FdsnStation.class);
}
