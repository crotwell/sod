/**
 * WaveformNetworkStatus.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.waveformArm;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventNetworkPair;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.EventVectorPair;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.AbstractVelocityStatus;
import edu.sc.seis.sod.status.networkArm.NetworkMonitor;
import edu.sc.seis.sod.status.networkArm.VelocityStationGetter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;



public class WaveformNetworkStatus extends AbstractVelocityStatus implements WaveformMonitor, NetworkMonitor {
    public WaveformNetworkStatus(Element config) throws SQLException, MalformedURLException, IOException {
        super(config);
        String networkListLoc = getNestedTextForElement("networkListTemplate",
                                                        config);
        networkListTemplate = loadTemplate(networkListLoc);
        if(Start.getNetworkArm() != null) {Start.getNetworkArm().add(this);}
    }

    public int getNumDirDeep() { return 1; }

    public void update(EventNetworkPair ecp) {
    }

    public void update(EventStationPair ecp) {
    }

    public void update(EventChannelPair ecp) {
        // update the page for num successes change
        change(ecp.getChannel().getSite().getStation(), ecp.getStatus());
    }

    public void update(EventVectorPair ecp) {
        // update the page for num successes change
        change(ecp.getChannelGroup().getChannels()[0].getSite().getStation(), ecp.getStatus());
    }

    public void setArmStatus(String status) throws Exception {}

    public void change(Station station, Status s) {
        try {
            VelocityContext context = new VelocityContext();
            NetworkAccess networkAccess = Start.getNetworkArm().getNetwork(station.getNetworkAttr().get_id());
            context.put("network", networkAccess);
            context.put("stations", new VelocityStationGetter(station.getNetworkAttr().get_id()));
            String id = NetworkIdUtil.toStringNoDates(station.getNetworkAttr().get_id());
            scheduleOutput("waveformStations/"+ id +".html", context);
        } catch (Throwable e) {
            GlobalExceptionHandler.handle("Can't set status("+s+") for "+StationIdUtil.toString(station.get_id()), e);
        }
    }

    public void change(Channel channel, Status s) {}

    public void change(NetworkAccess networkAccess, Status s) {
        if (s.getStanding().equals(Standing.SUCCESS)) {
            // update the index list
            VelocityContext context = new VelocityContext();
            context.put("network", networkAccess);
            scheduleOutput("waveformStations/waveformNetworks.html",
                           context,
                           networkListTemplate);
        }
    }

    public void change(Site site, Status s) {}

    protected String networkListTemplate;
}



