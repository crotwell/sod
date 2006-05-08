package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinderOperations;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.UserConfigurationException;

public class BelongsToVirtual implements StationSubsetter {

    private static NetworkAccess getVirtual(Element el)
            throws ConfigurationException {
        NetworkFinderOperations finder = Start.getNetworkArm()
                .getNetworkDC()
                .a_finder();
        return getVirtual(SodUtil.getNestedText(el), finder);
    }

    private static NetworkAccess getVirtual(String name,
                                            NetworkFinderOperations finder)
            throws ConfigurationException {
        try {
            NetworkAccess[] nets = finder.retrieve_by_name(name);
            if(nets.length > 1) {
                throw new ConfigurationException("There are several nets with the name "
                        + name);
            }
            return nets[0];
        } catch(NetworkNotFound nnf) {
            throw new UserConfigurationException("No network by the name of "
                    + name + " found");
        }
    }

    public BelongsToVirtual(Element el) throws ConfigurationException {
        this(getVirtual(el), Start.getNetworkArm().getRefreshInterval());
    }

    public BelongsToVirtual(NetworkFinderOperations operations, String name)
            throws ConfigurationException {
        this(getVirtual(name, operations), new TimeInterval(1,
                                                            UnitImpl.FORTNIGHT));
    }

    public BelongsToVirtual(NetworkAccess virtualNet,
                            TimeInterval refreshInterval) {
        this.net = virtualNet;
        this.refreshInterval = refreshInterval;
    }

    public boolean accept(Station station, NetworkAccess network) {
        refreshStations();
        for(int i = 0; i < stations.length; i++) {
            if(StationIdUtil.areEqual(station, stations[i])) {
                return true;
            }
        }
        return false;
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
