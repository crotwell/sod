package edu.sc.seis.sod.subsetter.station;

import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinderOperations;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class BelongsToVirtual implements StationSubsetter {

    private static NetworkAccess getVirtual(Element el)
            throws ConfigurationException {
        return getVirtual(SodUtil.getNestedText(el));
    }

    private static NetworkAccess getVirtual(String name)
            throws ConfigurationException {
        try {
            List<CacheNetworkAccess> nets = Start.getNetworkArm().getNetworkSource().getNetworkByName(name);
            if(nets.size() > 1) {
                throw new ConfigurationException("There are several nets with the name "
                        + name);
            }
            return nets.get(0);
        } catch(NetworkNotFound nnf) {
            throw new UserConfigurationException("No network by the name of "
                    + name + " found");
        }
    }

    public BelongsToVirtual(Element el) throws ConfigurationException {
        this(getVirtual(el), Start.getNetworkArm().getRefreshInterval());
    }

    public BelongsToVirtual(NetworkAccess virtualNet,
                            TimeInterval refreshInterval) {
        this.net = virtualNet;
        this.refreshInterval = refreshInterval;
    }

    public StringTree accept(StationImpl station, NetworkAccess network) {
        refreshStations();
        for(int i = 0; i < stations.length; i++) {
            if(StationIdUtil.areEqual(station, stations[i])) {
                return new Pass(this);
            }
        }
        return new Fail(this);
    }

    private void refreshStations() {
        if(ClockUtil.now().subtract(refreshInterval).after(lastQuery)) {
            lastQuery = ClockUtil.now();
            stations = net.retrieve_stations();
        }
    }

    private Station[] stations;

    private TimeInterval refreshInterval;

    private MicroSecondDate lastQuery = new MicroSecondDate(0);

    private NetworkAccess net;
}
