package edu.sc.seis.sod.source.network;

import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA.BAD_PARAM;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.VirtualNetworkHelper;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.cache.VestingNetworkDC;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.InstrumentationDBNetworkAccess;

public class NetworkFinder extends NetworkSource {


    public NetworkFinder(String dns, String name, int retries) {
        super(dns, name, retries);
    }
    
    public NetworkFinder(Element element) throws Exception {
        super(element);
    }

    public synchronized ProxyNetworkDC getNetworkDC() {
        if(netDC == null) {
            netDC = new VestingNetworkDC(getDNS(),
                                         getName(),
                                         getFissuresNamingService(),
                                         Start.createRetryStrategy(getRetries()));
        }
        return netDC;
    }

    @Override
    public List<CacheNetworkAccess> getNetworks() {
        if(constrainingCodes.length > 0) {
            edu.iris.Fissures.IfNetwork.NetworkFinder netFinder = netDC.a_finder();
            List<CacheNetworkAccess> constrainedNets = new ArrayList<CacheNetworkAccess>(constrainingCodes.length);
            for(int i = 0; i < constrainingCodes.length; i++) {
                CacheNetworkAccess[] found = null;
                // this is a bit of a hack as names could be one or two
                // characters, but works with _US-TA style
                // virtual networks at the DMC
                try {
                if(constrainingCodes[i].length() > 2) {
                    found = (CacheNetworkAccess[])netFinder.retrieve_by_name(constrainingCodes[i]);
                } else {
                    found = (CacheNetworkAccess[])netFinder.retrieve_by_code(constrainingCodes[i]);
                }

                } catch(NetworkNotFound e) {
                    // this probably indicates a bad conf file, warn and exit
                    Start.informUserOfBadNetworkAndExit(constrainingCodes[i], e);
                }
                for(int j = 0; j < found.length; j++) {
                    constrainedNets.add(found[j]);
                }
            }
            return constrainedNets;
        } else {
            NetworkAccess[] tmpNets = netDC.a_finder().retrieve_all();
            ArrayList<CacheNetworkAccess> goodNets = new ArrayList<CacheNetworkAccess>();
            for(int i = 0; i < tmpNets.length; i++) {
                try {
                    VirtualNetworkHelper.narrow(tmpNets[i]);
                    // Ignore any virtual nets returned here
                    logger.debug("ignoring virtual network "
                            + tmpNets[i].get_attributes().get_code());
                    continue;
                } catch(BAD_PARAM bp) {
                    // Must be a concrete, keep it
                    goodNets.add(new InstrumentationDBNetworkAccess((CacheNetworkAccess)tmpNets[i]));
                }
            }
            return goodNets;
        }
    }

    @Override
    public List<StationImpl> getStations(CacheNetworkAccess net) {
        StationImpl[] stations = StationImpl.implize(net.retrieve_stations());
        List<StationImpl> out = new ArrayList<StationImpl>(stations.length);
        for (int i = 0; i < stations.length; i++) {
            out.add(stations[i]);
        }
        return out;
    }

    @Override
    public List<ChannelImpl> getChannels(CacheNetworkAccess net, StationImpl station) {
        Channel[] tmpChannels = net.retrieve_for_station(station.get_id());
        MicroSecondDate stationBegin = new MicroSecondDate(station.getBeginTime());
        // dmc network server ignores date in station id in
        // retrieve_for_station, so all channels for station code are
        // returned. This checks to make sure the station is the same.
        // ProxyNetworkAccess already interns stations in channels
        // so as long as station begin times are the same, they are
        // equal...we hope
        ArrayList<ChannelImpl> chansAtStation = new ArrayList<ChannelImpl>();
        for(int i = 0; i < tmpChannels.length; i++) {
            if(new MicroSecondDate(tmpChannels[i].getSite().getStation().getBeginTime()).equals(stationBegin)) {
                chansAtStation.add((ChannelImpl)tmpChannels[i]);
            } else {
                logger.info("Channel "
                        + ChannelIdUtil.toString(tmpChannels[i].get_id())
                        + " has a station that is not the same as the requested station: req="
                        + StationIdUtil.toString(station.get_id())
                        + "  chan sta="
                        + StationIdUtil.toString(tmpChannels[i].getSite()
                                .getStation())+"  "+tmpChannels[i].getSite().getStation().getBeginTime().date_time+" != "+station.getBeginTime().date_time);
            }
        }
        return chansAtStation;
    }
    
    private VestingNetworkDC netDC;
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(NetworkFinder.class);
}// NetworkFinder
