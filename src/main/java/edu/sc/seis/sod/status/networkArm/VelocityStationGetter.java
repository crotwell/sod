/**
 * VelocityStationGetter.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.networkArm;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.model.station.NetworkId;
import edu.sc.seis.sod.model.station.NetworkIdUtil;

public class VelocityStationGetter {

    public VelocityStationGetter(NetworkId net) {
        this.net = net;
    }

    public List<Station> getSuccessful() throws Exception {
        List<Station> out = new LinkedList<Station>();
        List<Network> nets = Start.getNetworkArm().getSuccessfulNetworks();
        for (Network cachenet : nets) {
            if (NetworkIdUtil.areEqual(cachenet, net)) {
                Station[] sta = Start.getNetworkArm().getSuccessfulStations(cachenet);
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

    public int getNumSuccessful(Station station) throws SQLException {
    	return sodDb.getNumSuccessful(station);
    }

    public int getNumFailed(Station station) throws SQLException {
    	return sodDb.getNumFailed(station);
    }

    public int getNumRetry(Station station) throws SQLException {
    	return sodDb.getNumRetry(station);
    }
}

