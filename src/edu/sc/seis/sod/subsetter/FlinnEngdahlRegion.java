package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import java.util.*;
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
	NodeList children = config.getChildNodes();
	Node node;
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    logger.debug(node.getNodeName());
	}
    }

    public boolean accept(EventAttr e,  CookieJar cookies) {
	return true;
    }

    static Category logger = 
        Category.getInstance(FlinnEngdahlRegion.class.getName());

}// FlinnEngdahlRegion
