package edu.sc.seis.sod;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.sc.seis.seisFile.fdsnws.FDSNWSException;
import edu.sc.seis.sod.hibernate.AbstractHibernateDB;
import edu.sc.seis.sod.hibernate.ConnMgr;
import edu.sc.seis.sod.hibernate.HibernateUtil;
import edu.sc.seis.sod.hibernate.Initializer;
import edu.sc.seis.sod.hibernate.NetworkNotFound;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEventDB;
import edu.sc.seis.sod.hibernate.eventpair.EventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventVectorPair;
import edu.sc.seis.sod.model.common.ToDoException;
import edu.sc.seis.sod.model.common.Version;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.retry.RetryStrategy;
import edu.sc.seis.sod.status.OutputScheduler;
import edu.sc.seis.sod.util.exceptionHandler.Extractor;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.util.exceptionHandler.MailExceptionReporter;
import edu.sc.seis.sod.util.exceptionHandler.MissingPropertyException;
import edu.sc.seis.sod.util.exceptionHandler.QuitOnExceptionPostProcess;
import edu.sc.seis.sod.util.exceptionHandler.ResultMailer;
import edu.sc.seis.sod.util.exceptionHandler.SystemOutReporter;
import edu.sc.seis.sod.util.exceptionHandler.WindowConnectionInterceptor;
import edu.sc.seis.sod.util.time.ClockUtil;
import edu.sc.seis.sod.validator.Validator;
import edu.sc.seis.sod.web.WebAdmin;

public class Start {

    static {
        GlobalExceptionHandler.add(new Extractor() {

            public boolean canExtract(Throwable throwable) {
                return (throwable instanceof FDSNWSException);
            }

            public String extract(Throwable throwable) {
                String out = "";
                if(throwable instanceof FDSNWSException) {
                    FDSNWSException mie = (FDSNWSException)throwable;
                    out += "URI: " + mie.getTargetURI() + "\n";
                }
                return out;
            }

            public Throwable getSubThrowable(Throwable throwable) {
                return null;
            }
        });
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
        GlobalExceptionHandler.add(new Extractor() {

            public boolean canExtract(Throwable throwable) {
                return (throwable instanceof org.hibernate.JDBCException);
            }

            public String extract(Throwable throwable) {
                String out = "";
                if(throwable instanceof org.hibernate.JDBCException) {
                    org.hibernate.JDBCException mie = (org.hibernate.JDBCException)throwable;
                    out += mie.getMessage();
                    out += "\nSQL: "+mie.getSQL();
                    out += "\nSQLState: "+mie.getSQLState();
                    if (AbstractHibernateDB.isSessionOpen()) {
                    out += "\n\nSession:\n"+AbstractHibernateDB.getSession().toString();
                    } else {
                        out+="\nSession: none\n";
                    }
                }
                return out;
            }

            public Throwable getSubThrowable(Throwable throwable) {
                if(throwable instanceof org.hibernate.JDBCException) {
                    Throwable sub = ((org.hibernate.JDBCException)throwable).getSQLException();
                    if (sub != throwable) {
                        return sub;
                    }
                }
                return null;
            }
        });
        GlobalExceptionHandler.add(new Extractor() {

            public boolean canExtract(Throwable throwable) {
                return (throwable instanceof FDSNWSException);
            }

            public String extract(Throwable throwable) {
                String out = "";
                if(throwable instanceof FDSNWSException) {
                    FDSNWSException mie = (FDSNWSException)throwable;
                    out += mie.getMessage();
                    out += "\nURI: "+mie.getTargetURI();
                }
                return out;
            }

            public Throwable getSubThrowable(Throwable throwable) {
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
        this(args, new InputSourceCreator(), null, false);
    }

    public static class InputSourceCreator {

        public InputSource create() throws IOException {
            return createInputSource(Start.class.getClassLoader(),
                                     configFileName);
        }
    }

    public Start(Args args,
                 InputSourceCreator sourceMaker,
                 Properties props,
                 boolean commandLineToolRun) throws Exception {
        Start.args = args;
        this.creator = sourceMaker;
        this.commandLineToolRun = commandLineToolRun;
        configFileName = args.getRecipe();
        if(props == null) {
            loadProps();
        } else {
            // this is for command line tools and unit tests
            Start.props = props;
        }
        try {
            setConfig(createDoc(sourceMaker.create(), configFileName).getDocumentElement());
        } catch(IOException io) {
            informUserOfBadFileAndExit(configFileName);
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Trouble creating xml document", e);
        }
        logger.info("logging configured");
        logger.info("SOD version "+VersionHistory.current().getVersion());
        logger.info("Args: "+args.toString());
        logger.info("Recipe: "+configFileName);
        if(args.isQuitOnError()) {
            // really die on first exception
            GlobalExceptionHandler.add(new QuitOnExceptionPostProcess(Throwable.class));
        }
        if(!commandLineToolRun) {
            validate(sourceMaker);
        }
        if (args.isPrintRecipe()) {
            BufferedReader in = new BufferedReader(new InputStreamReader(createInputStream(configFileName)));
            String line;
            while((line = in.readLine()) != null) {
                System.out.println(line);
            }
            System.out.flush();
            System.exit(0);
        }
        if (args.isQuickAndDirty()) {
            Start.props.put("fissuresUtil.database.url", "jdbc:hsqldb:mem:SodDB");
            logger.info("Database: memory");
        } else {
            logger.info("Database: "+Start.props.get("fissuresUtil.database.url"));
        }
            ConnMgr.setURL(Start.props.getProperty("fissuresUtil.database.url"));
            if (ConnMgr.getURL().startsWith(HSQL_FILE_URL)) {
                File dbFile = new File(ConnMgr.getURL().substring(HSQL_FILE_URL.length())+".log");
                if (dbFile.exists()) {
                    logger.info("Database file exists: "+dbFile.getPath());
                } else {
                    logger.info("Database file does not exist, clean start.");
                }
            }
        
        parseArms(config.getChildNodes());
    }

    private void validate(InputSourceCreator sourceMaker) {
        try {
            logger.info("validating recipe...");
            Validator validator = new Validator(Validator.SOD_SCHEMA_LOC);
            if(!validator.validate(sourceMaker.create())) {
                logger.info("Invalid recipe file!");
                allHopeAbandon(validator.getErrorMessage());
            } else {
                logger.info("Congratulations, valid recipe.");
                if (! args.isPrintRecipe()) {
                    System.out.println("Congratulations, valid recipe.");
                }
            }
            if(args.onlyValidate()) {
                System.exit(0);
            }
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Problem configuring schema validator",
                                          e);
            exit("Problem configuring schema validator: " + e.getMessage());
        }
    }

    private static void informUserOfBadFileAndExit(String confFilename) {
        File configFile = new File(confFilename);
        System.err.println("You told SOD to use "
                + configFile.getAbsolutePath() + " as its recipe file");
        if(configFile.exists()) {
            System.err.println("SOD was unable to open it.  Make sure the file is readable.");
        } else {
            System.err.println("SOD could find no such file.  Make sure the file exists");
        }
        System.exit(0);
    }

    public static void informUserOfBadNetworkAndExit(String networkCode, NetworkNotFound nnf) {
        logger.error("Can't find "+networkCode+" network from server", nnf);
        String msg = "You told SOD to use the '"
                + networkCode + "' network, but the server does not think it exists.";
        informUserOfBadQueryAndExit(msg, nnf);
    }

    public static void informUserOfBadQueryAndExit(String message, Exception e) {
        logger.error(message, e);
        System.err.println();
        System.err.println(message);
        System.err.println();
        System.err.println("    SOD is now cowardly quitting.");
        armFailure = true;
        wakeUpAllArms();
    }

    public static Instant getStartTime() {
        return startTime;
    }

    public static Duration getElapsedTime() {
        return Duration.between(startTime, ClockUtil.now());
    }

    public static String getConfigFileName() {
        return configFileName;
    }

    public void setupDatabaseForUnitTests() throws ConfigurationException {
        initDatabase();
    }
    
    protected void initDatabase() throws ConfigurationException {
        
        warnIfDatabaseExists();
        synchronized(HibernateUtil.class) {
            
            
            Iterator it = getRunProps().getHibernateConfig().iterator();
            while(it.hasNext()) {
                String res = (String)it.next();
                logger.debug("Adding resource to HibernateUtil:  "+res);
                // need to add to MetadataSources, but maybe not needed???
                throw new ToDoException("Adding hibernate configs not supported yet");
               // HibernateUtil.getConfiguration().addResource(res);
            }
            HibernateUtil.setUp(props, getClass().getResource("/edu/sc/seis/sod/data/ehcache.xml"));
        }
        try {
            HibernateUtil.deploySchema(true);
        } catch (Exception e) {
            throw new ConfigurationException("Unable to set up database", e);
        }
        // check that hibernate is ok
        SodDB sodDb = SodDB.getSingleton();
        sodDb.commit();
    }
    
    protected String HSQL_FILE_URL = "jdbc:hsqldb:file:";
    
    protected void warnIfDatabaseExists() {
        if ( ! args.isContinue() && getRunProps().warnIfDatabaseExists()) {
            // only matters if hsql???
            if (props.getProperty("hibernate.connection.url").startsWith(HSQL_FILE_URL)) {
                File dbFile = new File(ConnMgr.getURL().substring(HSQL_FILE_URL.length())+".log");
                if (dbFile.exists()) {
                    allHopeAbandon("The database for this run, "+dbFile
                                   +" appears to already exist. This is fine if you want to restart a run that crashed,"
                                   +" but if you are trying to start a fresh SOD run, you may wish to delete this database directory first."
                                   +" Otherwise, SOD will consider any work in this database as already completed and will not redo it.");
                }
            }
        }
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
        if (args.isDebug()) {
            // kind of dangerous as depends on prop file using names R, C, E
            props.setProperty("log4j.rootCategory", "debug, R, C, E");
        }
        PropertyConfigurator.configure(props);
        // Error html dir and output should be set up now, so remove the
        // Std out reporter
        GlobalExceptionHandler.remove(sysOutReporter);
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
            URL url = SodUtil.getUrl(cl, loc);
            in = url.openConnection().getInputStream();
        } else {
            in = new FileInputStream(loc);
        }
        if(in == null) {
            throw new IOException("Unable to load configuration file " + loc);
        }
        return new BufferedInputStream(in);
    }

    public static RetryStrategy createRetryStrategy(int numRetries) {
        if(commandName.equals("sod")) {
            return new UserReportRetryStrategy(numRetries, 
                                               "SOD will pick up where it left off when restarted.");
        } else {
            return new UserReportRetryStrategy(numRetries);
        }
    }

    public static void setCommandName(String name) {
        commandName = name;
    }

    public static void setConfig(Element config) {
        Start.config = config;
    }

    public static AbstractWaveformRecipe getWaveformRecipe() {
        return waveformRecipe;
    }
    
    public static EventArm getEventArm() {
        return event;
    }

    public static NetworkArm getNetworkArm() {
        return network;
    }
    
    @Deprecated
    public static WaveformArm[] getWaveformArms() {
        return waveforms;
    }

    public static WaveformArm[] getWaveformArmArray() {
        return waveforms;
    }

    public static RunProperties getRunProps() {
        if(runProps == null) {
            try {
                runProps = new RunProperties();
            } catch(ConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
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

    public static ResultMailer getResultMailer() throws ConfigurationException {
        if(mailer != null) {
            return mailer;
        }
        throw new ConfigurationException("no mailer configured");
    }

    public static void addResultMailer(Properties mailProps)
            throws MissingPropertyException {
        if(mailer == null && mailProps.containsKey("mail.smtp.host")) {
            mailer = new ResultMailer(mailProps);
        }
    }

    private static ResultMailer mailer;

    public void start() throws Exception {
        // startTime = ClockUtil.now();
        startTime = Instant.now();
        if(runProps.removeDatabase() || getArgs().isClean()) {
            cleanHSQLDatabase();
        }
        initDatabase();
        if(!commandLineToolRun) {
            new UpdateChecker(false);
            handleStartupRunProperties();
            checkDBVersion();
            checkConfig(creator.create());
        }
        // start web interface
        if (args.isStatusUnsecure()) {
            Start.getRunProps().setStatusUnsecure(true);
        }
        if (args.isStatus()) {
            Start.getRunProps().setStatusWebKeepAlive(true);
        }
        if(args.isStatus() || Start.getRunProps().isStatusWebKeepAlive()) {
            webAdmin = new WebAdmin();
            add(webAdmin); // listen for arm fails
            webAdmin.start();
        }
        
        startArms();
        if(!commandLineToolRun) {
            MailExceptionReporter.addMailExceptionReporter(props);;
            addResultMailer(props);
            if(runProps.checkpointPeriodically()) {
                new PeriodicCheckpointer();
            }
            if(runProps.loserEventCleaner()) {
                TotalLoserEventCleaner loserCleaner = new TotalLoserEventCleaner(getRunProps().getEventLag());
                Timer t = new Timer("TotalLoserCleaner", true);
                t.schedule(loserCleaner, 0, 7*24*60*60*1000);
            }
        }
    }

    public void allHopeAbandon(String message) {
        logger.info("All hope abandon: " + message);
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
    
    static void parseArms(NodeList armNodes) throws Exception {
        runProps = new RunProperties();
        waveforms = new WaveformArm[0]; // just in case a non-waveform run
        for (int i = 0; i < armNodes.getLength(); i++) {
            if (armNodes.item(i) instanceof Element) {
                Element el = (Element)armNodes.item(i);
                if (el.getTagName().equals("properties")) {
                        // load the properties from the configurationfile.
                        runProps.addProperties(el);
                } else if (el.getTagName().equals("eventArm") && args.doEventArm()) {
                    event = new EventArm(el);
                } else if (el.getTagName().equals("networkArm") && args.doNetArm()) {
                    network = new NetworkArm(el);
                } else if (el.getTagName().startsWith("waveform") && args.doWaveformArm()) {
                    if (el.getTagName().equals("waveformVectorArm")) {
                        SodDB.setDefaultEcpClass(EventVectorPair.class);
                        waveformRecipe = new MotionVectorArm(el);
                    } else if (el.getTagName().equals("waveformArm")) {
                        SodDB.setDefaultEcpClass(EventChannelPair.class);
                        waveformRecipe = new LocalSeismogramArm(el);
                    } else {
                        throw new ConfigurationException("unknown waveform arm type: " + el.getTagName());
                    }
                }
            }
        }
    }

    private void startArms() throws Exception {
        if (waveformRecipe != null) {
            if (runProps.reopenSuspended()) {
                Runnable reopenEvents = new Runnable() {
                    public void run() {
                        // check for ECPairs that need to be reprocessed
                        SodDB.getSingleton().reopenSuspendedEventChannelPairs(Start.getRunProps()
                                                                              .getEventChannelPairProcessing(),
                                                                              (waveformRecipe instanceof LocalSeismogramArm));
                        SodDB.commit();
                    }
                    
                };
                Thread t = new Thread(reopenEvents, "Reopen Suspended ECPS");
                t.start();
            }

            // check for events that are "in progress" due to halt or reset
            StatefulEventDB eventDb = StatefulEventDB.getSingleton();
            for (StatefulEvent ev = eventDb.getNext(Standing.IN_PROG); ev != null; ev = eventDb.getNext(Standing.IN_PROG)) {
                WaveformArm.createEventNetworkPairs(ev);
            }
            eventDb.commit();
            int poolSize = runProps.getNumWaveformWorkerThreads();
            waveforms = new WaveformArm[poolSize];
            for (int j = 0; j < waveforms.length; j++) {
                waveforms[j] = new WaveformArm(j, waveformRecipe);
            }
            Timer retryTimer = new Timer("retry loader", true);
            retryTimer.schedule(new TimerTask() {

                public void run() {
                    // only run if the retry queue is empty
                    if (!SodDB.getSingleton().isESPTodo()) {
                        SodDB.getSingleton().populateRetryToDo();
                        // db connection recycling
                        SodDB.rollback();
                    }
                }
            }, 0, 10 * 60 * 1000);
        }
        if (waveformRecipe == null && event != null) {
            event.setWaitForWaveformProcessing(false);
        }
        if (RUN_ARMS) {
            // Make sure the OutputScheduler exists when the arms are started
            OutputScheduler.getDefault();
            startArm(network, "NetworkArm");
            startArm(event, "EventArm");
            for (int i = 0; i < waveforms.length; i++) {
                startArm(waveforms[i], waveforms[i].getName());
            }
        }
        for (Iterator iter = armListeners.iterator(); iter.hasNext();) {
            ((ArmListener)iter.next()).started();
        }
    }

    public static void add(ArmListener listener) {
        armListeners.add(listener);
    }

    private void startArm(Arm arm, String name) throws ConfigurationException {
        if(arm != null) {
            for(Iterator iter = armListeners.iterator(); iter.hasNext();) {
                ArmListener element = (ArmListener)iter.next();
                element.starting(arm);
            }
            new Thread(arm, arm.getName()).start();
            logger.debug(name + " started");
        } else {
            logger.debug(name + " doesn't exist");
        }
    }

    void cleanHSQLDatabase() {
        String dbUrl = ConnMgr.getURL();
        if(!dbUrl.startsWith("jdbc:hsqldb")
                || dbUrl.indexOf("hsql://") != -1
                || !(dbUrl.indexOf(DATABASE_DIR) != -1)) {
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
    }
    
    private void handleStartupRunProperties() {
        if(runProps.reopenEvents()) {
            StatefulEventDB eventDb = StatefulEventDB.getSingleton();
            eventDb.restartCompletedEvents();
        }
    }

    private void checkDBVersion() {
        SodDB sodDb = SodDB.getSingleton();
        try {
            logger.debug("SodDB in check DBVersion:" + sodDb);
            Version dbVersion = sodDb.getDBVersion();
            SodDB.commit();
            if(dbVersion == null) {
                throw new RuntimeException("db version is null");
            }
            if(VersionHistory.hasSchemaChangedSince(dbVersion.getVersion())) {
                System.err.println("SOD version: "
                        + VersionHistory.current().getVersion());
                System.err.println("Database version: "
                        + dbVersion.getVersion());
                System.err.println("Your database was created with an older version "
                        + "of SOD.");
                allHopeAbandon("There has been a change in the database "
                        + "structure since the database was created!  "
                        + "Continuing this sod run is not advisable!");
            }
        } catch(Exception e) {
            logger.error("exception", e);
            SodDB.rollback();
            GlobalExceptionHandler.handle("Trouble checking database version",
                                          e);
        }
    }

    private void checkConfig(InputSource is) {
        SodDB sodDb = SodDB.getSingleton();
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e1) {}
        try {
            SodConfig conf = new SodConfig(new BufferedReader(is.getCharacterStream()));
            SodConfig dbConfig = sodDb.getCurrentConfig();
            if(dbConfig == null) {
                sodDb.putConfig(conf);
            } else if(dbConfig.getConfig().equals(conf.getConfig())) {} else {
                if(args.replaceDBConfig()) {
                    sodDb.putConfig(conf);
                } else {
                    allHopeAbandon("Your config file has changed since your last run.  "
                            + "It may not be advisable to continue this SOD run.");
                }
            }
            SodDB.commit();
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Trouble checking stored config file",
                                          e);
            SodDB.rollback();
        }
    }

    private boolean commandLineToolRun;

    private InputSourceCreator creator;

    private static Args args;

    public static Args getArgs() {
        return args;
    }
    
    public static Element getConfig() {
        return config;
    }

    // this is not the real exception reporter, but do this to catch
    // initialization exceptions so they are not lost in the log file
    private static SystemOutReporter sysOutReporter = new SystemOutReporter();

    public static void checkGCJ() {
        if (System.getProperty("java.vm.name").equals("GNU libgcj")) {
            System.err
                    .println("You are running GNU's version of Java, gcj, which doesn't have all the features SOD requires.  Instead, use Sun's Java from http://java.sun.com.");
            System.exit(-1);
        }
    }
    
    public static void main(String[] args) {
        try {
            checkGCJ();
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
            logger.error("User configuration problem, quiting", e);
            exit(e.getMessage()
                    + "  SOD will quit now and continue to cowardly quit until this is corrected.");
        } catch(Throwable e) {
            GlobalExceptionHandler.handle("Problem in main, quiting", e);
            exit("Quitting due to error: " + e.getMessage());
        }
        logger.info("Finished starting all threads.");
        if (webAdmin != null) {
            try {
                webAdmin.join();
            } catch(InterruptedException e) {
                logger.warn("WebAdmin interrupted.", e);
            }
        }
    } // end of main ()

    public static void exit(String reason) {
        System.err.println(reason);
        System.exit(1);
    }

    public static void add(Properties newProps) {
        props.putAll(newProps);
    }
    
    public static void cataclysmicFailureOfUnbelievableProportions() {
        try {
            System.err.println("Oh boy, this is really bad. No, it is even worse then that.");
            logger.error("horror of horrors...");
        } catch(Throwable t) {
        }
        System.exit(1);
    }

    public static void simpleArmFailure(Arm arm, String reason) {
        armFailure = true;
        logger.error("Arm " + arm.getName()
                     + " failed: "+reason+" Sod is giving up and quiting.");
        logger.debug("Arm " + arm.getName()
                     + " failure stack trace: "+reason, new Exception(reason));
        wakeUpAllArms();
    }
    
    public static void armFailure(Arm arm, Throwable t) {
        armFailure = true;
        GlobalExceptionHandler.handle("Problem running "
                                              + arm.getName()
                                              + ", SOD is exiting abnormally. "
                                              + "Please email this to the sod development team at sod@seis.sc.edu",
                                      t);
        logger.error("Arm " + arm.getName()
                + " failed. Sod is giving up and quiting", t);
        wakeUpAllArms();
    }
     
    public static void wakeUpAllArms() {
        // wake up any sleeping arms
        Arm[] arms = new Arm[] {network, event};
        for(int i = 0; i < arms.length; i++) {
            if (arms[i] != null) {
                synchronized(arms[i]) {
                    arms[i].notifyAll();
                }
            }
        }
        for (int i = 0; i < waveforms.length; i++) {
            if (waveforms[i] != null) {
                synchronized(waveforms[i]) {
                    waveforms[i].notifyAll();
                }
            }
        }
        synchronized(OutputScheduler.getDefault()) {
            OutputScheduler.getDefault().notify();
        }
    }
    
    public static boolean isAnyWaveformArmActive() {
        for (int i = 0; i < waveforms.length; i++) {
            if (waveforms[i].isActive()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isArmFailure() {
        return armFailure;
    }

    private static boolean armFailure = false;

    public static final String DEFAULT_PARSER = "org.apache.xerces.parsers.SAXParser";

    private static Element config;

    private static Logger logger = LoggerFactory.getLogger(Start.class);
    
    private static WaveformArm[] waveforms;
    
    private static AbstractWaveformRecipe waveformRecipe;

    private static Properties props = System.getProperties();

    private static RunProperties runProps;

    private static EventArm event;

    protected static NetworkArm network;

    private static String configFileName;

    private static String commandName = "sod";

    private static Instant startTime;
    
    private static WebAdmin webAdmin;

    private static String DATABASE_DIR = "SodDb";

    public static final String DBURL_KEY = "fissuresUtil.database.url";

    public static boolean RUN_ARMS = true;

    private static List armListeners = new ArrayList();

    public static final String TUTORIAL_LOC = "jar:edu/sc/seis/sod/data/configFiles/demo.xml";

    public static final String DEFAULT_PROPS = "edu/sc/seis/sod/data/sod.prop";
    
}// Start
