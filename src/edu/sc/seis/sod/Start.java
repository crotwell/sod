package edu.sc.seis.sod;

import edu.sc.seis.sod.database.*;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.*;
import org.apache.log4j.*;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
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
     * Creates a new <code>Start</code> instance.
     *
     * @param configFile an <code>InputStream</code> value
     */
    public Start (InputStream configFile) {
        this.configFile = configFile;
    }

    /**
     * Describe <code>init</code> method here.
     *
     * @exception ParserConfigurationException if an error occurs
     * @exception org.xml.sax.SAXException if an error occurs
     * @exception IOException if an error occurs
     */
    public void init() throws ConfigurationException,
        ParserConfigurationException,
        org.xml.sax.SAXException,
        IOException {
        document = initParser(configFile);
    }

    /**
     * Describe <code>start</code> method here.
     *
     * @exception ConfigurationException if an error occurs
     */
    public void startA() throws Exception {
        Element docElement = document.getDocumentElement();
        logger.info("start "+docElement.getTagName());
        NodeList children = docElement.getChildNodes();
        Node node;
        Class[] constructorArgTypes = new Class[1];
        constructorArgTypes[0] = Element.class;

        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if (node instanceof Element) {
                Element subElement = (Element)node;
                if (subElement.getTagName().equals("description")) {
                    logger.info(subElement.getTagName());
                } else if (subElement.getTagName().equals("eventArm")) {

                    logger.info(subElement.getTagName());
                    eventArm = new EventArm(subElement, this, this.props);
                    eventArmThread = new Thread(eventArm);
                    eventArmThread.setName("eventArm Thread");
                    eventArmThread.start();

                } else if (subElement.getTagName().equals("networkArm")) {
                    logger.info(subElement.getTagName());
                    networkArm = new NetworkArm(subElement);

                } else if (subElement.getTagName().equals("waveFormArm")) {
                    logger.info(subElement.getTagName());
                    int threadPoolSize =
                        Integer.parseInt(getProperties().getProperty("edu.sc.seis.sod.waveformarm.threads",
                                                                     "5"));
                    waveFormArm = new WaveFormArm(subElement,
                                                  networkArm,
                                                  this,
                                                  threadPoolSize);
                    waveFormArmThread = new Thread(waveFormArm);
                    waveFormArmThread.setName("waveFormArm Thread");
                    // Thread.sleep(100000);
                    waveFormArmThread.start();


                }  else {
                    logger.debug("process "+subElement.getTagName());

                }
            }
        }
    }

    /**
     * Describe <code>getEventQueue</code> method here.
     *
     * @return an <code>EventQueue</code> value
     */
    public static Queue getEventQueue() {

        return eventQueue;

    }


    public static WaveformQueue getWaveformQueue() {
        return waveformQueue;
    }

    public static void setProperties(Properties props) {

        Start.props = props;

    }

    public static Properties getProperties() {

        return props;

    }

    /**
     * Describe <code>main</code> method here.
     *
     * @param args a <code>String[]</code> value
     */
    public static void main (String[] args) {
        try {
            Properties props = System.getProperties();
            props.put("org.omg.CORBA.ORBClass", "com.ooc.CORBA.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                      "com.ooc.CORBA.ORBSingleton");

            // get some defaults
            String propFilename=
                "sod.prop";
            String defaultsFilename=
                "edu/sc/seis/sod/"+propFilename;
            boolean commandlineProps = false;
            String confFilename = null;

            for (int i=0; i<args.length-1; i++) {
                if (args[i].equals("-props")) {
                    // override with values in local directory,
                    // but still load defaults with original name
                    propFilename = args[i+1];
                    commandlineProps = true;
                } if(args[i].equals("-conf") || args[i].equals("-f")) {
                    confFilename = args[i+1];
                }
            }



            // eventQueue = new HSqlDbQueue(commonAccess.getORB());

            boolean defaultPropLoadOK = false;
            boolean commandlinePropLoadOK = false;
            Exception preloggingException = null;

            try {
                props.load((Start.class).getClassLoader().getResourceAsStream(defaultsFilename ));
                defaultPropLoadOK = true;
            } catch (IOException e) {
                defaultPropLoadOK = false;
                preloggingException = e;
            }
            if (commandlineProps) {
                try {
                    FileInputStream in = new FileInputStream(propFilename);
                    props.load(in);
                    in.close();
                } catch (Exception f) {
                    commandlinePropLoadOK = false;
                    preloggingException = f;
                }
            } // end of if (commandlineProps)

            setProperties(props);

            if (defaultPropLoadOK) {
                // configure logging from properties...
                PropertyConfigurator.configure(props);
            } else {
                // can't configure logging from properties,
                // use basic which goes to console...
                BasicConfigurator.configure();
                logger.warn("Unable to get configuration properties!",
                            preloggingException);
            } // end of else
            logger.info("Logging configured");

            //done with the properties loading... now load the configuration file.
            //String filename
            //= props.getProperty("edu.sc.seis.sod.configuration");

            if (confFilename == null) {
                System.err.println("No configuration file given, quiting....");
                logger.fatal("No configuration file given, quiting....");
                return;
            } // end of if (filename == null)

            InputStream in;

            String schemaFilename = "edu/sc/seis/sod/data/sod.xsd";
            schemaURL =
                (Start.class).getClassLoader().getResource(schemaFilename);
            logger.debug(schemaFilename+"->"+schemaURL.toString());
            if (schemaURL == null) {
                logger.fatal("Can't find the sod.xsd xschema file, this may indicate a corrupt installation of sod! Cowardly quitting at this point.");
                return;
            }

            boolean b = Validator.validate(confFilename);
            if (b) {
                System.err.println("The configuration file did not validate against the xschema for sod.");
                logger.fatal("The configuration file did not validate against the xschema for sod.");
                System.err.println("Please see the log file for more information.");
                return;
            }

            if (confFilename.startsWith("http:") || confFilename.startsWith("ftp:")) {
                java.net.URL url = new java.net.URL(confFilename);
                java.net.URLConnection conn = url.openConnection();
                in = new BufferedInputStream(conn.getInputStream());
            } else {
                in = new BufferedInputStream(new FileInputStream(confFilename));
            } // end of else

            if (in == null) {
                logger.fatal("Unable to load configuration file "+confFilename+", quiting...");
                return;
            } // end of if (in == null)

            Start start = new Start(in);
            logger.info("Start init()");
            start.init();

            //now override the properties with the properties specified
            // in the configuration file.
            Element docElement = document.getDocumentElement();
            Element propertiesElement = SodUtil.getElement(docElement, "properties");
            if(propertiesElement != null) {
                //load the properties fromt the configurationfile.
                SodUtil.loadProperties(propertiesElement, props);
                setProperties(props);
            } else {
                logger.debug("No properties specified in the configuration file");
            }

            //here the orb must be initialized ..
            //configure commonAccess
            CommonAccess commonAccess = CommonAccess.getCommonAccess();
            commonAccess.initORB(args, props);

            checkRestartOptions();

            //configure the eventQueue and waveformQueue.
            eventQueue = new HSqlDbQueue(props);
            waveformQueue = new WaveformDbQueue(props);
            waveformQueue.clean();
            eventQueue.clean();

            logger.info("Start start()");
            start.startA();
            eventArmThread.join();
            waveFormArmThread.join();

            getEventQueue().closeDatabase();
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

    /** use sax vlidation as there seems to be a problem telling the
     xerces impl built into java1.4 to validate. This is just slower to use
     a separate step, but probably worth it. */
    public static void validate2(Reader xmlFile)
        throws ConfigurationException  {
        try {
            XMLReader parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
            //(XMLReader)Class.forName(DEFAULT_PARSER_NAME).newInstance();
            SimpleErrorHandler errorHandler = new SimpleErrorHandler();
            parser.setErrorHandler(errorHandler);
            //    parser.setFeature( "http://xml.org/sax/features/validation",
            //                    true);
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
            parser.setFeature( "http://xml.org/sax/features/namespaces",
                              true );

            parser.setFeature( "http://apache.org/xml/features/validation/schema",
                              true );
            parser.setFeature( "http://apache.org/xml/features/validation/schema-full-checking",
                              false );
            parser.setEntityResolver(new  EntityResolver() {
                        public InputSource resolveEntity(String publicId,
                                                         String systemId) {
                            try {
                                logger.info("entity resolve "+publicId+" "+systemId);
                                if (systemId.equals("http://www.seis.sc.edu/xschema/sod/1.0/sod.xsd") ||
                                    systemId.equals("http://www.seis.sc.edu/xschema/sod/1.0/utilities.xsd") ||
                                    systemId.equals("http://www.seis.sc.edu/xschema/sod/1.0/EventArm.xsd") ||
                                    systemId.equals("http://www.seis.sc.edu/xschema/sod/1.0/NetworkArm.xsd") ||
                                    systemId.equals("http://www.seis.sc.edu/xschema/sod/1.0/WaveFormArm.xsd") ||
                                    systemId.endsWith("sod.xsd") ||
                                    systemId.endsWith("utilities.xsd") ||
                                    systemId.endsWith("EventArm.xsd") ||
                                    systemId.endsWith("NetworkArm.xsd") ||
                                    systemId.endsWith("WaveFormArm.xsd")) {
                                    // return a special input source
                                    //You specify the schema location whereever your xsd/dtd files are located. But the systemId is the dtd that is mentioned
                                    //in the xml file/xml string
                                    String schemaFile = systemId.substring(systemId.lastIndexOf('/')+1);
                                    Class c = getClass();
                                    java.net.URL entityURL =
                                        c.getClassLoader().getResource("edu/sc/seis/sod/data/"+schemaFile);
                                    logger.debug("schema file is: "+schemaFile+" and URL is: "+entityURL);
                                    InputStream fis = entityURL.openStream();
                                    return new InputSource(fis);

                                } else {
                                    // use the default behaviour return null;
                                }
                            } catch (Exception e) {
                                logger.warn("Caught exception in EntityResolver",
                                            e);
                            }
                            return null;
                        }
                    });
            parser.setContentHandler(new SimpleContentHandler(parser));
            parser.parse(new InputSource(xmlFile));
            if (errorHandler.isFoundError()) {
                logger.fatal("Found error in validation of xml.");
                System.err.println("Found error in validation of xml.");
                System.exit(1);
            }
        } catch (org.xml.sax.SAXNotRecognizedException e) {
            throw new ConfigurationException("Can't configure parser", e);
        } catch (org.xml.sax.SAXNotSupportedException e) {
            throw new ConfigurationException("Can't configure parser", e);
        } catch (SAXException e) {
            throw new ConfigurationException(e.toString(), e);
        } catch (java.io.IOException e) {
            throw new ConfigurationException(e.toString(), e);
        } // end of try-catch
    }


    /**
     * Describe <code>initParser</code> method here.
     *
     * @param xmlFile an <code>InputStream</code> value
     * @return a <code>Document</code> value
     * @exception ParserConfigurationException if an error occurs
     * @exception org.xml.sax.SAXException if an error occurs
     * @exception java.io.IOException if an error occurs
     */
    protected Document initParser(InputStream xmlFile)
        throws ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);
        //factory.set
        factory.setNamespaceAware(true);
        //    factory.setAttribute("http://xml.org/sax/features/validation", "TRUE" );
        //    factory.setAttribute("http://apache.org/xml/features/validation/schema", "TRUE");

        DocumentBuilder docBuilder = factory.newDocumentBuilder();


        SimpleErrorHandler errorHandler = new SimpleErrorHandler();
        docBuilder.setErrorHandler(errorHandler);
        logger.info("Schema loc is: "+schemaURL.toString());
        Document document =  docBuilder.parse(xmlFile, schemaURL.toString());

        return document;

    }


    public void sodExceptionHandler(SodException sodException) {
        logger.fatal("Caught Exception in start becoz of the Listener",
                     sodException.getThrowable());


    }

    public static  void checkRestartOptions() {

        //get the quitTime
        //get refresh Time.
        //first check if the database alread exists.
        Start.REMOVE_DATABASE = isRemoveDatabase();
        Start.REFRESH_INTERVAL = getRefreshInterval();
        Start.RE_OPEN_EVENTS = isReOpenEvents();
        Start.GET_NEW_EVENTS = isGetNewEvents();
        Start.QUIT_TIME = getQuitTime();
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
                //nfe.printStackTrace();
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
        String str = props.getProperty("edu.sc.seis.sod.database.getNetEvents");
        if(str != null) {
            if(str.equalsIgnoreCase("true")) { return true;}
        }
        return false;
    }

    /** Default parser name. */
    private static final String
        DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

    public static boolean REMOVE_DATABASE = false;

    public static boolean GET_NEW_EVENTS = false;

    public static boolean RE_OPEN_EVENTS = false;

    public static int REFRESH_INTERVAL = 30;

    //later quitetime must be changed relatively
    public static int QUIT_TIME = 30; //quit time in terms of number of days;

    private static java.net.URL schemaURL;

    private static Properties props = null;

    InputStream configFile;

    static Document document;

    EventArm eventArm;

    private static Queue eventQueue; //= new HSqlDbQueue();

    private static WaveformQueue waveformQueue;

    NetworkArm networkArm;

    private WaveFormArm waveFormArm;

    static Logger logger = Logger.getLogger(Start.class);

    static Thread waveFormArmThread;

    static Thread eventArmThread;

}// Start

