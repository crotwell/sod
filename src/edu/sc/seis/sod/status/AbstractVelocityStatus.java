/**
 * AbstractVelocityStatus.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.process.waveformArm.LocalSeismogramTemplateGenerator;
import edu.sc.seis.sod.status.networkArm.NetworkArmContext;
import edu.sc.seis.sod.status.networkArm.NetworkArmMonitor;
import edu.sc.seis.sod.status.waveformArm.WaveformArmMonitor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractVelocityStatus  implements WaveformArmMonitor, NetworkArmMonitor {

    public AbstractVelocityStatus(String fileDir, String templateName) throws IOException {
        this.fileDir = fileDir;
        this.templateName = templateName;
        loadTemplate();
    }

    public AbstractVelocityStatus(Element config) throws SQLException, MalformedURLException, IOException {
        networkArmContext = new NetworkArmContext(CookieJar.getCommonContext());
        NodeList nl = config.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                Element element = (Element)n;
                if (element.getTagName().equals("fileDir")){
                    fileDir = SodUtil.getNestedText(element);
                } else if(n.getNodeName().equals("template")) {
                    templateName = SodUtil.getNestedText(element);
                }
            }
        }
        if (fileDir == null){
            fileDir = FileWritingTemplate.getBaseDirectoryName();
        }
        if (templateName == null) {
            throw new MalformedURLException("template config param is null");
        }
        loadTemplate();

    }

    protected void loadTemplate() throws IOException {
        URL templateURL = TemplateFileLoader.getUrl(this.getClass().getClassLoader(), templateName);
        BufferedReader read = new BufferedReader(new InputStreamReader(templateURL.openStream()));
        String line;
        while ((line = read.readLine()) != null) {
            template += line+System.getProperty("line.separator");
        }
        read.close();
    }

    public void scheduleOutput(final String filename, final Context context) {
        if ( ! runnableMap.containsKey(filename)) {
            Runnable runner = new Runnable() {
                public void run() {
                    runnableMap.remove(filename);
                    StringWriter out = new StringWriter();
                    try {
                        synchronized (LocalSeismogramTemplateGenerator.getVelocity()) {
                            // the new VeocityContext "wrapper" is to help with a possible memory leak
                            // due to velocity gathering introspection information,
                            // see http://jakarta.apache.org/velocity/developer-guide.html#Other%20Context%20Issues
                            boolean status = LocalSeismogramTemplateGenerator.getVelocity().evaluate(new VelocityContext(context),
                                                                                                     out,
                                                                                                     "waveformNetworkStatus",
                                                                                                     template);
                        }
                        FileWritingTemplate.write(fileDir+"/"+filename,
                                                  out.getBuffer().toString());
                    } catch (Exception e) {
                        GlobalExceptionHandler.handle(e);
                    }
                }
            };
            runnableMap.put(filename, runner);
            OutputScheduler.getDefault().schedule(runner);
        }
    }

    protected NetworkArmContext networkArmContext;

    protected String fileDir;

    protected String templateName;

    protected String template = "";

    protected HashMap runnableMap = new HashMap();

}

