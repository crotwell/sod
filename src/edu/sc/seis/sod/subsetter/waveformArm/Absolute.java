package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;
import edu.sc.seis.TauP.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;

import java.util.*;
import org.w3c.dom.*;

/**
 * sample xml
 *<pre>
 * &lt;absolute&gt;
 *      &lt;boxArea&gt;
 *              &lt;latitudeRange&gt;
 *                      &lt;min&gt;30&lt;/min&gt;
 *                      &lt;max&gt;33&lt;/max&gt;
 *              &lt;/latitudeRange&gt;
 *              &lt;longitudeRange&gt;
 *                      &lt;min&gt;-100&lt;/min&gt;
 *                      &lt;max&gt;100&lt;/max&gt;
 *              &lt;/longitudeRange&gt;
 *      &lt;/boxArea&gt;
 *	&lt;depthRange&gt;
 *	        &lt;unitRange&gt;
 *			&lt;unit&gt;KILOMETER&lt;/unit&gt;
 *			&lt;min&gt;-1000&lt;/min&gt;
 *			&lt;max&gt;1000&lt;/max&gt;
 *		&lt;/unitRange&gt;
 *	&lt;/depthRange&gt;
 * &lt;/absolute&gt; 
 *
 *                      (or)
 * 
 *
  * &lt;absolute&gt;
 *      &lt;boxArea&gt;
 *              &lt;latitudeRange&gt;
 *                      &lt;min&gt;30&lt;/min&gt;
 *                      &lt;max&gt;33&lt;/max&gt;
 *              &lt;/latitudeRange&gt;
 *              &lt;longitudeRange&gt;
 *                      &lt;min&gt;-100&lt;/min&gt;
 *                      &lt;max&gt;100&lt;/max&gt;
 *              &lt;/longitudeRange&gt;
 *      &lt;/boxArea&gt;
 * &lt;/absolute&gt;
 *
 *                      (or)
 *
 * &lt;absolute&gt;
 *	&lt;depthRange&gt;
 *	        &lt;unitRange&gt;
 *			&lt;unit&gt;KILOMETER&lt;/unit&gt;
 *			&lt;min&gt;-1000&lt;/min&gt;
 *			&lt;max&gt;1000&lt;/max&gt;
 *		&lt;/unitRange&gt;
 *	&lt;/depthRange&gt;
 * &lt;/absolute&gt;
 *</pre>
 */

public class Absolute extends PhaseInteractionType {

	public Absolute(Element config) throws ConfigurationException{
		super(config);	
		this.config = config;
	}

	public void processConfig() throws ConfigurationException{
		NodeList nodeList = config.getChildNodes();
		Node node;
		for(int counter = 0; counter < nodeList.getLength(); counter++) {
			node = nodeList.item(counter);
			if(node instanceof Element) {
				Object obj = SodUtil.load((Element)node, "edu.sc.seis.sod.subsetter");
				if(obj instanceof edu.iris.Fissures.Area) area = (edu.iris.Fissures.Area)obj;
				else if(obj instanceof edu.sc.seis.sod.subsetter.DepthRange) depthRange = (edu.sc.seis.sod.subsetter.DepthRange)obj;
			}

		}

	}

	public edu.iris.Fissures.Area getArea() { 

		return this.area;
	}

	public edu.sc.seis.sod.subsetter.DepthRange getDepthRange() {

		return this.depthRange;

	}

	private edu.iris.Fissures.Area area = null;

	private edu.sc.seis.sod.subsetter.DepthRange depthRange = null;

	private Element config;
}//Absolute
