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
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.sc.seis.fissuresUtil.database.network.JDBCStation;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.StationDbObject;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;

public class VelocityStationGetter {

    public VelocityStationGetter(NetworkId net) {
        this.net = net;
    }

    public List getSuccessful() throws Exception {
        List out = new LinkedList();
        NetworkDbObject[] nets = Start.getNetworkArm().getSuccessfulNetworks();
        for (int i = 0; i < nets.length; i++) {
            if (NetworkIdUtil.areEqual(nets[i].getNetworkAccess().get_attributes().get_id(), net)) {
                StationDbObject[] sta = Start.getNetworkArm().getSuccessfulStations(nets[i]);
                for (int j = 0; j < sta.length; j++) {
                    out.add(sta[j]);
                }
                return out;
            }
        }
        // oh well
        return null;
    }

    public int getNumSuccessful(StationDbObject station) throws SQLException {
        success.setInt(1, station.getDbId());
        ResultSet rs = success.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    public int getNumFailed(StationDbObject station) throws SQLException {
        failed.setInt(1, station.getDbId());
        ResultSet rs = failed.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    public int getNumRetry(StationDbObject station) throws SQLException {
        retry.setInt(1, station.getDbId());
        ResultSet rs = retry.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    NetworkId net;

    private static JDBCEventChannelStatus evStatus;
    private static JDBCStation stationTable;
    private static PreparedStatement retry, failed, success;

    static{
        try {
            stationTable = new JDBCStation();
            evStatus = new JDBCEventChannelStatus();
            String baseStatement = "SELECT COUNT(*) FROM eventchannelstatus, channel, site WHERE " +
                "site.sta_id = ? AND " +
                "eventchannelstatus.channelid = channel.chan_id AND " +
                "channel.site_id = site.site_id";
            int pass = Status.get(Stage.PROCESSOR, Standing.SUCCESS).getAsShort();
            success = evStatus.prepareStatement(baseStatement + " AND status = " + pass);
            String failReq = JDBCEventChannelStatus.getFailedStatusRequest();
            failed = evStatus.prepareStatement(baseStatement + " AND " + failReq);
            String retryReq = JDBCEventChannelStatus.getRetryStatusRequest();
            retry = evStatus.prepareStatement(baseStatement + " AND " + retryReq);
        } catch (SQLException e) {
            GlobalExceptionHandler.handle(e);
        }
    }

}

