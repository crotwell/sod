package edu.sc.seis.sod;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.database.JDBCStatus;
import edu.sc.seis.sod.database.JDBCVersion;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import edu.sc.seis.sod.status.IndexTemplate;
import edu.sc.seis.sod.validator.Validator;
import java.io.BufferedInputStream;
import java.io.File;
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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Start{
    static { GlobalExceptionHandler.registerWithAWTThread(); }

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
                System.err.println("Invalid config file! ");
                System.err.println();
                System.err.println("     All hope abandon, ye who enter in!");
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

                System.err.println();
                System.err.println();
                System.err.println("And lo! towards us coming in a boat");
                System.err.println("  An old man, hoary with the hair of eld,");
                System.err.println("  Crying: \"Woe unto you, ye souls depraved!");
                System.err.println("");
                System.err.println("Hope nevermore to look upon the heavens;");
                System.err.println("  I come to lead you to the other shore,");
                System.err.println("  To the eternal shades in heat and frost.");
                System.err.println();
                System.err.println();

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
            runProps = new RunProperties(propertiesElement);
        } else {
            logger.debug("No properties specified in the configuration file");
        }

        //here the orb must be initialized ..
        //configure commonAccess
        CommonAccess.getCommonAccess().initORB(args, props);
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

    public static RunProperties getRunProps(){ return runProps; }

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
        UpdateChecker check = new UpdateChecker(false);
        checkDBVersion();
        handleStartupRunProperties();
        IndexTemplate indexTemplate = new IndexTemplate();
        Element docElement = document.getDocumentElement();
        startArms(docElement.getChildNodes());
        indexTemplate.performRegistration();
        new JDBCStatus();
    }

    private void startArms(NodeList armNodes) throws Exception{
        for (int i=0; i<armNodes.getLength(); i++) {
            if (armNodes.item(i) instanceof Element) {
                Element el = (Element)armNodes.item(i);
                if (el.getTagName().equals("eventArm")) {
                    event = new EventArm(el);
                } else if (el.getTagName().equals("networkArm")) {
                    network = new NetworkArm(el);
                } else if (el.getTagName().equals("waveformArm")) {
                    int poolSize = runProps.getNumWaveformWorkerThreads();
                    waveform = new WaveformArm(el, network, poolSize);
                }
            }
        }
        new Thread(event, "Event Arm").start();
        new Thread(waveform, "Waveform Arm").start();
    }

    private void handleStartupRunProperties() {
        if(runProps.removeDatabase()){
            File dbDir = new File("SodDb");
            if(dbDir.exists()){
                File[] dbFiles = dbDir.listFiles();
                for (int i = 0; i < dbFiles.length; i++) {
                    dbFiles[i].delete();
                }
                dbDir.delete();
            }
        }else if(runProps.reopenEvents()){
            try {
                JDBCEventStatus eventStatus = new JDBCEventStatus();
                eventStatus.restartCompletedEvents();
            } catch (SQLException e) {
                GlobalExceptionHandler.handle("Trouble restarting completed events", e);
            }
        }
    }

    private void checkDBVersion(){
        try {
            JDBCVersion dbVersion = new JDBCVersion();
            if (!dbVersion.getDBVersion().equals(Version.getVersion())){
                System.err.println("SOD version: " + Version.getVersion());
                System.err.println("Database version: " + dbVersion.getDBVersion());
                System.err.println("Your database was created with an older version "
                                       + "of SOD.");
                if (Version.hasSchemaChangedSince(dbVersion.getDBVersion())){
                    System.err.println("There has been a change in the database "
                                           + "structure since the database was created!  "
                                           + "Continuing this sod run is not advisable!!!");
                }
                else {
                    System.err.println("The structure of the database has not "
                                      + "changed, so SOD may work if there "
                                      + "haven't been significant underlying "
                                      + "changes in SOD. Check "
                                      + "http://www.seis.sc.edu/SOD/download.html "
                                      + "to see the differences between your "
                                      + "running version, " + Version.getVersion()
                                      + ", and the version that created the "
                                      + "database, " + dbVersion.getDBVersion());
                }
            }
        } catch (Exception e) {
            GlobalExceptionHandler.handle("Trouble checking database version", e);
        }
    }

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
                exit("No configuration file given, quiting....");
            }
            Start start = new Start(confFilename, args);

            logger.info("Start start()");
            start.start();
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Problem in main, quiting", e);
            exit("Problem in main, quiting: "+e.toString());
        }
        logger.info("Finished starting all threads.");
    } // end of main ()

    private static void exit(String reason) {
        logger.fatal(reason);
        System.err.println(reason);
        System.exit(1);
    }

    private static void loadProps(InputStream propStream){
        try {
            props.load(propStream);
            propStream.close();
        } catch (Exception f) {
            GlobalExceptionHandler.handle("Problem loading props!", f);
            System.exit(0);
        }
    }


    public static void add(Properties newProps){
        props.putAll(newProps);
    }


    public static final String
        DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

    private Document document;

    private static Logger logger = Logger.getLogger(Start.class);

    private static WaveformArm waveform;

    private static Properties props = System.getProperties();

    private static RunProperties runProps;

    private static EventArm event;

    private static NetworkArm network;

    private static String configFile;

    private static MicroSecondDate startTime;

    public static final String DEFAULT_PROPS = "edu/sc/seis/sod/sod.prop";
}// Start

