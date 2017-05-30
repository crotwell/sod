/**
 * WaveformStationStatus.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.waveformArm;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Site;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.eventpair.EventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventNetworkPair;
import edu.sc.seis.sod.hibernate.eventpair.EventStationPair;
import edu.sc.seis.sod.hibernate.eventpair.EventVectorPair;
import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.model.station.StationIdUtil;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.model.status.Stage;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.model.status.Status;
import edu.sc.seis.sod.status.AbstractVelocityStatus;
import edu.sc.seis.sod.status.networkArm.NetworkMonitor;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;



public class WaveformStationStatus extends AbstractVelocityStatus implements WaveformMonitor, NetworkMonitor {

    public WaveformStationStatus(String fileDir, String templateName) throws IOException, SQLException {
        super( fileDir, templateName);
        if(Start.getNetworkArm() != null) Start.getNetworkArm().add(this);
    }

    public WaveformStationStatus(Element config) throws IOException, SQLException {
        super(config);
        if(Start.getNetworkArm() != null) Start.getNetworkArm().add(this);
    }

    public int getNumDirDeep() { return 2; }

    public void update(EventNetworkPair ecp) {
    }

    public void update(EventStationPair ecp) {
    }

    public void update(EventChannelPair ecp) {
        Status status = ecp.getStatus();
        if (status.getStage().equals(Stage.PROCESSOR) && status.getStanding().equals(Standing.SUCCESS)) {
            StationImpl station = (StationImpl)ecp.getChannel().getSite().getStation();
            doUpdate(station);
        }
    }

    public void update(EventVectorPair ecp) {
        Status status = ecp.getStatus();
        if (status.getStage().equals(Stage.PROCESSOR) && status.getStanding().equals(Standing.SUCCESS)) {
            StationImpl station = (StationImpl)ecp.getChannelGroup().getChannels()[0].getSite().getStation();
            doUpdate(station);
        }
    }
    
    protected void doUpdate(StationImpl station) {
            try {
                int stationDbid = ((StationImpl)station).getDbid();
                VelocityContext context = new VelocityContext(new StationWaveformContext((StationImpl)station));
                context.put("station", station);
                context.put("networkid", station.get_id().network_id);
                context.put("network", station.getNetworkAttr());
                scheduleOutput("waveformStations/"+NetworkIdUtil.toStringNoDates(station.get_id().network_id)+"/"+StationIdUtil.toStringNoDates(station.get_id())+".html",
                               context);
            } catch (SQLException e) {
                GlobalExceptionHandler.handle(e);
            }
        
    }

    public void setArmStatus(String status) throws Exception {
    }

    public void change(Station station, Status s) {
    }

    public void change(Channel channel, Status s) {
    }

    public void change(NetworkAttrImpl net, Status status){
    }

    public void change(Site site, Status s) {
    }

}


