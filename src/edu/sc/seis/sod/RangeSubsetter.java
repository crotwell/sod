package edu.sc.seis.sod;

import edu.sc.seis.sod.*;

import java.lang.*;
import org.w3c.dom.*;
public class RangeSubsetter {

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
	public float getMinValue() {
		String rtnValue = SodUtil.getNestedText(minElement);
		return  Float.parseFloat(rtnValue);
	}

	public float getMaxValue() {
		String rtnValue = SodUtil.getNestedText(maxElement);	
		return Float.parseFloat(rtnValue);

	}

	private Element minElement = null;

	private Element maxElement = null;


}
