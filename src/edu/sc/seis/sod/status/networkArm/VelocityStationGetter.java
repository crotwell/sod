/**
 * VelocityStationGetter.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.networkArm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.database.network.JDBCStation;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.StationDbObject;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;

public class VelocityStationGetter {

    public VelocityStationGetter(NetworkId net) {
        this.net = net;
    }

    public List getSuccessful() throws Exception {
        List out = new LinkedList();
        CacheNetworkAccess[] nets = Start.getNetworkArm().getSuccessfulNetworks();
        for (int i = 0; i < nets.length; i++) {
            if (NetworkIdUtil.areEqual(nets[i].get_attributes().get_id(), net)) {
                StationImpl[] sta = Start.getNetworkArm().getSuccessfulStations(nets[i]);
                for (int j = 0; j < sta.length; j++) {
                    out.add(sta[j]);
                }
                return out;
            }
        }
        // oh well
        return null;
    }

    public int getNumSuccessful(Station station) throws SQLException {
        success.setInt(1, station.getDbId());
        ResultSet rs = success.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    public int getNumFailed(Station station) throws SQLException {
        failed.setInt(1, station.getDbId());
        ResultSet rs = failed.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    public int getNumRetry(Station station) throws SQLException {
        retry.setInt(1, station.getDbId());
        ResultSet rs = retry.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    NetworkId net;

    private static String retry, failed, success;

    static{
            String baseStatement = "SELECT COUNT(*) FROM edu.sc.seis.sod.EventChannelPair ecp WHERE " +
                "ecp.channel.site.station = :sta " ;
            int pass = Status.get(Stage.PROCESSOR, Standing.SUCCESS).getAsShort();
            success = baseStatement + " AND status = " + pass;
            String failReq = JDBCEventChannelStatus.getFailedStatusRequest();
            failed = baseStatement + " AND " + failReq;
            String retryReq = JDBCEventChannelStatus.getRetryStatusRequest();
            retry = baseStatement + " AND " + retryReq;
    }

}

