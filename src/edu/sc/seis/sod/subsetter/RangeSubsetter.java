package edu.sc.seis.sod.subsetter;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.sc.seis.sod.SodUtil;

public class RangeSubsetter {
    public RangeSubsetter(){}

    public RangeSubsetter(Element config) {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i) instanceof Element) {
                Element el = (Element)children.item(i);
                String tagName = el.getTagName();
                if(tagName.equals("min")) {
                    min = extractValue(el);
                } else if(tagName.equals("max")) {
                    max = extractValue(el);
                }
            }
        }
    }

    private static float extractValue(Element e) {
        return Float.parseFloat(SodUtil.getNestedText(e));
    }

    public float getMinValue() {
        return min;
    }

    public float getMaxValue() {
        return max;
    }

    float min = -1f * (Float.MAX_VALUE - 1);

    float max = Float.MAX_VALUE;
}