package edu.sc.seis.sod;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfSeismogramDC.*;
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

    public void start() {
	NodeList children = document.getChildNodes();
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
		    
		} else if (subElement.getTagName().equals("networkArm")) {
		    logger.info(subElement.getTagName());
		    
		} else if (subElement.getTagName().equals("waveformArm")) {
		    logger.info(subElement.getTagName());
		    
		} else {
		    
		}
	    }
	}
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
	} catch(Exception e) {
	    logger.error("Problem... ", e);
	}
    } // end of main ()

    protected Document initParser(InputStream xmlFile) 
	throws ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = factory.newDocumentBuilder();
	return docBuilder.parse(xmlFile);
    }

    InputStream configFile;

    Document document;

    static Category logger = 
        Category.getInstance(Start.class.getName());

}// Start
