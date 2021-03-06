package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.sod.SodUtil;


class RSDistanceRange {

    public RSDistanceRange(Element el) {
        minDistance = new Double(SodUtil.getText(SodUtil.getElement(el,
                                                                    "min"))).doubleValue();
        maxDistance = new Double(SodUtil.getText(SodUtil.getElement(el,
                                                                    "max"))).doubleValue();
    }

    public RSDistanceRange(double minDistance, double maxDistance) {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    private double minDistance;

    private double maxDistance;
}