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
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.StationDbObject;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.StationFormatter;
import edu.sc.seis.sod.status.StationTemplate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
            return "" + queryStatus(station, Status.get(Stage.PROCESSOR,
                                                        Standing.SUCCESS));
        }
    }

    private class FailedQuery implements StationTemplate{
        public String getResult(Station station) {
            List statii = new ArrayList();
            statii.add(Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.REJECT));
            statii.add(Status.get(Stage.EVENT_STATION_SUBSETTER, Standing.SYSTEM_FAILURE));
            statii.add(Status.get(Stage.EVENT_CHANNEL_SUBSETTER, Standing.REJECT));
            statii.add(Status.get(Stage.EVENT_CHANNEL_SUBSETTER, Standing.SYSTEM_FAILURE));
            statii.add(Status.get(Stage.REQUEST_SUBSETTER, Standing.REJECT));
            statii.add(Status.get(Stage.REQUEST_SUBSETTER, Standing.SYSTEM_FAILURE));
            statii.add(Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.SYSTEM_FAILURE));
            statii.add(Status.get(Stage.DATA_SUBSETTER, Standing.SYSTEM_FAILURE));
            statii.add(Status.get(Stage.PROCESSOR, Standing.SYSTEM_FAILURE));
            Iterator it = statii.iterator();
            int numOfStatus = 0;
            while(it.hasNext()){
                Status cur = (Status)it.next();
                numOfStatus +=  queryStatus(station, cur);
            }
            return "" + numOfStatus;
        }
    }

    private class RetryQuery implements StationTemplate{
        public String getResult(Station station) {
            List statii = new ArrayList();
            statii.add(Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.REJECT));
            statii.add(Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.CORBA_FAILURE));
            statii.add(Status.get(Stage.DATA_SUBSETTER, Standing.CORBA_FAILURE));
            statii.add(Status.get(Stage.PROCESSOR, Standing.CORBA_FAILURE));
            Iterator it = statii.iterator();
            int numOfStatus = 0;
            while(it.hasNext()){
                Status cur = (Status)it.next();
                numOfStatus +=  queryStatus(station, cur);
            }
            return "" + numOfStatus;
        }
    }



    private int queryStatus(Station s, Status status){
        int id = -1;
        try {
            NetworkDbObject[] netDbs = Start.getNetworkArm().getSuccessfulNetworks();
            for (int i = 0; i < netDbs.length; i++) {
                if(NetworkIdUtil.areEqual(netDbs[i].getNetworkAccess().get_attributes().get_id(),
                                          s.get_id().network_id)){
                    StationDbObject[] staDbs = netDbs[i].stationDbObjects;
                    for (int j = 0; j < staDbs.length; j++) {
                        if(StationIdUtil.areEqual(staDbs[j].getStation().get_id(),
                                                  s.get_id())){
                            id = staDbs[j].getDbId();
                        }
                    }
                }
            }
        } catch (Exception e) {
            GlobalExceptionHandler.handle("Trouble getting successful networks from the network arm", e);
            return -1;
        }
        if(id == -1){
            throw new RuntimeException("The network arm knows nothing about station " + StationIdUtil.toString(s.get_id()));
        }
        try {
            synchronized(evStatus){ return evStatus.getNum(ev, status, id); }
        } catch (Exception e) {
            GlobalExceptionHandler.handle("Trouble getting channels out of the db", e);
        }
        return -1;
    }

    private static JDBCEventChannelStatus evStatus;
    static{
        try {
            evStatus = new JDBCEventChannelStatus();
        } catch (SQLException e) {
            GlobalExceptionHandler.handle(e);
        }
    }

    private EventAccessOperations ev;
}

