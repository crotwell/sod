package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.log4j.*;
import edu.iris.Fissures.*;

/**
 * EffectiveTimeOverlap.java
 *
 *
 * Created: Tue Mar 19 13:27:02 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public abstract class EffectiveTimeOverlap { 
    public EffectiveTimeOverlap (Element config){
	NodeList children = config.getChildNodes();
	Node node;
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    logger.debug(node.getNodeName());
	}
    }

    public edu.iris.Fissures.Time getMinEffectiveTime() {

	return null;

    }

    public edu.iris.Fissures.Time getMaxEffectiveTime() {

	return null;

    }

    static Category logger = 
        Category.getInstance(EffectiveTimeOverlap.class.getName());

}// EffectiveTimeOverlap
