package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;
import java.lang.reflect.*;

/**
 * EventAttrAND.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EventAttrAND implements EventAttrSubsetter {
    public EventAttrAND (Element config) throws ConfigurationException {
	processConfig(config);
    }
    
    public void add(EventAttrSubsetter eventSubsetter) {
	filterList.add(eventSubsetter);
    }

    public boolean accept(EventAttr e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	while (it.hasNext()) {
	    EventAttrSubsetter filter = (EventAttrSubsetter)it.next();
	    if ( ! filter.accept(e, cookies)) { //changed from event to e
		return false;
	    } // end of if (! filter.accept(event))
	} // end of while (it.hasNext())
	return true;
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
		try {
		    Class eventAttrSubclass = 
			Class.forName("edu.sc.seis.sod.subsetter."+
				      subElement.getTagName());
		    Constructor constructor = 
			eventAttrSubclass.getConstructor(constructorArgTypes);
		    Object[] constructorArgs = new Object[1];
		    constructorArgs[0] = subElement;
		    Object obj = 
			constructor.newInstance(constructorArgs);
		    add((EventAttrSubsetter)obj);
		} catch (InvocationTargetException e) {
		    // occurs if the constructor throws an exception
		    // don't repackage ConfigurationException
		    Throwable subException = e.getTargetException();
		    if (subException instanceof ConfigurationException) {
			throw (ConfigurationException)subException;
		    } else if (subException instanceof Exception) {
			throw new ConfigurationException("Problem creating "+
						      subElement.getTagName()+
						      " within "+
						      config.getTagName(), 
						      (Exception)subException);
		    } else {
			// not an Exception, so must be an Error
			throw (java.lang.Error)subException;
		    } // end of else
		} catch (Exception e) {
		    throw new ConfigurationException("Problem understanding "+
						     subElement.getTagName()+
						     " within "+
						     config.getTagName(), e);
		} // end of try-catch
		
	    } // end of if (node instanceof Element)
	    
	} // end of for (int i=0; i<children.getSize(); i++)
	
    }

    List filterList = new LinkedList();

}// EventAttrAND
