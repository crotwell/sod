/**
 * EventStationFormatter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.database.network.JDBCStation;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.StationDbObject;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.StationFormatter;
import edu.sc.seis.sod.status.StationTemplate;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.w3c.dom.Element;

public class EventStationFormatter extends StationFormatter{
    public EventStationFormatter(Element el, EventAccessOperations ev) throws ConfigurationException{
        super(el);
        this.ev = ev;
    }

    public Object getTemplate(String name, Element el){
        if(name.equals("numSuccess")){
            return new SuccessfulQuery();
        }else if(name.equals("numFailed")){
            return new FailedQuery();
        }else if(name.equals("numRetry")){
            return new RetryQuery();
        }
        return super.getTemplate(name, el);
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

    private static JDBCEventChannelStatus evStatus;
    private static JDBCStation stationTable;
    private static PreparedStatement retry, failed, success;

    private static String getStatusRequest(Status[] statii){
        String request = "( status = " + statii[0].getAsShort();
        for (int i = 1; i < statii.length; i++) {
            request += " OR status = " + statii[i].getAsShort();
        }
        request += ")";
        return request;
    }

    static{
        try {
            stationTable = new JDBCStation();
            evStatus = new JDBCEventChannelStatus();
            String baseStatement = "SELECT COUNT(*) FROM eventchannelstatus, channel, site WHERE " +
                "eventid = ? AND " +
                "eventchannelstatus.channelid = channel.chan_id AND " +
                "channel.site_id = site.site_id AND site.sta_id = ?";
            success = evStatus.prepareStatement(baseStatement + " AND status = " + Status.get(Stage.PROCESSOR,
                                                                                              Standing.SUCCESS).getAsShort());
            Status[] failedStatus = new Status[]{Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.REJECT),
                    Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.SYSTEM_FAILURE),
                    Status.get(Stage.EVENT_CHANNEL_SUBSETTER, Standing.REJECT),
                    Status.get(Stage.EVENT_CHANNEL_SUBSETTER, Standing.SYSTEM_FAILURE),
                    Status.get(Stage.REQUEST_SUBSETTER, Standing.REJECT),
                    Status.get(Stage.REQUEST_SUBSETTER, Standing.SYSTEM_FAILURE),
                    Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.SYSTEM_FAILURE),
                    Status.get(Stage.DATA_SUBSETTER, Standing.SYSTEM_FAILURE),
                    Status.get(Stage.PROCESSOR, Standing.SYSTEM_FAILURE)};
            failed = evStatus.prepareStatement(baseStatement + " AND " + getStatusRequest(failedStatus));
            Status[] retryStatus = new Status[]{
                Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.REJECT),
                    Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.CORBA_FAILURE),
                    Status.get(Stage.DATA_SUBSETTER, Standing.CORBA_FAILURE),
                    Status.get(Stage.PROCESSOR, Standing.CORBA_FAILURE)};
            retry = evStatus.prepareStatement(baseStatement + " AND " + getStatusRequest(retryStatus));
        } catch (SQLException e) {
            GlobalExceptionHandler.handle(e);
        }
    }

    private EventAccessOperations ev;
}

