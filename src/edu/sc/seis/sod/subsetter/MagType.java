package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.Subsetter;
import org.w3c.dom.Element;

public class MagType implements Subsetter{
    public MagType (Element config){ this.config = config; }
    
    public String getType() { return SodUtil.getNestedText(config); }

    private Element config;
}// MagType
