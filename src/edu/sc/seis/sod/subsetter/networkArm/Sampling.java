package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * sample xml file<br>
 * <body>
 * <pre>
 * <bold>
 * 	&lt;sampling&gt;
 *		&lt;min&gt;1&lt;/min&gt;
 *		&lt;max&gt;40&lt;/max&gt;
 *		&lt;interval&gt;
 *			&lt;unit&gt;SECOND&lt;/unit&gt;
 *			&lt;value&gt;1&lt;/value&gt;
 *		&lt;/interval&gt;
 *	&lt;/sampling&gt;
 * </bold></pre></body>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class Sampling extends RangeSubsetter implements ChannelSubsetter {

    /**
     * Creates a new <code>Sampling</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public Sampling(Element config) {

		super(config);
		NodeList children  = config.getChildNodes();
		Node node;

		for(int i = 0; i < children.getLength(); i ++) {

			node = children.item(i);
			if(node instanceof Element) {
				
				String tagName = ((Element)node).getTagName();
				if(tagName.equals("interval"))  {
				     try {  
					interval = (Interval)SodUtil.load((Element)node, "edu.sc.seis.sod.subsetter");
				     } catch(Exception e) {e.printStackTrace();}
				}

			}

		}
		if(interval == null) System.out.println("The interval is null");	
	
	}

    /**
     * Describe <code>accept</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAccess network,Channel channel, CookieJar cookies) {

		System.out.println("The min Value is "+getMinValue());
		System.out.println("The max Value is "+getMaxValue());
		System.out.println("The unit of the interval is "+interval.getUnit());
		System.out.println("The value of the interval is "+interval.getValue());	
		return true;
	}

	Interval interval = null;
}//Sampling
