
package edu.sc.seis.sod.model.common;

import java.io.Serializable;


/**
 * UnitRangeImpl.java
 *
 *
 * Created: Mon Sep  6 16:04:23 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class UnitRangeImpl implements Serializable {

    public UnitImpl the_units;

    public double min_value;

    public double max_value;
    
    protected UnitRangeImpl() {}

    public static Serializable createEmpty() { return new UnitRangeImpl(); }

    public UnitRangeImpl(double min, double max, UnitImpl the_units) {
        if (the_units==null) {
            throw new IllegalArgumentException("Unit must not be null.");
        }
        if (min > max) {
            throw new IllegalArgumentException("min > max");
        }
        this.min_value = min;
        this.max_value = max;
        this.the_units = the_units;
    }

    public double getMinValue() { return min_value; }

    public double getMaxValue() { return max_value; }

    public UnitImpl getUnit() { return (UnitImpl)the_units; }

    /** converts this UnitRange into the given units.
     *  @return a new quantity with the given units and its value
     *     correspondingly adjusted.
     *  @throws IllegalArgumentException if the given units are
     *     not compatible.
     */
    public UnitRangeImpl convertTo(UnitImpl newUnit) {
        UnitImpl currUnit = getUnit();

        if (newUnit.equals(currUnit)) {
            return this;
        } else if ( ! currUnit.isConvertableTo(newUnit)) {
            throw new IllegalArgumentException("Cannot convert, units are not compatible");
        }

        double mulfac = currUnit.getTotalMultiFactor() /
            newUnit.getTotalMultiFactor();
        int powerDiff = currUnit.getTotalPower() -
            newUnit.getTotalPower();
        double newMinValue =
            getMinValue() * mulfac * Math.pow(10, powerDiff);
        double newMaxValue =
            getMaxValue() * mulfac * Math.pow(10, powerDiff);
        return new UnitRangeImpl(newMinValue, newMaxValue, newUnit);
    }

    public String toString() {
        return "("+getMinValue()+","+getMaxValue()+") "+getUnit();
    }

    public boolean equals(Object o){
        if(o == this){ return true; }
        if(o instanceof UnitRangeImpl){
            UnitRangeImpl oRange = (UnitRangeImpl)o;
            return oRange.min_value == min_value && oRange.max_value == max_value &&
                the_units.equals(oRange.the_units);
        }
        return false;
    }

    public int hashCode(){
        int result = 123;
        long minBits = Double.doubleToLongBits(min_value);
        result = (int)(minBits^(minBits>>>32)) + 37 * result;
        long maxBits = Double.doubleToLongBits(max_value);
        result = (int)(maxBits^(maxBits>>>32)) + 37 * result;
        return getUnit().hashCode() + result * 37;
    }
} // UnitRangeImpl
