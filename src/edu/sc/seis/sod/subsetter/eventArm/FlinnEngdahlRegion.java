package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;
import java.util.*;
import java.lang.*;
import org.w3c.dom.*;
import org.apache.log4j.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * FlinnEngdahlRegion.java
 *
 *
 * Created: Tue Mar 19 13:27:02 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public abstract class FlinnEngdahlRegion implements EventAttrSubsetter {
    public FlinnEngdahlRegion (Element config){

	String regionStr = SodUtil.getNestedText(config);
	StringTokenizer strtok = new StringTokenizer(regionStr, " ");
	ArrayList arrayList = new ArrayList();
	while(strtok.hasMoreTokens()) {
	    Integer newEntry = new Integer(Integer.parseInt(strtok.nextToken()));
	    arrayList.add(newEntry);
	}
	regions = new Integer[arrayList.size()];
	regions = (Integer[]) arrayList.toArray(regions);
    
    }

    public boolean accept(EventAttr e,  CookieJar cookies) {
	
	if(e.region.type.value() == getType().value()) {
	     for(int counter = 0; counter < regions.length; counter++) {
		System.out.println("The considered are type = "+getType().value()+" value "+regions[counter].intValue());
		if(e.region.number == regions[counter].intValue()) {
		    System.out.println("In FlinnEngdahlRegion returnign TRUE");
		    return true;
		}
	    }

	}
	System.out.println("The regiontype is "+e.region.type.value());
	System.out.println("The region value is "+e.region.number);
	System.out.println("In FlinnEngdhal Region returning FALSE");
	return false;
    }

    public abstract FlinnEngdahlType getType();

    private Integer[] regions = new Integer[0];

    static Category logger = 
        Category.getInstance(FlinnEngdahlRegion.class.getName());

}// FlinnEngdahlRegion
