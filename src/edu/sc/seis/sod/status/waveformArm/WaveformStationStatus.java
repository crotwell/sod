/**
 * WaveformStationStatus.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.waveformArm;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.AbstractVelocityStatus;
import edu.sc.seis.sod.status.networkArm.NetworkArmMonitor;
import edu.sc.seis.sod.status.waveformArm.StationWaveformContext;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;



public class WaveformStationStatus extends AbstractVelocityStatus implements WaveformArmMonitor, NetworkArmMonitor {

    public WaveformStationStatus(Element config) throws IOException, SQLException {
        super(config);
        if(Start.getNetworkArm() != null) Start.getNetworkArm().add(this);
    }

    public void update(EventChannelPair ecp) {

    }

    public void setArmStatus(String status) throws Exception {
    }

    public void change(Station station, Status s) {
        try {
            int stationDbid = Start.getNetworkArm().getStationDbId(station);
            VelocityContext context = new VelocityContext(new StationWaveformContext(stationDbid));
            context.put("station", station);
            scheduleOutput(StationIdUtil.toStringNoDates(station.get_id())+".html",
                           context);
        } catch (SQLException e) {
            GlobalExceptionHandler.handle(e);
        }
    }

    public void change(Channel channel, Status s) {
    }

    public void change(NetworkAccess net, Status status){
    }

    public void change(Site site, Status s) {
    }

}

