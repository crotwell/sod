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
 * &lt;relative&gt;
 *	&lt;reference&gt;EVENT&lt;/reference&gt;
 *	&lt;depthRange&gt;
 *	        &lt;unitRange&gt;
 *			&lt;unit&gt;KILOMETER&lt;/unit&gt;
 *			&lt;min&gt;-1000&lt;/min&gt;
 *			&lt;max&gt;1000&lt;/max&gt;
 *		&lt;/unitRange&gt;
 *	&lt;/depthRange&gt;
 *	&lt;distanceRange&gt;
 *		&lt;unit&gt;DEGREE&lt;/unit&gt;
 *        	&lt;min&gt;60&lt;/min&gt;
 *		&lt;max&gt;70&lt;/max&gt;
 *	&lt;/distanceRange&gt;
 * &lt;/relative&gt;
 *  
 *                       (or)
 *
 * &lt;relative&gt;
 *	&lt;reference&gt;EVENT&lt;/reference&gt;
 *	&lt;distanceRange&gt;
 *		&lt;unit&gt;DEGREE&lt;/unit&gt;
 *        	&lt;min&gt;60&lt;/min&gt;
 *		&lt;max&gt;70&lt;/max&gt;
 *	&lt;/distanceRange&gt;
 * &lt;/relative&gt;
 *
 *                      (or)
 *
 * &lt;relative&gt;
 *	&lt;reference&gt;EVENT&lt;/reference&gt;
 *	&lt;depthRange&gt;
 *	        &lt;unitRange&gt;
 *			&lt;unit&gt;KILOMETER&lt;/unit&gt;
 *			&lt;min&gt;-1000&lt;/min&gt;
 *			&lt;max&gt;1000&lt;/max&gt;
 *		&lt;/unitRange&gt;
 *	&lt;/depthRange&gt;
 * &lt;/relative&gt; 
 *</pre>
 */

public class Relative extends PhaseInteractionType {

	public Relative(Element config) throws ConfigurationException{
		super(config);
		this.config = config;
	}

	public void processConfig() throws ConfigurationException{
		/*NodeList nodeList = config.getChildNodes();
		Node node;

		for(int counter = 0; counter < nodeList.getLength(); counter++) {

			node = nodeList.item(counter);
			if(node instanceof Element) {


			}
		}*/
		Element element;
		element = SodUtil.getElement(config, "reference");
		if(element != null) reference = SodUtil.getNestedText(element);
		element = SodUtil.getElement(config, "depthRange");
		if(element != null) depthRange = (edu.sc.seis.sod.subsetter.DepthRange) SodUtil.load(element, "edu.sc.seis.sod.subsetter");
		element = SodUtil.getElement(config, "distanceRange");
		if(element != null) distanceRange = (DistanceRangeSubsetter) SodUtil.load(element, "edu.sc.seis.sod.subsetter");
	}

	public String getReference() {

		return this.reference;
	}
	
	public edu.sc.seis.sod.subsetter.DepthRange getDepthRange() {
		return this.depthRange;
	}

	public DistanceRangeSubsetter getDistanceRange() {
		return this.distanceRange;
	}
	private String reference;

	private DistanceRangeSubsetter distanceRange = null;

	private edu.sc.seis.sod.subsetter.DepthRange depthRange = null;

	private Element config;

}//Relative
