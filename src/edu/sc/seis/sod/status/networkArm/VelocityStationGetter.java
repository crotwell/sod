/**
 * VelocityStationGetter.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.networkArm;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.StationDbObject;
import java.util.LinkedList;
import java.util.List;

public class VelocityStationGetter {

    public VelocityStationGetter(NetworkAccess net) {
        this.net = net;
    }

    public List getSuccessful() throws Exception {
        List out = new LinkedList();
        NetworkDbObject[] nets = Start.getNetworkArm().getSuccessfulNetworks();
        for (int i = 0; i < nets.length; i++) {
            if (NetworkIdUtil.areEqual(nets[i].getNetworkAccess().get_attributes().get_id(),
                                       net.get_attributes().get_id())) {
                StationDbObject[] sta = Start.getNetworkArm().getSuccessfulStations(nets[i]);
                for (int j = 0; j < sta.length; j++) {
                    out.add(sta[j].getStation());
                }
                return out;
            }
        }
        // oh well
        return null;
    }

    NetworkAccess net;

}

