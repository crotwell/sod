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
 */

public class StationArea 
    implements StationSubsetter,SodElement {
    
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

    public boolean accept(Station e,  CookieJar cookies) {
	System.out.println("now is the time to call accept on the area");	
	if(area instanceof edu.iris.Fissures.BoxArea) {
		edu.iris.Fissures.BoxArea boxArea = (edu.iris.Fissures.BoxArea)area;
		System.out.println("The min_latitude is "+boxArea.min_latitude);
		System.out.println("The max_latitude is "+boxArea.max_latitude);
		System.out.println("The min_longitude is "+boxArea.min_longitude);
		System.out.println("The max_longitude is "+boxArea.max_longitude);
		System.out.println("Area is of type BoxArea");
	}
	else System.out.println("The area is null");
	return true;
    }

    private edu.iris.Fissures.Area area = null;
}// StationArea
