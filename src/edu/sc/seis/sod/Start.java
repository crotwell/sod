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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Start implements SodExceptionListener {
    /**
     * Creates a new <code>Start</code> instance set to use the XML config file
     * the input stream points to as its configuration
     *
     * @param configFile an <code>InputStream</code> value pointing to a SOD xml
     * config file
     */
    public Start (String confFilename, String[] args) throws Exception{
        try {
            document = createDoc(createInputSource(confFilename));
        } catch (Exception e) {
            CommonAccess.handleException(e, "Trouble creating xml document");
        }
        try {
            if(!Validator.validate(createInputSource(confFilename))){
                logger.info("Invalid config file!");
                System.err.println();
                System.err.println("******************************************************************");
                System.err.println();
                System.err.println("Invalid config file! Abandon all hope ye who continue running SOD!");
                System.err.println();
                System.err.println("******************************************************************");
                System.err.println();
            }else{
                logger.info("Valid config file");
            }
        } catch (Exception e) {
            CommonAccess.handleException(e, "Problem configuring schema validator");
            System.exit(0);
        }
        initDocument(args);
    }

    public Start(Document document) throws Exception {
        this(document, new String[0]);
    }

    public Start(Document document, String[] args) throws Exception {
        this.document = document;
        initDocument(args);
    }

    protected void initDocument(String[] args) throws Exception {
        // get some defaults
        loadProps((Start.class).getClassLoader().getResourceAsStream(DEFAULT_PROPS));

        for (int i=0; i<args.length-1; i++) {
            if (args[i].equals("-props")) {
                // override with values in local directory,
                // but still load defaults with original name
                loadProps(new FileInputStream(args[i+1]));
                System.out.println("loaded file props from "+args[i+1]+"  log4j.rootCategory="+props.getProperty("log4j.rootCategory"));
            }
        }

        PropertyConfigurator.configure(props);
        logger.info("logging configured");

        //now override the properties with the properties specified
        // in the configuration file.
        Element docElement = getDocument().getDocumentElement();
        Element propertiesElement = SodUtil.getElement(docElement, "properties");
        if(propertiesElement != null) {
            //load the properties fromt the configurationfile.
            SodUtil.loadProperties(propertiesElement, props);
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
    }

    private InputSource createInputSource(String loc) throws IOException{
        InputStream in = null;
        if(loc.startsWith("http:") || loc.startsWith("ftp:")){
            in = new URL(loc).openConnection().getInputStream();
        }else{
            in = new FileInputStream(loc);
        }
        if(in == null){
            throw new IOException("Unable to load configuration file "+loc);
        }
        return new InputSource(new BufferedInputStream(in));
    }

    public static WaveFormArm getWaveformArm() {
        return waveform;
    }

    public static EventArm getEventArm() {
        return event;
    }

    public static NetworkArm getNetworkArm() {
        return network;
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

    public void start() throws Exception {
        Element docElement = document.getDocumentElement();
        logger.info("start "+docElement.getTagName());
        NodeList children = docElement.getChildNodes();
        Node node;
        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if (node instanceof Element) {
                Element el = (Element)node;
                if (el.getTagName().equals("description")) {
                    logger.info(el.getTagName());
                } else if (el.getTagName().equals("eventArm")) {
                    logger.info(el.getTagName());
                    event = new EventArm(el, this, props);
                    eventArmThread = new Thread(event, "eventArm thread");
                    eventArmThread.start();
                } else if (el.getTagName().equals("networkArm")) {
                    logger.info(el.getTagName());
                    network = new NetworkArm(el);
                } else if (el.getTagName().equals("waveFormArm")) {
                    logger.info(el.getTagName());
                    int poolSize =
                        Integer.parseInt(props.getProperty("edu.sc.seis.sod.waveformarm.threads",
                                                           "5"));
                    waveform = new WaveFormArm(el, network, this, poolSize);
                    waveFormArmThread = new Thread(waveform, "waveFormArm Thread");
                    waveFormArmThread.start();

                }  else {
                    logger.debug("process "+el.getTagName());
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

            String confFilename = null;

            for (int i=0; i<args.length-1; i++) {
                if(args[i].equals("-conf") || args[i].equals("-f")) {
                    confFilename = args[i+1];
                }
            }

            if (confFilename == null) {
                System.err.println("No configuration file given, quiting....");
                return;
            }
            Start start = new Start(confFilename, args);

            logger.info("Start start()");
            start.start();

            eventArmThread.join();
            waveFormArmThread.join();

            eventQueue.closeDatabase();
            logger.debug("Did not track the Thread bug Yet. so using System.exit()");
            System.exit(1);
        } catch(Exception e) {
            CommonAccess.handleException(e, "Problem in main");
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
        CommonAccess.handleException(sodException.getThrowable(),
                                     "Caught Exception in start because of the Listener");
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
            } catch(NumberFormatException nfe) {}
        }
        return REFRESH_INTERVAL;
    }

    private static int getQuitTime() {
        String str = props.getProperty("edu.sc.seis.sod.database.quitTime");
        if(str != null) {
            try {
                return Integer.parseInt(str);
            } catch(NumberFormatException nfe) {}
        }
        return QUIT_TIME;
    }

    private static boolean isReOpenEvents() {
        String str = props.getProperty("edu.sc.seis.sod.database.reopenEvents");
        if(str != null && str.equalsIgnoreCase("true")) {
            return true;
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

    public static int REFRESH_INTERVAL = 600;

    private static Properties props = System.getProperties();;

    private Document document;

    private static Logger logger = Logger.getLogger(Start.class);

    private static WaveformQueue waveformQueue;

    private static Queue eventQueue;

    private static Thread waveFormArmThread;

    private static Thread eventArmThread;

    private static WaveFormArm waveform;

    private static EventArm event;

    private static NetworkArm network;

    private static String DEFAULT_PROPS = "edu/sc/seis/sod/sod.prop";
}// Start

