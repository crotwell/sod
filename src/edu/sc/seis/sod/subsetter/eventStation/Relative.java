package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.DistanceRangeSubsetter;

public class Relative extends PhaseInteractionType {

    public Relative(Element config) throws ConfigurationException {
        super(config);
        this.config = config;
    }

    public void processConfig() throws ConfigurationException {
        Element element;
        element = SodUtil.getElement(config, "reference");
        if(element != null) reference = SodUtil.getNestedText(element);
        element = SodUtil.getElement(config, "depthRange");
        if(element != null) depthRange = (edu.sc.seis.sod.subsetter.DepthRange)SodUtil.load(element,
                                                                                            "");
        element = SodUtil.getElement(config, "distanceRange");
        if(element != null) distanceRange = (DistanceRangeSubsetter)SodUtil.load(element,
                                                                                 "");
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
