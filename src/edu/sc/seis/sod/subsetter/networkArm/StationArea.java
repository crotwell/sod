package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * StationArea.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 *
 * This class is used to represent the subsetter StationArea. Station Area implements StationSubsetter
 * and can be any one of GlobalArea or BoxArea or PointDistanceArea or FlinneEngdahlArea.
 * 
 * sample xml representation of StationArea are
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


public class StationArea 
    implements StationSubsetter,SodElement {
    
    /**
     * Creates a new <code>StationArea</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public StationArea (Element config) throws ConfigurationException {
	NodeList children = config.getChildNodes();
	System.out.println("In StationArea ");
	for(int i = 0; i < children.getLength() ; i++) {
		Node node = children.item(i);
		if(node instanceof Element) {
			System.out.println("Now it is time to get the Area");
			area = (edu.iris.Fissures.Area)SodUtil.load((Element)node, "edu.sc.seis.sod");	
			break;
		}
	}
	
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param e a <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAccess network, Station e,  CookieJar cookies) {
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
	
	} else if(area instanceof GlobalArea) return true;
	else System.out.println("The area is null");
	return true;
	
    }

    private edu.iris.Fissures.Area area = null;
}// StationArea
