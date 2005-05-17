package edu.sc.seis.sod.status;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.exceptionHandler.HTMLReporter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventArm;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.NetworkArm;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.WaveformArm;
import edu.sc.seis.sod.process.waveform.LocalSeismogramTemplateGenerator;
import edu.sc.seis.sod.status.eventArm.EventStatusTemplate;
import edu.sc.seis.sod.status.eventArm.MapEventStatus;
import edu.sc.seis.sod.status.networkArm.NetworkInfoTemplateGenerator;
import edu.sc.seis.sod.status.waveformArm.WaveformEventTemplateGenerator;
import edu.sc.seis.sod.status.waveformArm.WaveformMonitor;
import edu.sc.seis.sod.status.waveformArm.WaveformStationStatus;

public class IndexTemplate extends FileWritingTemplate implements
        WaveformMonitor {

    public IndexTemplate() throws IOException {
        this(FileWritingTemplate.getBaseDirectoryName());
    }

    public IndexTemplate(String dirName) throws IOException {
        super(dirName, "index.html");
        try {
            initExceptionHandler();
            Element template = TemplateFileLoader.getTemplate(getClass().getClassLoader(),
                                                              indexLoc);
            parse(template);
            write();
            FileWritingTemplate help = new FileWritingTemplate(dirName
                    + "/help/", "eventPageHelp.html");
            help.parse(TemplateFileLoader.getTemplate(getClass().getClassLoader(),
                                                      eventPageHelp));
            help.write();
            SodUtil.copyFile(cssLoc, dirName + "/main.css");
            SodUtil.copyFile(sortLoc, dirName + "/sorttable.js");
            SodUtil.copyFile(rulLoc, dirName + "/tableRuler.js");
            SodUtil.copyFile(footPosLoc, dirName + "/footerPositioner.js");
            SodUtil.copyFile(helpMark, dirName + "/images/helpmark.png");
            SodUtil.copyFile(up, dirName + "/images/up.gif");
            SodUtil.copyFile(down, dirName + "/images/down.gif");
            SodUtil.copyFile(none, dirName + "/images/none.gif");
            SodUtil.copyFile(key, dirName + "/images/mapkey.gif");
            SodUtil.copyFile(individualKey, dirName
                    + "/images/individualEventMapKey.gif");
            /*
             * To avoid problems during rendering of XML by some of the browsers
             * like Mac Safari
             */
            convertToHTML(dirName);
        } catch(Exception e) {
            GlobalExceptionHandler.handle("unexpected problem creating index.html page",
                                          e);
        }
    }

    public static void setConfigFileLoc() throws FileNotFoundException {
        String configFileLoc = Start.getConfigFileName();
        String configFileName = new File(configFileLoc).getName();
        configFile = configFileName;
        SodUtil.copyFile(Start.getConfigFileName(),
                         FileWritingTemplate.getBaseDirectoryName() + "/"
                                 + configFile);
    }

    public static String getCopiedConfigFileLocation() {
        return configFile;
    }

    public static String getHtmlConfigFileName() {
        String configFileLoc = getCopiedConfigFileLocation();
        String fileName = configFileLoc.substring(0,
                                                  configFileLoc.indexOf(".xml"));
        return fileName += ".html";
    }

    public void update(EventChannelPair ecp) {
        write();
    }

    /**
     * Exists so IndexTemplate can be created before arms, in order for
     * exceptions in initialization to be in the status pages.
     */
    public void performRegistration() throws Exception {
        if(Start.getEventArm() != null) {
            Start.getEventArm().add(mapEventStatus);
        }
        loadStatusTemplates();
        if(Start.getWaveformArm() != null) {
            Start.getWaveformArm().addStatusMonitor(this);
        }
    }

    private void loadStatusTemplates() throws Exception {
        ClassLoader cl = this.getClass().getClassLoader();
        Element statusConfig = TemplateFileLoader.getTemplate(cl,
                                                              "jar:edu/sc/seis/sod/data/statusPageConfig.xml");
        EventArm event = Start.getEventArm();
        if(event != null) {
            Element eventStatusEl = DOMHelper.extractElement(statusConfig,
                                                             "eventStatusTemplate");
            event.add(new EventStatusTemplate(eventStatusEl));
        }
        NetworkArm net = Start.getNetworkArm();
        if(net != null) {
            Element netInfoEl = DOMHelper.extractElement(statusConfig,
                                                         "networkInfoTemplateGenerator");
            net.add(new NetworkInfoTemplateGenerator(netInfoEl));
        }
        WaveformArm waveformArm = Start.getWaveformArm();
        if(waveformArm != null) {
            Element seisTempEl = DOMHelper.extractElement(statusConfig,
                                                          "localSeismogramTemplateGenerator");
            waveformArm.add(new LocalSeismogramTemplateGenerator(seisTempEl));
            Element waveformEventTempEl = DOMHelper.extractElement(statusConfig,
                                                                   "waveformEventTemplateGenerator");
            waveformArm.addStatusMonitor(new WaveformEventTemplateGenerator(waveformEventTempEl));
            Element waveformStationEl = DOMHelper.extractElement(statusConfig,
                                                                 "waveformStationStatus");
            waveformArm.addStatusMonitor(new WaveformStationStatus(waveformStationEl));
        }
    }

    protected Object getTemplate(String tagName, Element el)
            throws ConfigurationException {
        if(tagName.equals("eventMap")) {
            mapEventStatus = new MapEventStatus(el);
            return new RelativeLocationTemplate(getOutputLocation(),
                                                mapEventStatus.getLocation());
        }
        return super.getTemplate(tagName, el);
    }

    protected void initExceptionHandler() throws IOException {
        File errorDir = new File(getOutputDirectory(), "Errors");
        errorDir.mkdirs();
        GlobalExceptionHandler.add(new HTMLReporter(errorDir));
    }

    private void convertToHTML(String statusDir) throws TransformerException,
            FileNotFoundException, TransformerConfigurationException,
            IOException {
        String wrapperFile = statusDir + "/xmlverbatimwrapper.xsl";
        String mainXSL = statusDir + "/xmlverbatim.xsl";
        SodUtil.copyFile(xslWrapperFileLoc, wrapperFile);
        SodUtil.copyFile(supportXslFileLoc, mainXSL);
        SodUtil.copyFile(cssFileLoc, statusDir + "/xmlverbatim.css");
        String htmlFile = statusDir + "/" + getHtmlConfigFileName();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer t = tFactory.newTransformer(new StreamSource(wrapperFile));
        t.transform(new StreamSource(statusDir + "/" + configFile),
                    new StreamResult(new FileOutputStream(htmlFile)));
        new File(wrapperFile).delete();
        new File(mainXSL).delete();
    }

    private MapEventStatus mapEventStatus;

    private static String indexLoc = "jar:edu/sc/seis/sod/data/templates/index.xml";

    private static String cssLoc = "jar:edu/sc/seis/sod/data/templates/main.css";

    private static String sortLoc = "jar:edu/sc/seis/sod/data/templates/sorttable.js";

    private static String rulLoc = "jar:edu/sc/seis/sod/data/templates/tableRuler.js";

    private static String footPosLoc = "jar:edu/sc/seis/sod/data/templates/footerPositioner.js";

    private static String helpMark = "jar:edu/sc/seis/sod/data/templates/defaults/helpmark.png";

    private static String up = "jar:edu/sc/seis/sod/data/templates/defaults/up.gif";

    private static String down = "jar:edu/sc/seis/sod/data/templates/defaults/down.gif";

    private static String none = "jar:edu/sc/seis/sod/data/templates/defaults/none.gif";

    private static String key = "jar:edu/sc/seis/sod/data/templates/defaults/mapkey.gif";

    private static String individualKey = "jar:edu/sc/seis/sod/data/templates/defaults/individualEventMapKey.gif";

    private static String eventPageHelp = "jar:edu/sc/seis/sod/data/templates/defaults/eventPageHelp.xml";

    private static String xslWrapperFileLoc = "jar:edu/sc/seis/sod/data/xmlverbatimwrapper.xsl";

    private static String supportXslFileLoc = "jar:edu/sc/seis/sod/data/xmlverbatim.xsl";

    private static String cssFileLoc = "jar:edu/sc/seis/sod/data/xmlverbatim.css";

    private static String configFile;
}