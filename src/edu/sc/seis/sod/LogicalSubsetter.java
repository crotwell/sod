package edu.sc.seis.sod;

import java.util.*;
import org.w3c.dom.*;

/**
 * An abstract superclass for all of the logical operations, AND,
 * OR, NOT, XOR, for all of the types of subsetter. Manages the configuration
 * and storage of the subelements.
 *
 *
 * Created: Tue Mar 19 14:37:49 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public abstract class LogicalSubsetter implements Subsetter {
    public LogicalSubsetter (Element config) throws ConfigurationException {
	processConfig(config);
    }
    
    protected void processConfig(Element config) throws ConfigurationException{
	NodeList children = config.getChildNodes();
	Node node;
	Class[] constructorArgTypes = new Class[1];
	constructorArgTypes[0] = Element.class;

	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    if (node instanceof Element) {
		Element subElement = (Element)node;
		Object obj = SodUtil.load(subElement, getPackageName());
		if (obj instanceof Subsetter) {
		    filterList.add((Subsetter)obj);
		} // end of if (sodElement instanceof EventAttrSubsetter)
		
	    } // end of if (node instanceof Element)
	    
	} // end of for (int i=0; i<children.getSize(); i++)
	
    }

    public abstract String getPackageName();

    protected List filterList = new LinkedList();

}// LogicalSubsetter
