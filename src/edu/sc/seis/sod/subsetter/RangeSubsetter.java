package edu.sc.seis.sod.subsetter;
import edu.sc.seis.sod.*;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RangeSubsetter {

    public RangeSubsetter(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength() ; i++) {
           Node node = children.item(i);
            if(node instanceof Element)  {
                String tagName = ((Element)node).getTagName();
                if(tagName.equals("min")) minElement = ((Element)node);
                else if(tagName.equals("max")) maxElement = ((Element)node);
            }
        }
        if (getMinValue() > getMaxValue()) {
            throw new ConfigurationException("min > max: min="+getMinValue()+"  max="+getMaxValue());
        }
    }

    public float getMinValue() {
        if(minElement == null) return Float.MIN_VALUE;
        String rtnValue = SodUtil.getNestedText(minElement);
        return  Float.parseFloat(rtnValue);
    }

    public float getMaxValue() {
        if(maxElement == null) return Float.MAX_VALUE;
        String rtnValue = SodUtil.getNestedText(maxElement);
        return Float.parseFloat(rtnValue);

    }

    private Element minElement = null;

    private Element maxElement = null;
}
