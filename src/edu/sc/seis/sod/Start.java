package edu.sc.seis.sod;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.*;
import edu.sc.seis.fissuresUtil.cache.*;
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

public class Start {
    public Start (InputStream configFile) {
	this.configFile = configFile;
    }

    public void init() throws ParserConfigurationException, org.xml.sax.SAXException, IOException {
	document = initParser(configFile);
    }

    public void start() throws ConfigurationException {
	Element docElement = document.getDocumentElement();
	logger.info("start "+docElement.getTagName());
	System.out.println("In the method start the tagName is "+docElement.getTagName());
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
		    eventArm = new EventArm(subElement);
		} else if (subElement.getTagName().equals("networkArm")) {
		    logger.info(subElement.getTagName());
		    System.out.println("****** START OF NETWOTK ARM *********");
		    networkArm = new NetworkArm(subElement);
		} else if (subElement.getTagName().equals("waveFormArm")) {
		    logger.info(subElement.getTagName());
		    // waveFormArm = new WaveFormArm(subElement);
		    
		} else {
		logger.debug("process "+subElement.getTagName());
		    
		}
	    }
	}
    }

    public static EventQueue getEventQueue() {

	return eventQueue;

    }
    
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
	    if (filename == null) {
		filename = "edu/sc/seis/sod/data/DefaultConfig.xml";		 
	    } // end of if (filename == null)
	    
	    InputStream in;
	    if (filename.startsWith("http:") || filename.startsWith("ftp:")) {
		java.net.URL url = new java.net.URL(filename);
		java.net.URLConnection conn = url.openConnection();
		in = new BufferedInputStream(conn.getInputStream());
	    } else {
		in = new BufferedInputStream(new FileInputStream(filename));
	    } // end of else
	    

	    //n = new BufferedInputStream(new FileInputStream("/home/telukutl/sod/xml/network.xml"));
	    Start start = new Start(in);
            logger.info("Start init()");
	    start.init();
            logger.info("Start start()");
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

    protected Document initParser(InputStream xmlFile) 
	throws ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = factory.newDocumentBuilder();
	return docBuilder.parse(xmlFile);
    }

    InputStream configFile;

    Document document;

    EventArm eventArm;

    private static EventQueue eventQueue = new EventQueue();
    
    NetworkArm networkArm;

    private WaveFormArm waveFormArm;

    static Category logger = 
        Category.getInstance(Start.class.getName());

}// Start
