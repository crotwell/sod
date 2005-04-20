package edu.sc.seis.sod.subsetter;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.sc.seis.sod.SodUtil;

public class RangeSubsetter {

    public RangeSubsetter() {}

    public RangeSubsetter(Element config) {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i) instanceof Element) {
                Element el = (Element)children.item(i);
                String tagName = el.getTagName();
                if(tagName.equals("min") || tagName.equals("greaterThanEquals")) {
                    min = extractValue(el);
                } else if(tagName.equals("max")
                        || tagName.equals("lessThanEquals")) {
                    max = extractValue(el);
                } else if(tagName.equals("greaterThan")) {
                    min = extractValue(el);
                    allowEqualsToMin = false;
                } else if(tagName.equals("lessThan")) {
                    max = extractValue(el);
                    allowEqualsToMax = false;
                }
            }
        }
    }

    public boolean accept(double value) {
        return acceptMin(value) && acceptMax(value);
    }

    public boolean acceptMin(double value) {
        return min < value || (allowEqualsToMin && min == value);
    }

    public boolean acceptMax(double value) {
        return max > value || (allowEqualsToMax && max == value);
    }

    boolean allowEqualsToMin = true, allowEqualsToMax = true;

    private static double extractValue(Element e) {
        return Double.parseDouble(SodUtil.getNestedText(e));
    }

    public double getMinValue() {
        return min;
    }

    public double getMaxValue() {
        return max;
    }

    protected double min = -1f * (Double.MAX_VALUE - 1);

    protected double max = Double.MAX_VALUE;
}