package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.WrappedException;
import edu.sc.seis.sod.database.HSqlDbQueue;
import edu.sc.seis.sod.database.Queue;
import edu.sc.seis.sod.database.WaveformDbQueue;
import edu.sc.seis.sod.database.WaveformQueue;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

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
    public Start (InputStream configFile, URL schemaURL) throws ParserConfigurationException,
        SAXException,
        IOException{
        //initialize the parser
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        docBuilder.setErrorHandler(new SimpleErrorHandler());
        logger.info("Schema loc is: "+schemaURL.toString());
        document =  docBuilder.parse(configFile, schemaURL.toString());
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
                    new Thread(eventArm, "eventArm thread").start();
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
            Properties props = System.getProperties();
            //            props.put("org.omg.CORBA.ORBClass", "com.ooc.CORBA.ORB");
            //            props.put("org.omg.CORBA.ORBSingletonClass",
            //                      "com.ooc.CORBA.ORBSingleton");
            // get some defaults
            InputStream propStream = (Start.class).getClassLoader().getResourceAsStream("edu/sc/seis/sod/sod.prop");
            String confFilename = null;
            
            for (int i=0; i<args.length-1; i++) {
                System.out.println(args[i]);
                if (args[i].equals("-props")) {
                    // override with values in local directory,
                    // but still load defaults with original name
                    propStream = new FileInputStream(args[i+1]);
                } if(args[i].equals("-conf") || args[i].equals("-f")) {
                    confFilename = args[i+1];
                }
            }
            if (confFilename == null) {
                System.err.println("No configuration file given, quiting....");
                return;
            }
            
            try {
                props.load(propStream);
                propStream.close();
            } catch (Exception f) {
                System.err.println("Problem loading props!");
                f.printStackTrace();
                System.exit(0);
            }
            Start.props = props;
            PropertyConfigurator.configure(props);
            InputStream in;
            
            URL schemaURL =
                (Start.class).getClassLoader().getResource("edu/sc/seis/sod/data/sod.xsd");
            if (schemaURL == null) {
                logger.fatal("Can't find the sod.xsd xschema file, this may indicate a corrupt installation of sod! Cowardly quitting at this point.");
                return;
            }
            
            boolean b = Validator.validate(confFilename);
            if (b) {
                System.err.println("The configuration file "+confFilename+" did not validate against the xschema for sod.");
                logger.fatal("The configuration file "+confFilename+" did not validate against the xschema for sod.");
                System.err.println("Please see the log file for more information.");
                System.exit(0);
            } else {
                System.out.println("Configuration file "+confFilename+" is valid.");
            }
            
            if (confFilename.startsWith("http:") || confFilename.startsWith("ftp:")) {
                URL url = new java.net.URL(confFilename);
                URLConnection conn = url.openConnection();
                in = new BufferedInputStream(conn.getInputStream());
            } else {
                in = new BufferedInputStream(new FileInputStream(confFilename));
            } // end of else
            
            if (in == null) {
                logger.fatal("Unable to load configuration file "+confFilename+", quiting...");
                return;
            } // end of if (in == null)
            
            Start start = new Start(in, schemaURL);
            
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
    
    /** Default parser name. */
    private static final String
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
}// Start

