package edu.sc.seis.sod;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;

/**
 * BoxArea.java
 *
 *
 * Created: Thu Dec 13 17:05:33 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class BoxArea extends AbstractArea{

    public BoxArea(Element element) {

	super(element);
   } 
   
    public  edu.iris.Fissures.Area getArea(Element element) {
	System.out.println("The name of the Area tag is "+element.getTagName()); 
	return null;
   }
    
}// BoxArea
