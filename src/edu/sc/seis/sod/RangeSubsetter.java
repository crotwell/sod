package edu.sc.seis.sod;

import edu.sc.seis.sod.*;

import java.lang.*;
import org.w3c.dom.*;
/**
 * Describe class <code>RangeSubsetter</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class RangeSubsetter {

    /**
     * Creates a new <code>RangeSubsetter</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public RangeSubsetter(Element config) {
	
		NodeList children = config.getChildNodes();
		Node node;
		for(int i = 0; i < children.getLength() ; i++) {

			node = children.item(i);
			if(node instanceof Element)  {
			
				String tagName = ((Element)node).getTagName();
				if(tagName.equals("min")) minElement = ((Element)node);
				else if(tagName.equals("max")) maxElement = ((Element)node);
			}

		}

	}
    /**
     * Describe <code>getMinValue</code> method here.
     *
     * @return a <code>float</code> value
     */
    public float getMinValue() {
		String rtnValue = SodUtil.getNestedText(minElement);
		return  Float.parseFloat(rtnValue);
	}

    /**
     * Describe <code>getMaxValue</code> method here.
     *
     * @return a <code>float</code> value
     */
    public float getMaxValue() {
		String rtnValue = SodUtil.getNestedText(maxElement);	
		return Float.parseFloat(rtnValue);

	}

	private Element minElement = null;

	private Element maxElement = null;


}
