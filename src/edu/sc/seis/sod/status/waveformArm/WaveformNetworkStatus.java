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
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.process.waveformArm.LocalSeismogramTemplateGenerator;
import edu.sc.seis.sod.status.FileWritingTemplate;
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



public class WaveformNetworkStatus implements WaveformArmMonitor, NetworkArmMonitor {

    public WaveformNetworkStatus(Element config) throws SQLException, MalformedURLException, IOException {
        networkArmContext = new NetworkArmContext();
        NodeList nl = config.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                Element element = (Element)n;
                if (element.getTagName().equals("fileDir")){
                    fileDir = SodUtil.getNestedText(element);
                } else if(n.getNodeName().equals("networkTemplate")) {
                    networkTemplate = SodUtil.getNestedText(element);
                }
            }
        }
        if (fileDir == null){
            fileDir = FileWritingTemplate.getBaseDirectoryName();
        }
        String template = "";
        URL templateURL = TemplateFileLoader.getUrl(this.getClass().getClassLoader(), networkTemplate);
        BufferedReader read = new BufferedReader(new InputStreamReader(templateURL.openStream()));
        String line;
        while ((line = read.readLine()) != null) {
            template += line;
        }

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
        StringWriter out = new StringWriter();

        try {
        // the new VeocityContext "wrapper" is to help with a possible memory leak
        // due to velocity gathering introspection information,
        // see http://jakarta.apache.org/velocity/developer-guide.html#Other%20Context%20Issues
        boolean status = LocalSeismogramTemplateGenerator.getVelocity().evaluate(new VelocityContext(networkArmContext),
                                                                                 out,
                                                                                 "localSeismogramTemplate",
                                                                                 template);
        } catch (Exception e) {
            GlobalExceptionHandler.handle(e);
        }
    }

    public void change(Site site, Status s) {
    }

    private NetworkArmContext networkArmContext;

    private String fileDir;

    private String networkTemplate;

    private String template = "";

}

