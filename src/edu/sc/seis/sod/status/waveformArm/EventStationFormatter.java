/**
 * EventStationFormatter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.network.JDBCStation;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.StationFormatter;
import edu.sc.seis.sod.status.StationTemplate;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import org.w3c.dom.Element;

public class EventStationFormatter extends StationFormatter{
    public EventStationFormatter(Element el) throws ConfigurationException{
        super(el);
    }

    public Object getTemplate(String name, Element el){
        if(name.equals("numSuccess")){ return new SuccessfulQuery(); }
        else if(name.equals("numFailed")){ return new FailedQuery(); }
        else if(name.equals("numRetry")){ return new RetryQuery(); }
        else if(name.equals("distance")){ return new Distance(); }
        else if(name.equals("baz")){ return new BackAz(); }
        return super.getTemplate(name, el);
    }

    public void setEvent(EventAccessOperations ev){ this.ev = ev; }

    private class Distance implements StationTemplate{
        public String getResult(Station station) {
            DistAz dAz = new DistAz(station, ev);
            return df.format(dAz.getDelta());
        }

        private DecimalFormat df = new DecimalFormat("0.00");
    }

    private class BackAz implements StationTemplate{
        public String getResult(Station station) {
            DistAz dAz = new DistAz(station, ev);
            return df.format(dAz.getBaz());
        }

        private DecimalFormat df = new DecimalFormat("0.00");
    }

    private class SuccessfulQuery implements StationTemplate{
        public String getResult(Station station) {
            return "" + queryStatus(station, success);
        }
    }

    private class FailedQuery implements StationTemplate{
        public String getResult(Station station) {
            return "" + queryStatus(station, failed);
        }
    }

    private class RetryQuery implements StationTemplate{
        public String getResult(Station station) {
            return "" + queryStatus(station, retry);
        }
    }

    private int queryStatus(Station s, PreparedStatement stmt){
        int id;
        try {
            synchronized(stationTable){ id = stationTable.getDBId(s.get_id()); }
        } catch (Exception e) {
            GlobalExceptionHandler.handle("Trouble getting dbid from the station table for " + StationIdUtil.toString(s.get_id())
                                              , e);
            return -1;
        }
        try {
            synchronized(evStatus){ return evStatus.getNum(stmt, ev, id); }
        } catch (Exception e) {
            GlobalExceptionHandler.handle("Trouble getting channels out of the db", e);
        }
        return -1;
    }

    private EventAccessOperations ev;

    private static JDBCEventChannelStatus evStatus;
    private static JDBCStation stationTable;
    private static PreparedStatement retry, failed, success;

    static{
        try {
            stationTable = new JDBCStation();
            evStatus = new JDBCEventChannelStatus();
            String baseStatement = "SELECT COUNT(*) FROM eventchannelstatus, channel, site WHERE " +
                "eventid = ? AND " +
                "eventchannelstatus.channelid = channel.chan_id AND " +
                "channel.site_id = site.site_id AND site.sta_id = ?";
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


