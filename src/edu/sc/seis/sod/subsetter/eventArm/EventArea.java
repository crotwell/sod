package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * EventArea.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 *
 * This class is used to represent the subsetter EventArea. Event Area implements EventSubsetter
 * and can be any one of GlobalArea or BoxArea or PointDistanceArea or FlinneEngdahlArea.
 * 
 * sample xml representation of EventArea are
 *
 *
  *              &lt;stationArea&gt;
 *                           &lt;boxArea&gt;
 *                                    &lt;latitudeRange&gt;
 *                                                   &lt;min&gt;30&lt;/min&gt;
 *                                                   &lt;max&gt;33&lt;/max&gt;
 *                                    &lt;/latitudeRange&gt;
 *                                    &lt;longitudeRange&gt;
 *                                                   &lt;min&gt;-100&lt;/min&gt;
 *                                                   &lt;max&gt;100&lt;/max&gt;
 *                                    &lt;/longitudeRange&gt;
 *                           &lt;/boxArea&gt;
 *              &lt;/stationArea&gt;
 */


public class EventArea 
    implements OriginSubsetter,SodElement {
    
    public EventArea (Element config) throws ConfigurationException {
	NodeList children = config.getChildNodes();
	System.out.println("In EventArea ");
	for(int i = 0; i < children.getLength() ; i++) {
		Node node = children.item(i);
		if(node instanceof Element) {
			System.out.println("Now it is time to get the Area");
			area = (edu.iris.Fissures.Area)SodUtil.load((Element)node, "edu.sc.seis.sod");	
			break;
		}
	}
	
    }

    public boolean accept(EventAccessOperations event, Origin e,  CookieJar cookies) {
	System.out.println("now is the time to call accept on the area");	
	if(area instanceof edu.iris.Fissures.BoxArea) {
		edu.iris.Fissures.BoxArea boxArea = (edu.iris.Fissures.BoxArea)area;
		System.out.println("actual_latitude is "+e.my_location.latitude);
		System.out.println("actual_longitude is "+e.my_location.longitude);
		
		if(e.my_location.latitude >= boxArea.min_latitude 
		   && e.my_location.latitude <=boxArea.max_latitude
		   && e.my_location.longitude >= boxArea.min_longitude
		   && e.my_location.longitude <= boxArea.max_longitude) {
		    System.out.println("RETURNING TRUE");
		    return true;
		} else return false;
	
	}
	else System.out.println("The area is null");
	return true;
	
    }

    private edu.iris.Fissures.Area area = null;
}// EventArea
