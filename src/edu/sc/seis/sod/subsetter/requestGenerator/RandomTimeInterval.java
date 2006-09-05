package edu.sc.seis.sod.subsetter.requestGenerator;

import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;

public class RandomTimeInterval extends TimeInterval {

    public RandomTimeInterval(double min, double max, UnitImpl unit) {
        super(min, unit);
        this.min = min;
        this.max = max;
    }

    public double getValue() {
        double randomValue = Math.random() * (max - min) + min;
        System.out.println("random value returned: " + randomValue);
        return randomValue;
    }

    private double min, max;
}
