package edu.sc.seis.sod;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;

/**
 * AbstractArea.java
 *
 *
 * Created: Thu Dec 13 17:05:33 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public abstract class AbstractArea implements Area{
    public AbstractArea(Element element) {

	this.element = element;	
   }
    public boolean accept(Station station , CookieJar cookies) {
	getArea(element);
	return true;
    }

    public abstract edu.iris.Fissures.Area getArea(Element element); 
    private Element element; 
}// AbstractArea
