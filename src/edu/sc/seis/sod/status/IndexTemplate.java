package edu.sc.seis.sod.status;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.exceptionHandler.HTMLReporter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.eventArm.MapEventStatus;
import edu.sc.seis.sod.status.waveformArm.WaveformArmMonitor;
import java.io.File;
import java.io.IOException;
import org.w3c.dom.Element;
import edu.sc.seis.sod.EventChannelPair;



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
            SodUtil.copyFile(cssLoc, dirName+"/main.css");
            SodUtil.copyFile(Start.getConfigFileName(), dirName + "/" + Start.getConfigFileName());
        } catch (Exception e) {
            GlobalExceptionHandler.handle("unexpected problem creating index.html page", e);
        }
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

    private MapEventStatus mapEventStatus;

    private static String indexLoc = "jar:edu/sc/seis/sod/data/templates/index.xml";

    private static String cssLoc = "jar:edu/sc/seis/sod/data/templates/main.css";
}

