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
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.process.waveformArm.LocalSeismogramTemplateGenerator;
import edu.sc.seis.sod.status.AbstractVelocityStatus;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.OutputScheduler;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.status.networkArm.NetworkArmContext;
import edu.sc.seis.sod.status.networkArm.NetworkArmMonitor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class WaveformNetworkStatus extends AbstractVelocityStatus implements WaveformArmMonitor, NetworkArmMonitor {

    public WaveformNetworkStatus(Element config) throws SQLException, MalformedURLException, IOException {
        super(config);
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
        scheduleOutput("waveformNetworks.html", networkArmContext);
    }

    public void change(Site site, Status s) {
    }

}


