package edu.sc.seis.sod.database.waveform;

import java.sql.*;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.sod.database.SodJDBC;

public class JDBCEventChannelRetry extends SodJDBC{
    public JDBCEventChannelRetry() throws SQLException{
        Connection conn = ConnMgr.getConnection();
        if(!DBUtil.tableExists("eventchannelretry", conn)){
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("eventchannelretry.create"));
        }
        insertFailure = conn.prepareStatement("INSERT into eventchannelretry (pairid, numfailure) VALUES (?, ?)");
        updateFailure = conn.prepareStatement("UPDATE eventchannelretry SET status = ?, numfailure = numfailure + 1, retry_time = ? WHERE pairid = ?");
        selectPair = conn.prepareStatement("SELECT numfailure FROM eventchannelretry WHERE pairid = ?");
        selectNext = conn.prepareStatement("SELECT pairid, retry_time FROM eventchannelretry " +
                                               "WHERE status = " + EventChannelCondition.CORBA_FAILURE.getNumber() +
                                               " OR status = " + EventChannelCondition.NO_AVAILABLE_DATA.getNumber() +
                                               " ORDER BY retry_time DESC");
        clearAll = conn.prepareStatement("DELETE FROM eventchannelretry");
        updateStatus = conn.prepareStatement("UPDATE eventchannelretry SET status = ? WHERE pairid = ?");
    }

    private PreparedStatement insertFailure, updateFailure, selectPair, selectNext,
        clearAll, updateStatus;

    public void clear() throws SQLException {
        clearAll.executeUpdate();
    }

    public static TimeInterval getRetryDelay(int numFailures) {
        if(numFailures <= 0) throw new IllegalArgumentException("Failures must be greater than or equal to 1");
        long millis = BASE_DELAY;
        for(int i = numFailures; i > 1; i--) millis *= 2;
        TimeInterval retryDelay = new TimeInterval(millis, UnitImpl.MILLISECOND);
        if(retryDelay.greaterThan(MAX_DELAY)) return MAX_DELAY;
        return retryDelay;
    }


    public int next() throws SQLException {
        ResultSet rs = selectNext.executeQuery();
        while(rs.next()){
            if(rs.getTimestamp("retry_time").after(ClockUtil.now())) continue;
            updateStatus.setInt(1, EventChannelCondition.RETRY.getNumber());
            updateStatus.setInt(2, rs.getInt("pairid"));
            updateStatus.executeUpdate();
            return rs.getInt("pairid");
        }
        return -1;
    }

    public void failed(int pairId, EventChannelCondition failureType) throws SQLException{
        if(!tableContains(pairId)){
            insert(pairId);
        }
        updateFailure.setInt(1, failureType.getNumber());
        updateFailure.setTimestamp(2, getTimestamp(pairId));
        updateFailure.setInt(3, pairId);
        updateFailure.executeUpdate();
    }

    private Timestamp getTimestamp(int pairId) throws SQLException {
        int numFailures = getNumFailure(pairId);
        TimeInterval retryLength = getRetryDelay(numFailures);
        MicroSecondDate retryTime = ClockUtil.now().add(retryLength);
        return retryTime.getTimestamp();
    }

    private int getNumFailure(int pairId) throws SQLException {
        selectPair.setInt(1, pairId);
        ResultSet rs = selectPair.executeQuery();
        if(rs.next())return rs.getInt("numfailure") + 1;
        return -1;
    }

    private void insert(int pairId) throws SQLException {
        insertFailure.setInt(1, pairId);
        insertFailure.setInt(2, 0);
        insertFailure.executeUpdate();
    }

    private boolean tableContains(int pairId) throws SQLException {
        if(getNumFailure(pairId) == -1)return false;
        return true;
    }

    public static void setBaseDelay(TimeInterval base){
        BASE_DELAY = (long)base.convertTo(UnitImpl.MILLISECOND).getValue();
    }

    public static void setMaxDelay(TimeInterval delay){ MAX_DELAY = delay; }

    private static long BASE_DELAY =  84375;//this * 2^10 = 86400000, or the number of millis in a day
    private static TimeInterval MAX_DELAY = new TimeInterval(1, UnitImpl.DAY);
}

