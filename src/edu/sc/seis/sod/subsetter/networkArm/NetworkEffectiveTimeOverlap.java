package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.subsetter.*;
import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.log4j.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * NetworkEffectiveTimeOverlap.java
 *
 *
 * Created: Tue Mar 19 13:27:02 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public abstract class NetworkEffectiveTimeOverlap extends
EffectiveTimeOverlap implements NetworkAttrSubsetter {
    public NetworkEffectiveTimeOverlap (Element config){
	super(config);
	NodeList children = config.getChildNodes();
	Node node;
	for (int i=0; i<children.getLength(); i++) {
	    node = children.item(i);
	    logger.debug(node.getNodeName());
	}
    }

    public boolean accept(NetworkAttr e,  CookieJar cookies) {
	return true;
    }

    static Category logger = 
        Category.getInstance(NetworkEffectiveTimeOverlap.class.getName());

}// NetworkEffectiveTimeOverlap
