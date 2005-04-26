package edu.sc.seis.sod;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.exceptionHandler.Extractor;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.exceptionHandler.SystemOutReporter;
import edu.sc.seis.fissuresUtil.exceptionHandler.WindowConnectionInterceptor;
import edu.sc.seis.sod.database.JDBCConfig;
import edu.sc.seis.sod.database.JDBCStatus;
import edu.sc.seis.sod.database.JDBCVersion;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.editor.SimpleGUIEditor;
import edu.sc.seis.sod.status.IndexTemplate;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.validator.Validator;

public class Start {

    static {
        GlobalExceptionHandler.add(new Extractor() {

            public boolean canExtract(Throwable throwable) {
                return (throwable instanceof org.apache.velocity.exception.MethodInvocationException);
            }

            public String extract(Throwable throwable) {
                String out = "";
                if(throwable instanceof org.apache.velocity.exception.MethodInvocationException) {
                    org.apache.velocity.exception.MethodInvocationException mie = (org.apache.velocity.exception.MethodInvocationException)throwable;
                    out += "Method Name: " + mie.getMethodName() + "\n";
                    out += "reference Name: " + mie.getReferenceName() + "\n";
                }
                return out;
            }

            public Throwable getSubThrowable(Throwable throwable) {
                if(throwable instanceof org.apache.velocity.exception.MethodInvocationException) { return ((org.apache.velocity.exception.MethodInvocationException)throwable).getWrappedThrowable(); }
                return null;
            }
        });
        GlobalExceptionHandler.registerWithAWTThread();
    }

    /**
     * Creates a new <code>Start</code> instance set to use the XML config
     * file in confFilename
     */
    public Start(String confFilename, String[] args) throws Exception {
        configFileName = confFilename;
        ClassLoader cl = getClass().getClassLoader();
        try {
            document = createDoc(createInputSource(cl, confFilename),
                                 confFilename);
        } catch(IOException io) {
            informUserOfBadFileAndExit(confFilename);
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Trouble creating xml document", e);
        }
        try {
            Validator validator = new Validator(Validator.SOD_SCHEMA_LOC);
            if(!validator.validate(createInputSource(cl, confFilename))) {
                logger.info("Invalid strategy file!");
                allHopeAbandon(validator.getErrorMessage());
            } else {
                logger.info("Valid strategy file");
            }
            if(onlyValidate) {
                System.exit(0);
            }
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Problem configuring schema validator",
                                          e);
            System.exit(0);
        }
        initDocument(args);
    }

    private static boolean onlyValidate = false;

    private static boolean waitOnError = true;

    private static boolean replaceDBConfig = false;

    private static void informUserOfBadFileAndExit(String confFilename) {
        File configFile = new File(confFilename);
        System.err.println("You told SOD to use "
                + configFile.getAbsolutePath() + " as its strategy file");
        if(configFile.exists()) {
            System.err.println("SOD was unable to open it.  Make sure the file is readable.");
        } else {
            System.err.println("SOD could find no such file.  Make sure the file exists");
        }
        System.exit(0);
    }

    public Start(Document document) throws Exception {
        this(document, new String[0]);
    }

    public Start(Document document, String[] args) throws Exception {
        this.document = document;
        initDocument(args);
    }

    public static MicroSecondDate getStartTime() {
        return startTime;
    }

    public static TimeInterval getElapsedTime() {
        return ClockUtil.now().subtract(startTime);
    }

    public static String getConfigFileName() {
        return configFileName;
    }

    protected void initDocument(String[] args) throws Exception {
        // get some defaults
        loadProps((Start.class).getClassLoader()
                .getResourceAsStream(DEFAULT_PROPS));
        for(int i = 0; i < args.length - 1; i++) {
            if(args[i].equals("-props")) {
                // override with values in local directory,
                // but still load defaults with original name
                loadProps(new FileInputStream(args[i + 1]));
                System.out.println("loaded file props from " + args[i + 1]);
            }
        }
        PropertyConfigurator.configure(props);
        logger.info("logging configured");
        //now override the properties with the properties specified
        // in the configuration file.
        loadRunProps(getDocument().getDocumentElement());
        //Must happen after the run props have been loaded
        IndexTemplate.setConfigFileLoc();
        //here the orb must be initialized ..
        //configure commonAccess
        CommonAccess.getCommonAccess().initORB(args, props);
    }

    public static void loadDbProperties(Properties sysProperties, String[] args)
            throws FileNotFoundException {
        Properties dbProperties = new Properties();
        for(int i = 0; i < args.length - 1; i++) {
            if(args[i].equals("-hsql")) {
                System.out.println("Loading db props");
                loadProps(new FileInputStream(args[i + 1]), dbProperties);
            }
        }
        loadDbProperties(props, dbProperties);
    }

    public static void loadDbProperties(Properties sysProperties,
                                        Properties dbProperties) {
        if(dbProperties.containsKey("server.port")) {
            if(dbProperties.containsKey(DBURL_KEY)) {
                logger.error("-hsql properties and SOD properties are both specifying the db connection.  Using -hsql properties");
            }
            //Use hsqldb properties specified in
            // http://hsqldb.sourceforge.net/doc/guide/ch04.html
            String url = "jdbc:hsqldb:hsql://localhost";
            if(dbProperties.containsKey("server.port")) {
                url += ":" + dbProperties.getProperty("server.port");
            }
            url += "/";
            if(dbProperties.containsKey("server.dbname.0")) {
                url += dbProperties.getProperty("server.dbname.0");
            }
            System.out.println("Connecting to db at " + url);
            ConnMgr.setURL(url);
        } else if(sysProperties.containsKey(DBURL_KEY)) {
            ConnMgr.setURL(sysProperties.getProperty(DBURL_KEY));
        }
    }

    public static void loadRunProps(Element document)
            throws ConfigurationException {
        Element propertiesElement = SodUtil.getElement(document, "properties");
        if(propertiesElement != null) {
            //load the properties fromt the configurationfile.
            runProps = new RunProperties(propertiesElement);
        } else {
            logger.debug("No properties specified in the configuration file");
        }
    }

    public static InputSource createInputSource(ClassLoader cl)
            throws IOException {
        return createInputSource(cl, getConfigFileName());
    }

    public static InputSource createInputSource(ClassLoader cl, String loc)
            throws IOException {
        InputStream in = null;
        if(loc.startsWith("http:") || loc.startsWith("ftp:")) {
            in = new URL(loc).openConnection().getInputStream();
        } else if(loc.startsWith("jar:")) {
            URL url = TemplateFileLoader.getUrl(cl, loc);
            in = url.openConnection().getInputStream();
        } else {
            in = new FileInputStream(loc);
        }
        if(in == null) { throw new IOException("Unable to load configuration file "
                + loc); }
        return new InputSource(new BufferedInputStream(in));
    }

    public static void initDocument(Document doc) {
        document = doc;
    }

    public static WaveformArm getWaveformArm() {
        return waveform;
    }

    public static EventArm getEventArm() {
        return event;
    }

    public static NetworkArm getNetworkArm() {
        return network;
    }

    public static RunProperties getRunProps() {
        return runProps;
    }

    public static Document createDoc(InputSource source, String filename)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new SimpleErrorHandler(filename));
        Document doc = builder.parse(source);
        return doc;
    }

    public void start() throws Exception {
        startTime = ClockUtil.now();
        new UpdateChecker(false);
        handleStartupRunProperties();
        checkDBVersion();
        ClassLoader cl = getClass().getClassLoader();
        checkConfig(createInputSource(cl, getConfigFileName()));
        // this next line sets up the status page for exception reporting, so
        // it should be as early as possible in the startup sequence
        IndexTemplate indexTemplate = null;
        if(runProps.doIndex()) {
            indexTemplate = new IndexTemplate();
        }
        Element docElement = document.getDocumentElement();
        startArms(docElement.getChildNodes());
        if(runProps.doIndex()) {
            indexTemplate.performRegistration();
        }
        new JDBCStatus();
    }

    public static void allHopeAbandon(String message) {
        System.err.println();
        System.err.println("******************************************************************");
        System.err.println();
        System.err.println(message);
        System.err.println();
        System.err.println("     All hope abandon, ye who enter in!");
        System.err.println();
        System.err.println("******************************************************************");
        if(waitOnError) {
            System.err.println();
            for(int i = 10; i >= 0; i--) {
                try {
                    Thread.sleep(1000);
                    System.err.print(" " + i);
                } catch(InterruptedException e) {}
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
        System.err.println("  To the eternal shades in heat and frost.\"");
        System.err.println();
        System.err.println();
        System.err.println(" ...a brave soul trudges on.");
    }

    private void startArms(NodeList armNodes) throws Exception {
        for(int i = 0; i < armNodes.getLength(); i++) {
            if(armNodes.item(i) instanceof Element) {
                Element el = (Element)armNodes.item(i);
                if(el.getTagName().equals("eventArm")) {
                    event = new EventArm(el);
                } else if(el.getTagName().equals("networkArm")) {
                    network = new NetworkArm(el);
                } else if(el.getTagName().startsWith("waveform")) {
                    int poolSize = runProps.getNumWaveformWorkerThreads();
                    waveform = new WaveformArm(el, event, network, poolSize);
                }
            }
        }
        if(RUN_ARMS) {
            new Thread(network, "Network Arm").start();
            new Thread(event, "Event Arm").start();
            new Thread(waveform, "Waveform Arm").start();
        }
    }

    private void handleStartupRunProperties() {
        if(runProps.removeDatabase()) {
            File dbDir = new File(DATABASE_DIR);
            if(dbDir.exists()) {
                logger.info("Removing old database");
                File[] dbFiles = dbDir.listFiles();
                for(int i = 0; i < dbFiles.length; i++) {
                    if(!dbFiles[i].delete()) {
                        logger.warn("Unable to delete "
                                + dbFiles[i]
                                + " when removing the previous database.  The old database might still exist");
                    }
                }
                if(!dbDir.delete()) {
                    logger.warn("Unable to delete the database directory.");
                }
            }
        } else if(runProps.reopenEvents()) {
            try {
                JDBCEventStatus eventStatus = new JDBCEventStatus();
                eventStatus.restartCompletedEvents();
            } catch(SQLException e) {
                GlobalExceptionHandler.handle("Trouble restarting completed events",
                                              e);
            }
        } else {
            File dbDir = new File(DATABASE_DIR);
            if(dbDir.exists()) {
                try {
                    JDBCEventChannelStatus evChanStatusTable = new JDBCEventChannelStatus();
                    suspendedPairs = evChanStatusTable.getSuspendedEventChannelPairs(runProps.getEventChannelPairProcessing());
                } catch(Exception e) {
                    GlobalExceptionHandler.handle("Trouble updating status of "
                            + "existing event-channel pairs", e);
                }
            }
        }
    }

    private void checkDBVersion() {
        try {
            JDBCVersion dbVersion = new JDBCVersion();
            if(!dbVersion.getDBVersion().equals(Version.getVersion())) {
                System.err.println("SOD version: " + Version.getVersion());
                System.err.println("Database version: "
                        + dbVersion.getDBVersion());
                System.err.println("Your database was created with an older version "
                        + "of SOD.");
                if(Version.hasSchemaChangedSince(dbVersion.getDBVersion())) {
                    allHopeAbandon("There has been a change in the database "
                            + "structure since the database was created!  "
                            + "Continuing this sod run is not advisable!");
                } else {
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
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Trouble checking database version",
                                          e);
        }
    }

    private void checkConfig(InputSource is) {
        try {
            String configString = JDBCConfig.extractConfigString(is);
            JDBCConfig dbConfig = new JDBCConfig(configString, replaceDBConfig);
            if(!dbConfig.isSameConfig(configString)) {
                allHopeAbandon("Your config file has changed since your last run.  "
                        + "It may not be advisable to continue this SOD run.");
            }
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Trouble checking stored config file",
                                          e);
        }
    }

    public static Document getDocument() {
        return document;
    }

    public static String getConfFileName(String[] args) {
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-conf") || args[i].equals("-f")) {
                return args[i + 1];
            } else if(args[i].equals("-demo")) { return SimpleGUIEditor.TUTORIAL_LOC; }
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            // this is not the real exception reporter, but do this to catch
            // initialization exceptions so they are not lost in the log file
            SystemOutReporter sysOutReporter = new SystemOutReporter();
            GlobalExceptionHandler.add(new WindowConnectionInterceptor());
            GlobalExceptionHandler.add(sysOutReporter);
            // start up log4j before read props so at least there is some
            // logging
            // later we will use PropertyConfigurator to really configure log4j
            BasicConfigurator.configure();
            String confFilename = getConfFileName(args);
            if(confFilename == null) {
                exit("No configuration file given.  Supply a configuration file "
                        + "using -f <configFile>, or to just see sod run use "
                        + "-demo.   quiting until that day....");
            }
            for(int i = 0; i < args.length; i++) {
                if(args[i].equals("-v")) {
                    onlyValidate = true;
                    waitOnError = false;
                } else if(args[i].equals("-i")) {
                    waitOnError = false;
                } else if(args[i].equals("--new-config")) {
                    replaceDBConfig = true;
                }
            }
            Start start = new Start(confFilename, args);
            logger.info("Start start()");
            start.start();
            // Error html dir and output should be set up now, so remove the
            // Std out reporter
            GlobalExceptionHandler.remove(sysOutReporter);
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Problem in main, quiting", e);
            exit("Problem in main, quiting: " + e.toString());
        }
        logger.info("Finished starting all threads.");
    } // end of main ()

    private static void exit(String reason) {
        logger.fatal(reason);
        System.err.println(reason);
        System.exit(1);
    }

    private static void loadProps(InputStream propStream) {
        loadProps(propStream, props);
    }

    private static void loadProps(InputStream propStream, Properties baseProps) {
        try {
            baseProps.load(propStream);
            propStream.close();
        } catch(Exception f) {
            GlobalExceptionHandler.handle("Problem loading props!", f);
            System.exit(0);
        }
    }

    public static void add(Properties newProps) {
        props.putAll(newProps);
    }

    public static final String DEFAULT_PARSER = "org.apache.xerces.parsers.SAXParser";

    private static Document document;

    private static Logger logger = Logger.getLogger(Start.class);

    private static WaveformArm waveform;

    private static Properties props = System.getProperties();

    private static RunProperties runProps;

    private static EventArm event;

    private static NetworkArm network;

    private static String configFileName;

    private static MicroSecondDate startTime;

    private static String DATABASE_DIR = "SodDb";

    public static final String DBURL_KEY = "sod.dburl";

    public static boolean RUN_ARMS = true;

    protected static int[] suspendedPairs = new int[0];

    public static final String DEFAULT_PROPS = "edu/sc/seis/sod/data/sod.prop";
}// Start
