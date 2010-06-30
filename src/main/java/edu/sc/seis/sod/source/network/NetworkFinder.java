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
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.cache.LazyNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.cache.VestingNetworkDC;
import edu.sc.seis.sod.Start;

public class NetworkFinder extends AbstractNetworkSource {

    public NetworkFinder(String dns, String name, int retries) {
        super(dns, name, retries);
    }

    public NetworkFinder(Element element) throws Exception {
        super(element);
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
        return new LazyNetworkAccess(attr, getNetworkDC());
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
    public synchronized List<CacheNetworkAccess> getNetworks() {
        ProxyNetworkDC netDC = getNetworkDC();
        // purge cache before loading from server
        netDC.reset();
        if (getConstrainingNetworkCodes().length > 0) {
            edu.iris.Fissures.IfNetwork.NetworkFinder netFinder = netDC.a_finder();
            List<CacheNetworkAccess> constrainedNets = new ArrayList<CacheNetworkAccess>();
            for (int i = 0; i < getConstrainingNetworkCodes().length; i++) {
                CacheNetworkAccess[] found = null;
                // this is a bit of a hack as names could be one or two
                // characters, but works with _US-TA style
                // virtual networks at the DMC
                try {
                    if (getConstrainingNetworkCodes()[i].length() > 2) {
                        found = (CacheNetworkAccess[])netFinder.retrieve_by_name(getConstrainingNetworkCodes()[i]);
                    } else {
                        found = (CacheNetworkAccess[])netFinder.retrieve_by_code(getConstrainingNetworkCodes()[i]);
                    }
                } catch(NetworkNotFound e) {
                    // this probably indicates a bad conf file, warn and exit
                    Start.informUserOfBadNetworkAndExit(getConstrainingNetworkCodes()[i], e);
                }
                for (int j = 0; j < found.length; j++) {
                    constrainedNets.add(found[j]);
                }
            }
            return constrainedNets;
        } else {
            NetworkAccess[] tmpNets = netDC.a_finder().retrieve_all();
            ArrayList<CacheNetworkAccess> goodNets = new ArrayList<CacheNetworkAccess>();
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
            return goodNets;
        }
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
        CacheNetworkAccess net = getNetwork(station.getId().network_id);
        Channel[] tmpChannels = net.retrieve_for_station(station.get_id());
        MicroSecondDate stationBegin = new MicroSecondDate(station.getBeginTime());
        // dmc network server ignores date in station id in
        // retrieve_for_station, so all channels for station code are
        // returned. This checks to make sure the station is the same.
        // ProxyNetworkAccess already interns stations in channels
        // so as long as station begin times are the same, they are
        // equal...we hope
        ArrayList<ChannelImpl> chansAtStation = new ArrayList<ChannelImpl>();
        for (int i = 0; i < tmpChannels.length; i++) {
            if (new MicroSecondDate(tmpChannels[i].getSite().getStation().getBeginTime()).equals(stationBegin)) {
                chansAtStation.add((ChannelImpl)tmpChannels[i]);
            } else {
                logger.info("Channel " + ChannelIdUtil.toString(tmpChannels[i].get_id())
                        + " has a station that is not the same as the requested station: req="
                        + StationIdUtil.toString(station.get_id()) + "  chan sta="
                        + StationIdUtil.toString(tmpChannels[i].getSite().getStation()) + "  "
                        + tmpChannels[i].getSite().getStation().getBeginTime().date_time + " != "
                        + station.getBeginTime().date_time);
            }
        }
        return chansAtStation;
    }
    
    public CacheNetworkAccess getNetwork(NetworkId netId)  {
        try {
        return (CacheNetworkAccess)getNetworkDC().a_finder().retrieve_by_id(netId);
        } catch (NetworkNotFound e) {
            throw new RuntimeException("don't think this should happen as we must have gotten the netid from the server", e);
        }
    }

    @Override
    public Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InstrumentationInvalid {
        return getNetwork(chanId.network_id).retrieve_instrumentation(chanId, chanId.begin_time);
    }

    @Override
    public Sensitivity getSensitivity(ChannelId chanId) throws ChannelNotFound, InstrumentationInvalid {
        return getNetwork(chanId.network_id).retrieve_sensitivity(chanId, chanId.begin_time);
    }

    protected VestingNetworkDC netDC;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(NetworkFinder.class);
}// NetworkFinder
