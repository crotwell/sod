package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.exceptionHandler.WrappedException;
import edu.sc.seis.sod.database.HSqlDbQueue;
import edu.sc.seis.sod.database.Queue;
import edu.sc.seis.sod.database.WaveformDbQueue;
import edu.sc.seis.sod.database.WaveformQueue;
import edu.sc.seis.sod.validator.Validator;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.kohsuke.validatelet.jarv.JARVVerifierImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Start.java
 *
 *
 * Created: Thu Dec 13 16:06:00 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class Start implements SodExceptionListener {
    /**
     * Creates a new <code>Start</code> instance set to use the XML config file
     * the input stream points to as its configuration
     *
     * @param configFile an <code>InputStream</code> value pointing to a SOD xml
     * config file
     */
    public Start (InputSource configFile){
        try {
            document = createDoc(configFile);
        } catch (Exception e) {
            System.out.println("Trouble creating xml document");
            e.printStackTrace();
        }
        try {
            if(!validate(document)){
                System.out.println("Invalid config file!");
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("Problem configuring schema validator");
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    public boolean validate(Document doc) throws SAXException, VerifierConfigurationException{
        return true;//following lines removed pending fix to bali
        //Verifier v = new JARVVerifierImpl(Validator.schema.createValidatelet());
        //if(v.verify(doc)) return true;
        //return false;
    }
    
    public static Document createDoc(InputSource source)
        throws SAXException, IOException, ParserConfigurationException{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new SimpleErrorHandler());
        Document doc =  builder.parse(source);
        return doc;
    }
    
    public void createArms() throws Exception {
        Element docElement = document.getDocumentElement();
        logger.info("start "+docElement.getTagName());
        NetworkArm networkArm = null;
        NodeList children = docElement.getChildNodes();
        Node node;
        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if (node instanceof Element) {
                Element subElement = (Element)node;
                if (subElement.getTagName().equals("description")) {
                    logger.info(subElement.getTagName());
                } else if (subElement.getTagName().equals("eventArm")) {
                    logger.info(subElement.getTagName());
                    EventArm eventArm = new EventArm(subElement, this, props);
                    eventArmThread = new Thread(eventArm, "eventArm thread");
                    eventArmThread.start();
                } else if (subElement.getTagName().equals("networkArm")) {
                    logger.info(subElement.getTagName());
                    networkArm = new NetworkArm(subElement);
                } else if (subElement.getTagName().equals("waveFormArm")) {
                    logger.info(subElement.getTagName());
                    int threadPoolSize =
                        Integer.parseInt(props.getProperty("edu.sc.seis.sod.waveformarm.threads",
                                                           "5"));
                    WaveFormArm waveformArm = new WaveFormArm(subElement,
                                                              networkArm,
                                                              this,
                                                              threadPoolSize);
                    waveFormArmThread = new Thread(waveformArm, "waveFormArm Thread");
                    waveFormArmThread.start();
                    
                }  else {
                    logger.debug("process "+subElement.getTagName());
                }
            }
        }
    }
    
    public static Properties getProperties() {
        return props;
    }
    
    public Document getDocument(){ return document; }
    
    public static WaveformQueue getWaveformQueue(){ return waveformQueue; }
    
    public static Queue getEventQueue(){ return eventQueue; }
    
    public static void main (String[] args) {
        try {
            Start.props  = System.getProperties();
            // get some defaults
            loadProps((Start.class).getClassLoader().getResourceAsStream(DEFAULT_PROPS));
            String confFilename = null;
            
            for (int i=0; i<args.length-1; i++) {
                if (args[i].equals("-props")) {
                    // override with values in local directory,
                    // but still load defaults with original name
                    loadProps(new FileInputStream(args[i+1]));
                    System.out.println("loaded file props");
                } if(args[i].equals("-conf") || args[i].equals("-f")) {
                    confFilename = args[i+1];
                }
            }
            if (confFilename == null) {
                System.err.println("No configuration file given, quiting....");
                return;
            }
            PropertyConfigurator.configure(props);
            InputStream in = null;
            if(confFilename.startsWith("http:") || confFilename.startsWith("ftp:")){
                in = new URL(confFilename).openConnection().getInputStream();
            }else{
                in = new FileInputStream(confFilename);
            }
            if(in == null){
                logger.fatal("Unable to load configuration file "+confFilename+", quiting...");
                return;
            }
            Start start = new Start(new InputSource(new BufferedInputStream(in)));
            
            //now override the properties with the properties specified
            // in the configuration file.
            Element docElement = start.getDocument().getDocumentElement();
            Element propertiesElement = SodUtil.getElement(docElement, "properties");
            if(propertiesElement != null) {
                //load the properties fromt the configurationfile.
                SodUtil.loadProperties(propertiesElement, props);
                Start.props = props;
            } else {
                logger.debug("No properties specified in the configuration file");
            }
            
            //here the orb must be initialized ..
            //configure commonAccess
            CommonAccess.getCommonAccess().initORB(args, props);
            
            checkRestartOptions();
            
            //configure the eventQueue and waveformQueue.
            eventQueue = new HSqlDbQueue(props);
            eventQueue.clean();
            waveformQueue = new WaveformDbQueue(props);
            waveformQueue.clean();
            
            logger.info("Start start()");
            start.createArms();
            eventArmThread.join();
            waveFormArmThread.join();
            
            eventQueue.closeDatabase();
            logger.debug("Did not track the Thread bug Yet. so using System.exit()");
            System.exit(1);
        } catch(Exception e) {
            e.printStackTrace();
            if (e instanceof WrappedException) {
                logger.error("Problem, wrapped is ", ((WrappedException)e).getCausalException());
            } // end of if (e instanceof WrappedException)
            logger.error("Problem... ", e);
        }
        logger.info("Done.");
    } // end of main ()
    
    private static void loadProps(InputStream propStream){
        try {
            props.load(propStream);
            propStream.close();
        } catch (Exception f) {
            System.err.println("Problem loading props!");
            f.printStackTrace();
            System.exit(0);
        }
    }
    
    public void sodExceptionHandler(SodException sodException) {
        logger.fatal("Caught Exception in start because of the Listener",
                     sodException.getThrowable());
    }
    
    private static  void checkRestartOptions() {
        Start.REMOVE_DATABASE = isRemoveDatabase();
        Start.QUIT_TIME = getQuitTime();
        Start.RE_OPEN_EVENTS = isReOpenEvents();
        Start.GET_NEW_EVENTS = isGetNewEvents();
        Start.REFRESH_INTERVAL = getRefreshInterval();
    }
    
    private static boolean isRemoveDatabase() {
        String str = props.getProperty("edu.sc.seis.sod.database.remove");
        if(str != null) {
            if(str.equalsIgnoreCase("true")) { return true;}
        }
        return false;
    }
    
    private static int getRefreshInterval() {
        String str = props.getProperty("edu.sc.seis.sod.database.eventRefreshInterval");
        if(str != null) {
            try {
                return Integer.parseInt(str);
            } catch(NumberFormatException nfe) {
                return 30;
            }
        }
        return 30;
    }
    
    private static int getQuitTime() {
        String str = props.getProperty("edu.sc.seis.sod.database.quitTime");
        if(str != null) {
            try {
                return Integer.parseInt(str);
            } catch(NumberFormatException nfe) {
                return 30;
            }
        }
        return 30;
    }
    
    private static boolean isReOpenEvents() {
        String str = props.getProperty("edu.sc.seis.sod.database.reopenEvents");
        if(str != null) {
            if(str.equalsIgnoreCase("true")) {return true; }
        }
        return false;
    }
    
    private static boolean isGetNewEvents() {
        String str = props.getProperty("edu.sc.seis.sod.database.getNewEvents");
        if(str != null) {
            if(str.equalsIgnoreCase("true")) { return true;}
        }
        return false;
    }
    
    public static final String
        DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
    
    public static boolean REMOVE_DATABASE = false;
    
    //later quitetime must be changed relatively
    public static int QUIT_TIME = 30; //quit time in terms of number of days;
    
    public static boolean RE_OPEN_EVENTS = false;
    
    public static boolean GET_NEW_EVENTS = false;
    
    public static int REFRESH_INTERVAL = 30;
    
    private static Properties props = null;
    
    private Document document;
    
    private static Logger logger = Logger.getLogger(Start.class);
    
    private static WaveformQueue waveformQueue;
    
    private static Queue eventQueue;
    
    private static Thread waveFormArmThread;
    
    private static Thread eventArmThread;
    
    private static String DEFAULT_PROPS = "edu/sc/seis/sod/sod.prop";
}// Start
