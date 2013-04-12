package edu.sc.seis.sod.source.network;

import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA.BAD_PARAM;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.IfNetwork.VirtualNetworkHelper;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.cache.VestingNetworkDC;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.Start;

public class NetworkFinder extends AbstractNetworkSource {

    public NetworkFinder(String dns, String name, int retries) {
        super(dns, name, retries);
    }

    public NetworkFinder(Element element) throws Exception {
        super(element);
        if (getDNS().equals("edu/iris/dmc")) {
            System.err.println("WARNING: DHI servers will be turned off June 2013, switch to <stationXML>");
        }
    }

    public synchronized ProxyNetworkDC getNetworkDC() {
        if (netDC == null) {
            netDC = new VestingNetworkDC(getDNS(),
                                         getName(),
                                         getFissuresNamingService(),
                                         Start.createRetryStrategy(getRetries()));
        }
        return netDC;
    }

    public CacheNetworkAccess getNetwork(NetworkAttrImpl attr) {
        return getNetwork(attr.getId());
    }

    public CacheNetworkAccess getNetwork(NetworkId netId)  {
        CacheNetworkAccess net = checkCache(netId);
        if (net == null) {
            if (netId.network_code.startsWith("_")) {
                // virtual network?
                try {
                    List<CacheNetworkAccess> byName = getNetworkByName(netId.network_code);
                    if (byName.size() != 0) {
                        byNameCache.add(byName.get(0));
                        return byName.get(0);
                    }
                    throw new RuntimeException("Can't get network by name: "+netId.network_code);
                } catch(NetworkNotFound e) {
                    throw new RuntimeException("Can't get network from cache or by name: "+netId.network_code);
                }
            }
            throw new RuntimeException("can't find net, should neven happen: "+NetworkIdUtil.toString(netId));
        }
        return net;
    }

    public List<CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound {
        List<CacheNetworkAccess> out = new ArrayList<CacheNetworkAccess>();
        NetworkAccess[] array = getNetworkDC().a_finder().retrieve_by_name(name);
        for (int i = 0; i < array.length; i++) {
            out.add(new CacheNetworkAccess(array[i]));
        }
        return out;
    }

    @Override
    public synchronized List<? extends NetworkAttrImpl> getNetworks() {
        if (recentNetworksCache == null) {
            getNetworksInternal();
        }
        List<NetworkAttrImpl> out = new ArrayList<NetworkAttrImpl>();
        for (CacheNetworkAccess netAccess : recentNetworksCache) {
            out.add(netAccess.get_attributes());
        }
        return out;
    }

    public synchronized List<CacheNetworkAccess> getNetworksInternal() {
        String[] constrainingCodes = Start.getNetworkArm().getConstrainingNetworkCodes();
        ProxyNetworkDC netDC = getNetworkDC();
        // purge cache before loading from server
        netDC.reset();
        ArrayList<CacheNetworkAccess> goodNets = new ArrayList<CacheNetworkAccess>();
        if (constrainingCodes.length > 0) {
            edu.iris.Fissures.IfNetwork.NetworkFinder netFinder = netDC.a_finder();
            for (int i = 0; i < constrainingCodes.length; i++) {
                CacheNetworkAccess[] found = null;
                // this is a bit of a hack as names could be one or two
                // characters, but works with _US-TA style
                // virtual networks at the DMC
                try {
                    if (constrainingCodes[i].length() > 2) {
                        found = (CacheNetworkAccess[])netFinder.retrieve_by_name(constrainingCodes[i]);
                    } else {
                        found = (CacheNetworkAccess[])netFinder.retrieve_by_code(constrainingCodes[i]);
                    }
                } catch(NetworkNotFound e) {
                    // this probably indicates a bad conf file, warn and exit
                    Start.informUserOfBadNetworkAndExit(constrainingCodes[i], e);
                }
                for (int j = 0; j < found.length; j++) {
                    goodNets.add(found[j]);
                }
            }
        } else {
            NetworkAccess[] tmpNets = netDC.a_finder().retrieve_all();
            for (int i = 0; i < tmpNets.length; i++) {
                try {
                    VirtualNetworkHelper.narrow(tmpNets[i]);
                    // Ignore any virtual nets returned here
                    logger.debug("ignoring virtual network " + tmpNets[i].get_attributes().get_code());
                    continue;
                } catch(BAD_PARAM bp) {
                    // Must be a concrete, keep it
                    goodNets.add((CacheNetworkAccess)tmpNets[i]);
                }
            }
        }
        this.recentNetworksCache = goodNets;
        return goodNets;
    }

    @Override
    public List<StationImpl> getStations(NetworkId netId) {
        CacheNetworkAccess net = getNetwork(netId);
        StationImpl[] stations = StationImpl.implize(net.retrieve_stations());
        List<StationImpl> out = new ArrayList<StationImpl>(stations.length);
        for (int i = 0; i < stations.length; i++) {
            out.add(stations[i]);
        }
        return out;
    }

    @Override
    public List<ChannelImpl> getChannels(StationImpl station) {
        if (station == null) {throw new NullPointerException("station cannot be null");}
        CacheNetworkAccess net = getNetwork(station.getNetworkAttrImpl());
        Channel[] tmpChannels = net.retrieve_for_station(station.get_id());
        return checkStationTimeOverlap(station, tmpChannels);
    }
    
    @Override
    public Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InvalidResponse {
        return getNetwork(chanId.network_id).retrieve_instrumentation(chanId, chanId.begin_time);
    }

    @Override
    public QuantityImpl getSensitivity(ChannelId chanId) throws ChannelNotFound, InvalidResponse {
        CacheNetworkAccess cna = getNetwork(chanId.network_id);
        Sensitivity s = cna.retrieve_sensitivity(chanId, chanId.begin_time);
        return new QuantityImpl(s.sensitivity_factor, cna.retrieve_initial_units(chanId, chanId.begin_time));
    }
    
    protected List<ChannelImpl> checkStationTimeOverlap(StationImpl station, Channel[] inChannels) {
        MicroSecondDate stationBegin = new MicroSecondDate(station.getBeginTime());
        // dmc network server ignores date in station id in
        // retrieve_for_station, so all channels for station code are
        // returned. This checks to make sure the station is the same.
        // ProxyNetworkAccess already interns stations in channels
        // so as long as station begin times are the same, they are
        // equal...we hope
        ArrayList<ChannelImpl> chansAtStation = new ArrayList<ChannelImpl>();
        for (int i = 0; i < inChannels.length; i++) {
            if (new MicroSecondDate(inChannels[i].getSite().getStation().getBeginTime()).equals(stationBegin)) {
                chansAtStation.add((ChannelImpl)inChannels[i]);
            } else {
                logger.info("Channel " + ChannelIdUtil.toString(inChannels[i].get_id())
                        + " has a station that is not the same as the requested station: req="
                        + StationIdUtil.toString(station.get_id()) + "  chan sta="
                        + StationIdUtil.toString(inChannels[i].getSite().getStation()) + "  "
                        + inChannels[i].getSite().getStation().getBeginTime().date_time + " != "
                        + station.getBeginTime().date_time);
            }
        }
        return chansAtStation;
    }
    
    protected CacheNetworkAccess checkCache(NetworkId netId) {
        if (recentNetworksCache == null) {
            getNetworksInternal();
        }
        for (CacheNetworkAccess net : recentNetworksCache) {
            if (NetworkIdUtil.areEqual(netId, net.get_attributes().getId())) {
                return net;
            }
        }
        for (CacheNetworkAccess net : byNameCache) {
            if (NetworkIdUtil.areEqual(netId, net.get_attributes().getId())) {
                return net;
            }
        }
        return null;
    }
    
    public void reset() {
        recentNetworksCache = null;
        byNameCache.clear();
    }
    
    protected List<CacheNetworkAccess> recentNetworksCache = null;
    
    protected List<CacheNetworkAccess> byNameCache = new ArrayList<CacheNetworkAccess>();
    
    protected VestingNetworkDC netDC;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkFinder.class);
}// NetworkFinder
