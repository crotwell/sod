package edu.sc.seis.sod.database.waveform;

import java.sql.*;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.SodJDBC;

/** This table stores a pairid, a number of failures, and the time of the next
 * retry.  The time of the next retry is set to ClockUtil.future() whenever
 * a retry is attempted on a pairid.  You can see which retries have succeeded by
 * joining on all pairids with a retry_time of future and a status of success in
 * EventChannelStatus
 */
public class JDBCEventChannelRetry extends SodJDBC{
    public JDBCEventChannelRetry() throws SQLException{
        Connection conn = ConnMgr.createConnection();
        if(!DBUtil.tableExists("eventchannelretry", conn)){
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("eventchannelretry.create"));
        }
        insert = conn.prepareStatement("INSERT into eventchannelretry (pairid, numfailure) VALUES (?, ?)");
        updateFailure = conn.prepareStatement("UPDATE eventchannelretry SET numfailure = numfailure + 1, retry_time = ? WHERE pairid = ?");
        selectPair = conn.prepareStatement("SELECT numfailure FROM eventchannelretry WHERE pairid = ?");
        selectNext = conn.prepareStatement("SELECT TOP 1 pairid, retry_time FROM eventchannelretry " +
                                               "WHERE retry_time < ?");
        selectNext.setTimestamp(1, ClockUtil.future().getTimestamp());
        selectRemainingAvailData = conn.prepareStatement("SELECT TOP 1 pairid, retry_time FROM eventchannelretry, eventchannelstatus " +
                                                             "WHERE retry_time < ? AND "+
                                                             "status = ?");
        selectRemainingCorbaFailures = conn.prepareStatement("SELECT TOP 1 pairid, retry_time FROM eventchannelretry, eventchannelstatus " +
                                                             "WHERE retry_time < ? AND "+
                                                             "status IN (?, ?, ?)");
        startRetry = conn.prepareStatement("UPDATE eventchannelretry SET retry_time = ? WHERE pairid = ?");
    }

    private PreparedStatement insert, updateFailure, selectPair, selectNext,
        startRetry, selectRemainingAvailData, selectRemainingCorbaFailures;

    public boolean availableDataRetriesRemain() throws SQLException {
        selectRemainingAvailData.setTimestamp(1, ClockUtil.future().getTimestamp());
        selectRemainingAvailData.setInt(2, Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.RETRY).getAsShort());
        ResultSet rs = selectRemainingAvailData.executeQuery();
        return rs.next();
    }

    public boolean serverFailureRetriesRemain() throws SQLException {
        selectRemainingCorbaFailures.setTimestamp(1, ClockUtil.future().getTimestamp());
        for (int i = 0; i < SERVER_FAIL_STATUS.length; i++) {
            selectRemainingCorbaFailures.setInt(i + 1, SERVER_FAIL_STATUS[i].getAsShort());
        }
        ResultSet rs = selectRemainingCorbaFailures.executeQuery();
        return rs.next();
    }

    public static TimeInterval getRetryDelay(int numFailures) {
        if(numFailures <= 0) {
            throw new IllegalArgumentException("Failures must be greater than or equal to 1");
        }
        long millis = BASE_DELAY;
        for(int i = numFailures; i > 1; i--){ millis *= 2; }
        TimeInterval retryDelay = new TimeInterval(millis, UnitImpl.MILLISECOND);
        if(retryDelay.greaterThan(MAX_DELAY)){ return MAX_DELAY;}
        return retryDelay;
    }


    public int next() throws SQLException {
        ResultSet rs = selectNext.executeQuery();
        if(rs.next()){
            if(! rs.getTimestamp("retry_time").after(ClockUtil.now())){
                startRetry.setTimestamp(1, ClockUtil.future().getTimestamp());
                startRetry.setInt(2, rs.getInt("pairid"));
                startRetry.executeUpdate();
                return rs.getInt("pairid");
            }
        }
        return -1;
    }

    public void addRetry(int pairId, Status failureType) throws SQLException{
        if(!tableContains(pairId)){insert(pairId);}
        updateRetry(pairId, failureType.getAsShort(), getTimestamp(pairId));
    }

    private void updateRetry(int pairId, short failType, Timestamp retryTime) throws SQLException{
        updateFailure.setShort(1, failType);
        updateFailure.setTimestamp(2, retryTime);
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
        if(rs.next()){ return rs.getInt("numfailure") + 1; }
        return -1;
    }

    private void insert(int pairId) throws SQLException {
        insert.setInt(1, pairId);
        insert.setInt(2, 0);
        insert.executeUpdate();
    }

    private boolean tableContains(int pairId) throws SQLException {
        return getNumFailure(pairId) != -1;
    }

    public static void setBaseDelay(TimeInterval base){
        BASE_DELAY = (long)base.convertTo(UnitImpl.MILLISECOND).getValue();
    }

    public static void setMaxDelay(TimeInterval delay){ MAX_DELAY = delay; }

    public static Status[] SERVER_FAIL_STATUS = {
        Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.CORBA_FAILURE),
            Status.get(Stage.DATA_SUBSETTER, Standing.CORBA_FAILURE),
            Status.get(Stage.PROCESSOR, Standing.CORBA_FAILURE) };

    private static long BASE_DELAY =  84375;//this * 2^10 = 86400000, or the number of millis in a day
    private static TimeInterval MAX_DELAY = new TimeInterval(1, UnitImpl.DAY);
}

