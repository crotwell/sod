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
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.SodDB;

public class VelocityStationGetter {

    public VelocityStationGetter(NetworkId net) {
        this.net = net;
    }

    public List getSuccessful() throws Exception {
        List out = new LinkedList();
        CacheNetworkAccess[] nets = Start.getNetworkArm().getSuccessfulNetworks();
        for (int i = 0; i < nets.length; i++) {
            if (NetworkIdUtil.areEqual(nets[i].get_attributes().get_id(), net)) {
                StationImpl[] sta = Start.getNetworkArm().getSuccessfulStations(nets[i].get_attributes());
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

