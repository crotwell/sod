package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;
import org.apache.log4j.*;

/**
 * EventFinder.java
 *
 *
 * Created: Tue Mar 19 12:49:48 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EventFinder implements SodElement {
    public EventFinder (Element config){
	processConfig(config);
    }
    
    protected void processConfig(Element config) {
	
    }

    static Category logger = 
        Category.getInstance(EventFinder.class.getName());

}// EventFinder
