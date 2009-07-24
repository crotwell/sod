package edu.sc.seis.sod.subsetter;

import org.w3c.dom.Element;

import edu.sc.seis.sod.SodUtil;

public class MagType implements Subsetter {

    public MagType(Element config) {
        type = SodUtil.getNestedText(config);
    }

    public String getType() {
        return type;
    }

    private String type;
}// MagType
