package edu.sc.seis.sod.subsetter.requestGenerator;

import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;

public class RandomTimeInterval extends TimeInterval {

    public RandomTimeInterval(double min, double max, UnitImpl unit) {
        super(min, unit);
        this.min = min;
        this.max = max;
    }

    public double getValue() {
        return Math.random() * (max - min) + min;
    }

    private double min, max;
}
