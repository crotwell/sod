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
    public void init() throws ParserConfigurationException, org.xml.sax.SAXException, IOException {
	document = initParser(configFile);
    }

    /**
     * Describe <code>start</code> method here.
     *
     * @exception ConfigurationException if an error occurs
     */
    public void start() throws Exception {
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
		    System.out.println("Starting the EVent Arm");
		    logger.info(subElement.getTagName());
		    eventArm = new EventArm(subElement, this);
		    Thread eventArmThread = new Thread(eventArm);
		     eventArmThread.start();
			    System.out.println("******************* EVENT ARM THREAD JOINED SO CAN EXIT");
		} else if (subElement.getTagName().equals("networkArm")) {
		    logger.info(subElement.getTagName());
		    networkArm = new NetworkArm(subElement);
   		} else if (subElement.getTagName().equals("waveFormArm")) {
		    logger.info(subElement.getTagName());
		    waveFormArm = new WaveFormArm(subElement, networkArm, this);
		    Thread waveFormArmThread = new Thread(waveFormArm);
		    waveFormArmThread.start();
		 	   System.out.println("EXITRING AS THE WAEFORM ARM THREAD RETURNED");
		} else {
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

    public static void setProperties(Properties props) {
	
	props = props;

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

            for (int i=0; i<args.length-1; i++) {
                if (args[i].equals("-props")) {
                    // override with values in local directory, 
                    // but still load defaults with original name
                    propFilename = args[i+1];
		    commandlineProps = true;
                }
            }


	    //configure commonAccess
	    CommonAccess commonAccess = CommonAccess.getCommonAccess();
	    commonAccess.init(args);
	    commonAccess.initORB();
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


	    String filename 
		= props.getProperty("edu.sc.seis.sod.configuration");
	 System.out.println("The file name is "+filename);
	    if (filename == null) {
		logger.fatal("No configuration file given, quiting....");
		return;
	    } // end of if (filename == null)
	    
	    InputStream in;
	    if (filename.startsWith("http:") || filename.startsWith("ftp:")) {
		java.net.URL url = new java.net.URL(filename);
		java.net.URLConnection conn = url.openConnection();
		in = new BufferedInputStream(conn.getInputStream());
	    } else {
		in = new BufferedInputStream(new FileInputStream(filename));
	    } // end of else

	    if (in == null) {
		logger.fatal("Unable to load configuration file "+filename+", quiting...");
		return;
	    } // end of if (in == null)
	    

	    String schemaFilename = "edu/sc/seis/sod/data/";
	    schemaURL = 
		(Start.class).getClassLoader().getResource(schemaFilename);
	    logger.debug(schemaFilename+"->"+schemaURL.toString());
	    

	    //n = new BufferedInputStream(new FileInputStream("/home/telukutl/sod/xml/network.xml"));
	    Start start = new Start(in);
            logger.info("Start init()");
	    start.init();
            logger.info("Start start()");
	    setProperties(props);
	    start.start();
	} catch(Exception e) {
	    e.printStackTrace();
	    if (e instanceof WrappedException) {
	    logger.error("Problem, wrapped is ", ((WrappedException)e).getCausalException());
	    } // end of if (e instanceof WrappedException)
	    logger.error("Problem... ", e);
	}
	logger.info("Done.");
    } // end of main ()

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
	//factory.setValidating(false);
	//factory.set
//	factory.setNamespaceAware(true);

	DocumentBuilder docBuilder = factory.newDocumentBuilder();
	//SimpleErrorHandler errorHandler = new SimpleErrorHandler();
	//docBuilder.setErrorHandler(errorHandler);
	Document document =  docBuilder.parse(xmlFile, schemaURL.toString());
	/*(if(errorHandler.isValid()) return document;
	else {
	    logger.fatal("The xml Configuration file contains errors.");
	    System.out.println("The xml Configuration file contains errors.");
	    System.exit(0);
	    return null;
	}*/
	return document;
    }


    public void sodExceptionHandler(SodException sodException) {
	logger.fatal("Caught Exception in start becoz of the Listener", 
		     sodException.getException());
	System.exit(0);
	
    }


    private static java.net.URL schemaURL;
 
    private static Properties props = null;

    InputStream configFile;

    Document document;

    EventArm eventArm;

    private static Queue eventQueue = new HSqlDbQueue();
    
    NetworkArm networkArm;

    private WaveFormArm waveFormArm;

    static Category logger = 
        Category.getInstance(Start.class.getName());

}// Start
