package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;

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
    public EventAttrAND (Element config){
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

    protected void processConfig(Element config) {
	NodeList children = config.getChildNodes();
	Node node;
	Class[] constructorArgs = new Class[1];
	constructorArgs[0] = Element.class;

	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    if (node instanceof Element) {
		try {
		    Element subElement = (Element)node;
		    Class eventAttrSubclass = 
			Class.forName("edu.sc.seis.sod.subsetter."+
				      subElement.getTagName());

		    Object obj = eventAttrSubclass.getInstance();
		} catch (ConfigurationException e) {
		    // don't repackage ConfigurationException
		    throw e;
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
