package edu.sc.seis.sod;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
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
import edu.sc.seis.fissuresUtil.cache.RetryStrategy;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.exceptionHandler.Extractor;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.exceptionHandler.SystemOutReporter;
import edu.sc.seis.fissuresUtil.exceptionHandler.WindowConnectionInterceptor;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.sod.database.JDBCConfig;
import edu.sc.seis.sod.database.JDBCStatus;
import edu.sc.seis.sod.database.JDBCVersion;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.IndexTemplate;
import edu.sc.seis.sod.status.OutputScheduler;
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
                if(throwable instanceof org.apache.velocity.exception.MethodInvocationException) {
                    return ((org.apache.velocity.exception.MethodInvocationException)throwable).getWrappedThrowable();
                }
                return null;
            }
        });
        GlobalExceptionHandler.registerWithAWTThread();
    }

    /**
     * Creates a new <code>Start</code> instance set to use the XML config
     * file in confFilename
     */
    public Start(Args args) throws Exception {
        this(args, new InputSourceCreator(), null);
    }

    public static class InputSourceCreator {

        public InputSource create() throws IOException {
            return createInputSource(Start.class.getClassLoader(),
                                     configFileName);
        }
    }

    public Start(Args args, InputSourceCreator sourceMaker, Properties props)
            throws Exception {
        this.args = args;
        this.creator = sourceMaker;
        configFileName = args.getRecipe();
        try {
            setConfig(createDoc(sourceMaker.create(), configFileName).getDocumentElement());
        } catch(IOException io) {
            informUserOfBadFileAndExit(configFileName);
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Trouble creating xml document", e);
        }
        try {
            Validator validator = new Validator(Validator.SOD_SCHEMA_LOC);
            if(!validator.validate(sourceMaker.create())) {
                logger.info("Invalid strategy file!");
                allHopeAbandon(validator.getErrorMessage());
            }
            if(args.onlyValidate()) {
                System.exit(0);
            }
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Problem configuring schema validator",
                                          e);
            exit("Problem configuring schema validator: " + e.getMessage());
        }
        if(props == null) {
            loadProps();
        } else {
            Start.props = props;
        }
        initDocument();
    }

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

    public static MicroSecondDate getStartTime() {
        return startTime;
    }

    public static TimeInterval getElapsedTime() {
        return ClockUtil.now().subtract(startTime);
    }

    public static String getConfigFileName() {
        return configFileName;
    }

    protected void initDocument() throws Exception {
        loadRunProps(getConfig());
        ConnMgr.installDbProperties(props, args.getInitialArgs());
        CommonAccess.initialize(props, args.getInitialArgs());
    }

    private void loadProps() throws IOException {
        // get some defaults
        Initializer.loadProps((Start.class).getClassLoader()
                .getResourceAsStream(DEFAULT_PROPS), props);
        if(args.hasProps()) {
            try {
                Initializer.loadProps(args.getProps(), props);
            } catch(IOException io) {
                System.err.println("Unable to load props file: "
                        + io.getMessage());
                System.err.println("Quitting until the error is corrected");
                System.exit(1);
            }
        }
        PropertyConfigurator.configure(props);
        logger.info("logging configured");
        // Error html dir and output should be set up now, so remove the
        // Std out reporter
        GlobalExceptionHandler.remove(sysOutReporter);
    }

    public static void loadRunProps(Element doc) throws ConfigurationException {
        Element propertiesElement = SodUtil.getElement(doc, "properties");
        if(propertiesElement != null) {
            // load the properties fromt the configurationfile.
            runProps = new RunProperties(propertiesElement);
        } else {
            logger.debug("No properties specified in the configuration file");
            runProps = new RunProperties();
        }
    }

    public static InputSource createInputSource(ClassLoader cl)
            throws IOException {
        return createInputSource(cl, getConfigFileName());
    }

    public static InputSource createInputSource(ClassLoader cl, String loc)
            throws IOException {
        return new InputSource(new InputStreamReader(createInputStream(cl, loc)));
    }

    public static InputStream createInputStream(String loc) throws IOException,
            MalformedURLException, FileNotFoundException {
        return createInputStream(Start.class.getClassLoader(), loc);
    }

    public static InputStream createInputStream(ClassLoader cl, String loc)
            throws IOException, MalformedURLException, FileNotFoundException {
        InputStream in = null;
        if(loc.startsWith("http:") || loc.startsWith("ftp:")) {
            in = new URL(loc).openConnection().getInputStream();
        } else if(loc.startsWith("jar:")) {
            URL url = TemplateFileLoader.getUrl(cl, loc);
            in = url.openConnection().getInputStream();
        } else {
            in = new FileInputStream(loc);
        }
        if(in == null) {
            throw new IOException("Unable to load configuration file " + loc);
        }
        return new BufferedInputStream(in);
    }

    public static RetryStrategy createRetryStrategy() {
        if(commandName.equals("sod")) {
            return new UserReportRetryStrategy("SOD will pick up where it left off when restarted.");
        } else {
            return new UserReportRetryStrategy();
        }
    }

    public static void setCommandName(String name) {
        commandName = name;
    }

    public static void setConfig(Element config) {
        Start.config = config;
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

    public static void addMailExceptionReporter(Properties mailProps) {
        if(mailProps.containsKey("mail.smtp.host")) {
            try {
                GlobalExceptionHandler.add(new MailExceptionReporter(mailProps));
            } catch(ConfigurationException e) {
                logger.debug("Not able to add a mail reporter.  This is only a problem if you specified one",
                             e);
            }
        } else {
            logger.debug("Not trying to add a mail reporter since mail.smtp.host isn't set");
        }
    }

    public static ResultMailer getResultMailer() throws ConfigurationException {
        if(mailer != null) {
            return mailer;
        }
        throw new ConfigurationException("no mailer configured");
    }

    public static void addResultMailer(Properties mailProps)
            throws ConfigurationException {
        if(mailer == null && mailProps.containsKey("mail.smtp.host")) {
            mailer = new ResultMailer(mailProps);
        }
    }

    private static ResultMailer mailer;

    public void start() throws Exception {
        startTime = ClockUtil.now();
        new UpdateChecker(false);
        handleStartupRunProperties();
        checkDBVersion();
        checkConfig(creator.create());
        // this next line sets up the status page for exception reporting, so
        // it should be as early as possible in the startup sequence
        IndexTemplate indexTemplate = null;
        if(runProps.doStatusPages()) {
            indexTemplate = new IndexTemplate();
        }
        startArms(getConfig().getChildNodes());
        if(runProps.doStatusPages()) {
            indexTemplate.performRegistration();
        }
        new JDBCStatus();
        addMailExceptionReporter(props);
        addResultMailer(props);
        if(runProps.checkpointPeriodically()) {
            new PeriodicCheckpointer();
        }
        if(runProps.loserEventCleaner()) {
            new TotalLoserEventCleaner();
        }
    }

    public void allHopeAbandon(String message) {
        System.err.println();
        System.err.println("******************************************************************");
        System.err.println();
        System.err.println(message);
        System.err.println();
        System.err.println("     All hope abandon, ye who enter in!");
        System.err.println();
        System.err.println("******************************************************************");
        if(args.waitOnError()) {
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
        OutputScheduler sched = OutputScheduler.getDefault();
        for(int i = 0; i < armNodes.getLength(); i++) {
            if(armNodes.item(i) instanceof Element) {
                Element el = (Element)armNodes.item(i);
                if(el.getTagName().equals("eventArm") && args.doEventArm()) {
                    event = new EventArm(el);
                    sched.registerArm(event);
                } else if(el.getTagName().equals("networkArm")
                        && args.doNetArm()) {
                    network = new NetworkArm(el);
                    sched.registerArm(network);
                } else if(el.getTagName().startsWith("waveform")
                        && args.doWaveformArm()) {
                    int poolSize = runProps.getNumWaveformWorkerThreads();
                    waveform = new WaveformArm(el, event, network, poolSize);
                    sched.registerArm(waveform);
                }
            }
        }
        if(waveform == null && event != null) {
            event.setWaitForWaveformProcessing(false);
        }
        if(RUN_ARMS) {
            startArm(network, "NetworkArm");
            startArm(event, "EventArm");
            startArm(waveform, "WaveformArm");
        }
    }

    private void startArm(Arm arm, String name) {
        if(arm != null) {
            new Thread(arm, arm.getName()).start();
            logger.debug(name + " started");
        } else {
            logger.debug(name + " doesn't exist");
        }
    }

    private void handleStartupRunProperties() {
        if(runProps.removeDatabase()) {
            String dbUrl = ConnMgr.getURL();
            if(!dbUrl.startsWith("jdbc:hsqldb") || dbUrl.contains("hsql://")
                    || !dbUrl.contains(DATABASE_DIR)) {
                logger.warn("The database isn't the default local hsqldb, so it couldn't be deleted as specified by the properties");
                return;
            }
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
            try {
                JDBCEventChannelStatus evChanStatusTable = new JDBCEventChannelStatus();
                suspendedPairs = evChanStatusTable.getSuspendedEventChannelPairs(runProps.getEventChannelPairProcessing());
                logger.debug("Found " + suspendedPairs.length
                        + " event channel pairs that were in process");
            } catch(Exception e) {
                GlobalExceptionHandler.handle("Trouble updating status of "
                        + "existing event-channel pairs", e);
            }
        }
    }

    private void checkDBVersion() {
        try {
            JDBCVersion dbVersion = new JDBCVersion();
            if(Version.hasSchemaChangedSince(dbVersion.getDBVersion())) {
                System.err.println("SOD version: " + Version.getVersion());
                System.err.println("Database version: "
                        + dbVersion.getDBVersion());
                System.err.println("Your database was created with an older version "
                        + "of SOD.");
                allHopeAbandon("There has been a change in the database "
                        + "structure since the database was created!  "
                        + "Continuing this sod run is not advisable!");
            }
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Trouble checking database version",
                                          e);
        }
    }

    private void checkConfig(InputSource is) {
        try {
            String configString = JDBCConfig.extractConfigString(is);
            JDBCConfig dbConfig = new JDBCConfig(configString,
                                                 args.replaceDBConfig());
            if(!dbConfig.isSameConfig(configString)) {
                allHopeAbandon("Your config file has changed since your last run.  "
                        + "It may not be advisable to continue this SOD run.");
            }
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Trouble checking stored config file",
                                          e);
        }
    }

    private InputSourceCreator creator;

    private Args args;

    public static Element getConfig() {
        return config;
    }

    // this is not the real exception reporter, but do this to catch
    // initialization exceptions so they are not lost in the log file
    private static SystemOutReporter sysOutReporter = new SystemOutReporter();

    public static void main(String[] args) {
        try {
            GlobalExceptionHandler.add(new WindowConnectionInterceptor());
            GlobalExceptionHandler.add(sysOutReporter);
            // start up log4j before read props so at least there is some
            // logging
            // later we will use PropertyConfigurator to really configure log4j
            BasicConfigurator.configure();
            Start start = new Start(new Args(args));
            logger.info("Start start()");
            start.start();
        } catch(UserConfigurationException e) {
            exit(e.getMessage()
                    + "  SOD will quit now and continue to cowardly quit until this is corrected.");
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Problem in main, quiting", e);
            exit("Quitting due to error: " + e.getMessage());
        }
        logger.info("Finished starting all threads.");
    } // end of main ()

    public static void exit(String reason) {
        System.err.println(reason);
        System.exit(1);
    }

    public static void add(Properties newProps) {
        props.putAll(newProps);
    }

    public static void armFailure(Arm arm, Throwable t) {
        armFailure = true;
        GlobalExceptionHandler.handle("Problem running "
                                              + arm.getName()
                                              + ", SOD is exiting abnormally. "
                                              + "Please email this to the sod development team at sod@seis.sc.edu",
                                      t);
        logger.fatal("Arm " + arm.getName()
                + " failed. Sod is giving up and quiting", t);
        // wake up any sleeping arms
        Arm[] arms = new Arm[] {network, event, waveform};
        for(int i = 0; i < arms.length; i++) {
            synchronized(arms[i]) {
                arms[i].notify();
            }
        }
        synchronized(OutputScheduler.getDefault()) {
            OutputScheduler.getDefault().notify();
        }
    }

    public static boolean isArmFailure() {
        return armFailure;
    }

    private static boolean armFailure = false;

    public static final String DEFAULT_PARSER = "org.apache.xerces.parsers.SAXParser";

    private static Element config;

    private static Logger logger = Logger.getLogger(Start.class);

    private static WaveformArm waveform;

    private static Properties props = System.getProperties();

    private static RunProperties runProps;

    private static EventArm event;

    protected static NetworkArm network;

    private static String configFileName, commandName = "sod";

    private static MicroSecondDate startTime;

    private static String DATABASE_DIR = "SodDb";

    public static final String DBURL_KEY = "fissuresUtil.database.url";

    public static boolean RUN_ARMS = true;

    protected static int[] suspendedPairs = new int[0];

    public static final String TUTORIAL_LOC = "jar:edu/sc/seis/sod/data/configFiles/demo.xml";

    public static final String DEFAULT_PROPS = "edu/sc/seis/sod/data/sod.prop";
}// Start
