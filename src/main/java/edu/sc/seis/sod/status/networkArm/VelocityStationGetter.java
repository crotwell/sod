/**
 * VelocityStationGetter.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.networkArm;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.SodDB;

public class VelocityStationGetter {

    public VelocityStationGetter(NetworkId net) {
        this.net = net;
    }

    public List<StationImpl> getSuccessful() throws Exception {
        List<StationImpl> out = new LinkedList<StationImpl>();
        List<NetworkAttrImpl> nets = Start.getNetworkArm().getSuccessfulNetworks();
        for (NetworkAttrImpl cachenet : nets) {
            if (NetworkIdUtil.areEqual(cachenet.get_id(), net)) {
                StationImpl[] sta = Start.getNetworkArm().getSuccessfulStations(cachenet);
                for (int j = 0; j < sta.length; j++) {
                    out.add(sta[j]);
                }
                return out;
            }
        }
        // oh well
        return null;
    }

    NetworkId net;
    
    static SodDB sodDb = SodDB.getSingleton();

    public int getNumSuccessful(StationImpl station) throws SQLException {
    	return sodDb.getNumSuccessful(station);
    }

    public int getNumFailed(StationImpl station) throws SQLException {
    	return sodDb.getNumFailed(station);
    }

    public int getNumRetry(StationImpl station) throws SQLException {
    	return sodDb.getNumRetry(station);
    }
}

