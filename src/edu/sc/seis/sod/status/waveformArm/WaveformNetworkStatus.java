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
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.AbstractVelocityStatus;
import edu.sc.seis.sod.status.networkArm.NetworkArmMonitor;
import edu.sc.seis.sod.status.networkArm.VelocityStationGetter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;



public class WaveformNetworkStatus extends AbstractVelocityStatus implements WaveformArmMonitor, NetworkArmMonitor {

    public WaveformNetworkStatus(Element config) throws SQLException, MalformedURLException, IOException {
        super(config);
        Element element = SodUtil.getElement(config, "networkListTemplate");
        if (element != null){
            networkListTemplateName = SodUtil.getNestedText(element);
        }

        networkListTemplate = loadTemplate(networkListTemplateName);
        if(Start.getNetworkArm() != null) Start.getNetworkArm().add(this);
    }

    public void update(EventChannelPair ecp) {
        // do nothing, just want to be a WaveformArmMonitor for loading
    }

    public void setArmStatus(String status) throws Exception {

    }

    public void change(Station station, Status s) {
    }

    public void change(Channel channel, Status s) {
    }

    public void change(NetworkAccess networkAccess, Status s) {
        if (s.getStanding().equals(Standing.SUCCESS)) {
            // update the index list
            scheduleOutput("waveformNetworks.html", networkArmContext, networkListTemplate);

            VelocityContext context = new VelocityContext(networkArmContext);
            context.put("network", networkAccess);
            context.put("stations", new VelocityStationGetter(networkAccess));
            scheduleOutput("waveformStations/"+NetworkIdUtil.toStringNoDates(networkAccess.get_attributes().get_id())+".html",
                           context);
        }
    }

    public void change(Site site, Status s) {
    }

    protected String networkListTemplateName;

    protected String networkListTemplate;
}



