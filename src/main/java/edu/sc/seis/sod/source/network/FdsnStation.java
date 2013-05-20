package edu.sc.seis.sod.source.network;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
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
        if (config != null) {
            // otherwise just use defaults
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
                    } else if (element.getTagName().equals("host")) {
                        queryParams.setHost(SodUtil.getNestedText(element));
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
    public List<? extends NetworkAttrImpl> getNetworks() {
        try {
            FDSNStationQueryParams staQP = setupQueryParams();
            staQP.setLevel(FDSNStationQueryParams.LEVEL_NETWORK);
            FDSNStationQuerier querier = new FDSNStationQuerier(staQP);
            querier.setUserAgent("SOD/"+BuildVersion.getVersion());
            FDSNStationXML staxml = querier.getFDSNStationXML();
            List<NetworkAttrImpl> out = new ArrayList<NetworkAttrImpl>();
            NetworkIterator netIt = staxml.getNetworks();
            while (netIt.hasNext()) {
                edu.sc.seis.seisFile.fdsnws.stationxml.Network n = netIt.next();
                out.add(StationXMLToFissures.convert(n));
            }
            return out;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkId net) {
        try {
            FDSNStationQueryParams staQP = setupQueryParams();
            staQP.setLevel(FDSNStationQueryParams.LEVEL_STATION);
            staQP.clearNetwork();
            staQP.appendToNetwork(net.network_code);
            FDSNStationQuerier querier = new FDSNStationQuerier(staQP);
            querier.setUserAgent("SOD/"+BuildVersion.getVersion());
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
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<? extends ChannelImpl> getChannels(StationImpl station) {
        return getChannels(station.get_id());
    }

    public List<? extends ChannelImpl> getChannels(StationId stationId) {
        try {
            FDSNStationQueryParams staQP = setupQueryParams();
            staQP.setLevel(FDSNStationQueryParams.LEVEL_CHANNEL);
            staQP.clearNetwork()
                    .appendToNetwork(stationId.network_id.network_code)
                    .clearStation()
                    .appendToStation(stationId.station_code);
            FDSNStationQuerier querier = new FDSNStationQuerier(staQP);
            querier.setUserAgent("SOD/"+BuildVersion.getVersion());
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
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public QuantityImpl getSensitivity(ChannelId chanId) throws ChannelNotFound, InvalidResponse {
        String key = ChannelIdUtil.toString(chanId);
        if (!chanSensitivityMap.containsKey(key)) {
            StationId sId = new StationId(chanId.network_id, chanId.station_code, chanId.begin_time);
            getChannels(sId);
        }
        if (!chanSensitivityMap.containsKey(key)) {
            throw new ChannelNotFound(chanId);
        }
        return chanSensitivityMap.get(key);
    }

    @Override
    public Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InvalidResponse {
        try {
            FDSNStationQueryParams staQP = setupQueryParams();
            staQP.setLevel(FDSNStationQueryParams.LEVEL_RESPONSE);
            staQP.clearNetwork()
                    .appendToNetwork(chanId.network_id.network_code)
                    .clearStation()
                    .appendToStation(chanId.station_code)
                    .clearLocation()
                    .appendToLocation(chanId.site_code)
                    .clearChannel()
                    .appendToChannel(chanId.channel_code)
                    .setEndAfter(new MicroSecondDate(chanId.begin_time))
                    // ends after
                    .setEndTime(new MicroSecondDate(chanId.begin_time)); // starts
                                                                         // before
                                                                         // or
                                                                         // on
            FDSNStationQuerier querier = new FDSNStationQuerier(staQP);
            querier.setUserAgent("SOD/"+BuildVersion.getVersion());
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
                        return StationXMLToFissures.convertInstrumentation(c); // first
                                                                               // one
                                                                               // should
                                                                               // be
                                                                               // right
                    }
                }
            }
            throw new ChannelNotFound();
        } catch(SeisFileException e) {
            throw new InvalidResponse(e);
        } catch(XMLStreamException e) {
            throw new InvalidResponse(e);
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
    
    HashMap<String, QuantityImpl> chanSensitivityMap = new HashMap<String, QuantityImpl>();

    FDSNStationQueryParams queryParams = new FDSNStationQueryParams();
}
