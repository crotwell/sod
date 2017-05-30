/**
 * WaveformNetworkStatus.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.waveformArm;
import java.io.IOException;
import java.net.MalformedURLException;
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
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.model.status.Status;
import edu.sc.seis.sod.status.AbstractVelocityStatus;
import edu.sc.seis.sod.status.networkArm.NetworkMonitor;
import edu.sc.seis.sod.status.networkArm.VelocityStationGetter;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;



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
            NetworkAttrImpl net = Start.getNetworkArm().getNetwork(station.getNetworkAttr().get_id());
            context.put("network", net);
            context.put("stations", new VelocityStationGetter(station.getNetworkAttr().get_id()));
            String id = NetworkIdUtil.toStringNoDates(station.getNetworkAttr().get_id());
            scheduleOutput("waveformStations/"+ id +".html", context);
        } catch (Throwable e) {
            GlobalExceptionHandler.handle("Can't set status("+s+") for "+StationIdUtil.toString(station.get_id()), e);
        }
    }

    public void change(Channel channel, Status s) {}

    public void change(NetworkAttrImpl net, Status s) {
        if (s.getStanding().equals(Standing.SUCCESS)) {
            // update the index list
            VelocityContext context = new VelocityContext();
            context.put("network", net);
            scheduleOutput("waveformStations/waveformNetworks.html",
                           context,
                           networkListTemplate);
        }
    }

    public void change(Site site, Status s) {}

    protected String networkListTemplate;
}



