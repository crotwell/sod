package edu.sc.seis.sod;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import edu.sc.seis.sod.status.IndexTemplate;
import edu.sc.seis.sod.validator.Validator;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.status.FileWritingTemplate;

public class Start{

    static {
        GlobalExceptionHandler.registerWithAWTThread();
    }

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
            configFile = confFilename;
        } catch (Exception e) {
            GlobalExceptionHandler.handle("Trouble creating xml document", e);
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
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(1000);
                        System.err.print(" *");
                    }
                    catch (InterruptedException e) {
                    }
                }
                System.err.println(" ...a brave soul trudges on.");
            }else{
                logger.info("Valid config file");
            }
        } catch (Exception e) {
            GlobalExceptionHandler.handle("Problem configuring schema validator", e);
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

    public static MicroSecondDate getStartTime(){ return startTime; }

    public static TimeInterval getElapsedTime(){
        return ClockUtil.now().subtract(startTime);
    }

    public static String getConfigFileName(){ return configFile; }

    public static String getRunName(){
        if(runName == null){
            runName = props.getProperty("sod.start.RunName", "Your Sod");
        }
        return runName;
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

        executeRestartOptions();
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

    public static WaveformArm getWaveformArm() { return waveform; }

    public static EventArm getEventArm() { return event; }

    public static NetworkArm getNetworkArm() { return network; }

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
        startTime = ClockUtil.now();
        IndexTemplate indexTemplate = new IndexTemplate();
        Element docElement = document.getDocumentElement();
        logger.info("start "+docElement.getTagName());
        NodeList children = docElement.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element) {
                Element el = (Element)node;
                if (el.getTagName().equals("description")) {
                    logger.info(el.getTagName());
                } else if (el.getTagName().equals("eventArm")) {
                    logger.info(el.getTagName());
                    event = new EventArm(el);
                    eventArmThread = new Thread(event, "eventArm thread");
                    eventArmThread.start();
                } else if (el.getTagName().equals("networkArm")) {
                    logger.info(el.getTagName());
                    network = new NetworkArm(el);
                } else if (el.getTagName().equals("waveformArm")) {
                    logger.info(el.getTagName());
                    int poolSize =
                        Integer.parseInt(props.getProperty("edu.sc.seis.sod.waveformarm.threads",
                                                           "5"));
                    waveform = new WaveformArm(el, network, poolSize);
                    waveformArmThread = new Thread(waveform, "waveformArm Thread");
                    waveformArmThread.start();
                }  else {
                    logger.debug("process "+el.getTagName());
                }
            }
        }
        indexTemplate.registerMapWithEventArm();
    }

    public static Properties getProperties() {return props; }

    public Document getDocument(){ return document; }

    public static void main (String[] args) {
        try {
            // start up log4j before read props so at least there is some logging
            // later we will use PropertyConfigurator to really configure log4j
            BasicConfigurator.configure();
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
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Problem in main", e);
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

    public static void add(Properties newProps){
        props.putAll(newProps);
    }

    private static  void executeRestartOptions() {
        //TODO - use this in new db
        Start.REMOVE_DATABASE = isRemoveDatabase();
        if(isReopenEvents()){
            try {
                JDBCEventStatus eventStatus = new JDBCEventStatus();
                eventStatus.restartCompletedEvents();
            } catch (SQLException e) {
                GlobalExceptionHandler.handle("Trouble restarting completed events", e);
            }
        }
    }

    public static TimeInterval getIntervalProp(TimeInterval defaultInterval,
                                               String propName) throws NoSuchFieldException {
        String unitName = props.getProperty(propName + ".unit", "DAY");
        if(unitName != null){
            UnitImpl unit = UnitImpl.getUnitFromString(unitName);
            int val = getIntProp(propName + ".value", -1);
            if(val != -1) return new TimeInterval(val, unit);
        }
        return defaultInterval;
    }

    private static boolean isRemoveDatabase() {
        String str = props.getProperty("edu.sc.seis.sod.database.remove");
        if(str != null) {
            if(str.equalsIgnoreCase("true")) { return true;}
        }
        return false;
    }

    public static int getIntProp(String propName, int defaultValue){
        String str = props.getProperty(propName);
        if(str != null) {
            try {
                return Integer.parseInt(str);
            } catch(NumberFormatException nfe) {}
        }
        return defaultValue;
    }

    private static boolean isReopenEvents() {
        String str = props.getProperty("sod.start.ReopenEvents");
        if(str != null && str.equalsIgnoreCase("true"))  return true;
        return false;
    }

    public static final String
        DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

    public static boolean REMOVE_DATABASE = false;

    private static Properties props = System.getProperties();

    private Document document;

    private static Logger logger = Logger.getLogger(Start.class);

    private static Thread waveformArmThread;

    private static Thread eventArmThread;

    private static WaveformArm waveform;

    private static EventArm event;

    private static NetworkArm network;

    private static String configFile, runName = null;

    private static MicroSecondDate startTime;

    private static String DEFAULT_PROPS = "edu/sc/seis/sod/sod.prop";
}// Start

