package edu.sc.seis.sod.status;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.exceptionHandler.HTMLReporter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.eventArm.MapEventStatus;
import edu.sc.seis.sod.status.waveformArm.WaveformArmMonitor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Element;


public class IndexTemplate extends FileWritingTemplate implements WaveformArmMonitor{
    public IndexTemplate() throws IOException {
        this(FileWritingTemplate.getBaseDirectoryName());
    }

    public IndexTemplate(String dirName) throws IOException{
        super(dirName, "index.html");
        try {
            initExceptionHandler();
            Element template = TemplateFileLoader.getTemplate(getClass().getClassLoader(),
                                                              indexLoc);
            parse(template);
            write();
            FileWritingTemplate help = new FileWritingTemplate(dirName + "/help/", "eventPageHelp.html");
            help.parse(TemplateFileLoader.getTemplate(getClass().getClassLoader(),
                                                      eventPageHelp));
            help.write();
            SodUtil.copyFile(cssLoc, dirName+"/main.css");
            SodUtil.copyFile(sortLoc, dirName+"/sorttable.js");
            SodUtil.copyFile(rulLoc, dirName +"/tableRuler.js");
            SodUtil.copyFile(helpMark, dirName + "/images/helpmark.png");
            SodUtil.copyFile(up, dirName + "/images/up.gif");
            SodUtil.copyFile(down, dirName + "/images/down.gif");
            SodUtil.copyFile(none, dirName + "/images/none.gif");
            String configFileLoc = Start.getConfigFileName();
            String configFileName = new File(configFileLoc).getName();
            copiedConfigFileLoc = dirName + "/" + configFileName;
            SodUtil.copyFile(Start.getConfigFileName(), copiedConfigFileLoc);
            /* To avoid problems during rendering of XML by some of the browsers like Mac Safari*/
            convertToHTML(dirName + "/" + configFileName, dirName);
        } catch (Exception e) {
            GlobalExceptionHandler.handle("unexpected problem creating index.html page", e);
        }
    }

    public static String getCopiedConfigFileLocation() {
        if(copiedConfigFileLoc == null){ return Start.getConfigFileName(); }
        return copiedConfigFileLoc;
    }

    public static String getHtmlConfigFileName() {
        String configFileLoc  = getCopiedConfigFileLocation();
        String fileName = configFileLoc.substring(0, configFileLoc.indexOf(".xml"));
        return fileName += ".html";
    }

    public void update(EventChannelPair ecp) { write(); }


    /** Exists so IndexTemplate can be created before arms, in order for exceptions
     * in initialization to be in the status pages.*/
    public void performRegistration() {
        Start.getEventArm().add(mapEventStatus);
        Start.getWaveformArm().addStatusMonitor(this);
    }

    protected Object getTemplate(String tagName, Element el) throws ConfigurationException  {
        if(tagName.equals("eventMap")){
            mapEventStatus = new MapEventStatus(el, false);
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
    private void convertToHTML(String configFileLoc, String statusDir) throws TransformerException, FileNotFoundException, MalformedURLException, TransformerConfigurationException, IOException {
        String xslFileName = statusDir +"/xmlverbatimwrapper.xsl";
        SodUtil.copyFile(xslWrapperFileLoc,xslFileName);
        SodUtil.copyFile(supportXslFileLoc,statusDir+"/xmlverbatim.xsl");
        SodUtil.copyFile(cssFileLoc,statusDir+"/xmlverbatim.css");
        String fileName = configFileLoc.substring(0, configFileLoc.indexOf(".xml"));
        String htmlFile = statusDir + "/" + new File(fileName).getName() + ".html";
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(new StreamSource(xslFileName));
        transformer.transform(new StreamSource(configFileLoc), new StreamResult(new FileOutputStream(htmlFile)));
        new File(xslFileName).delete();
        new File(statusDir + "/xmlverbatim.xsl").delete();
    }


    private MapEventStatus mapEventStatus;

    private static String indexLoc = "jar:edu/sc/seis/sod/data/templates/index.xml";
    private static String cssLoc = "jar:edu/sc/seis/sod/data/templates/main.css";
    private static String sortLoc = "jar:edu/sc/seis/sod/data/templates/sorttable.js";
    private static String rulLoc = "jar:edu/sc/seis/sod/data/templates/tableRuler.js";
    private static String helpMark = "jar:edu/sc/seis/sod/data/templates/defaults/helpmark.png";
    private static String up = "jar:edu/sc/seis/sod/data/templates/defaults/up.gif";
    private static String down = "jar:edu/sc/seis/sod/data/templates/defaults/down.gif";
    private static String none = "jar:edu/sc/seis/sod/data/templates/defaults/none.gif";
    private static String eventPageHelp = "jar:edu/sc/seis/sod/data/templates/defaults/eventPageHelp.xml";
    private static String xslWrapperFileLoc = "jar:edu/sc/seis/sod/data/xmlverbatimwrapper.xsl";
    private static String supportXslFileLoc = "jar:edu/sc/seis/sod/data/xmlverbatim.xsl";
    private static String cssFileLoc = "jar:edu/sc/seis/sod/data/xmlverbatim.css";
    private static String copiedConfigFileLoc;
}

