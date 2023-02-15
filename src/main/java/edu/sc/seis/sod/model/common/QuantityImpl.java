
package edu.sc.seis.sod.model.common;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;


/**
 * QuantityImpl.java
 *
 *
 * Created: Wed Sep  1 17:43:50 1999
 *
 * @author Philip Crotwell
 * @version
 */
public class QuantityImpl implements Serializable {
    
    private double value;
    private UnitImpl the_units;
    
    protected QuantityImpl() {}

    public static Serializable createEmpty() {
        return new QuantityImpl();
    }

    public QuantityImpl(double f, UnitImpl the_unit ) {
        value = f;
        the_units =  the_unit;
    }

    public static QuantityImpl createQuantityImpl(QuantityImpl q) {
        if (q instanceof QuantityImpl) {
            return (QuantityImpl)q;
        }
        return new QuantityImpl(q.value,
                                UnitImpl.createUnitImpl(q.the_units));
    }
    
    public static QuantityImpl of(Duration duration) {
        return new QuantityImpl(duration.toNanos(), UnitImpl.NANOSECOND);
    }
    
    @Deprecated
    public void setFormat(NumberFormat format) {
        this.format = format;
    }

    public String formatValue(String format) {
        return new DecimalFormat(format).format(value);
    }

    public double getValue() {
        return value;
    }
    
    public double getValue(UnitImpl unit){
        return convertTo(unit).getValue();
    }
    
    protected void setValue(double value) {
        this.value = value;
    }

    public double get_value() {
        return getValue();
    }

    public UnitImpl getUnit() {
        return (UnitImpl)the_units;
    }
    
    protected void setUnit(UnitImpl unit) {
        this.the_units = unit;
    }

    public UnitImpl get_unit() {
        return getUnit();
    }
    
    /** replaces current unit with new one that should be equals(). Used to 
     * prevent many copies of effectively the same units from wasting memory. 
     * Should be called like:
        QuantityImpl.internUnit(q, intern(q.getUnit()));
     * @param q
     * @param internedUnit
     */
    public static void internUnit(QuantityImpl q, UnitImpl internedUnit) {
        q.the_units = internedUnit;
    }

    /** converts this Quantity into the given units.
     *  @return a new quantity with the given units and its value
     *     correspondingly adjusted.
     *  @throws IllegalArgumentException if the given units are
     *     not compatible.
     */
    public QuantityImpl convertTo(UnitImpl newUnit) {
        UnitImpl currUnit = getUnit();
        if (currUnit.equals(newUnit)) {
            return this;
        } else if ( ! currUnit.isConvertableTo(newUnit)) {
            throw new IllegalArgumentException("Cannot convert, units are not compatible, "+
                          currUnit+" and "+newUnit);
        } else  {
            // only worry about Celcius and Fahrenheit if they are base units,
            // otherwise they are relative, ie deg C per meter, and so the dc
            // offset doesn't matter
            if (currUnit.the_unit_base == UnitBase.KELVIN) {
                if (currUnit.name.equalsIgnoreCase("KELVIN")) {
                    if (newUnit.name.equalsIgnoreCase(UnitImpl.CELSIUS_NAME)) {
                        return new QuantityImpl(getValue()-273.15, newUnit);
                    } else if (newUnit.name.equalsIgnoreCase(UnitImpl.FAHRENHEIT_NAME)) {
                        return new QuantityImpl((getValue()-273.15)*9.0/5.0+32, newUnit);
                    }
                } else if (currUnit.name.equalsIgnoreCase(UnitImpl.CELSIUS_NAME)) {
                    if (newUnit.name.equalsIgnoreCase(UnitImpl.KELVIN_NAME)) {
                        return new QuantityImpl(getValue()+273.15, newUnit);
                    } else if (newUnit.name.equalsIgnoreCase(UnitImpl.FAHRENHEIT_NAME)) {
                        return new QuantityImpl((getValue())*9.0/5.0+32, newUnit);
                    }
                } else if (currUnit.name.equalsIgnoreCase(UnitImpl.FAHRENHEIT_NAME)) {
                    if (newUnit.name.equalsIgnoreCase(UnitImpl.CELSIUS_NAME)) {
                        return new QuantityImpl((getValue()-32)*5.0/9.0, newUnit);
                    } else if (newUnit.name.equalsIgnoreCase(UnitImpl.KELVIN_NAME)) {
                        return new QuantityImpl((getValue()-32)*5.0/9.0 + 273.15, newUnit);
                    }
                }
            }
            double mulfac = currUnit.getTotalMultiFactor() /
                newUnit.getTotalMultiFactor();
            int powerDiff = currUnit.getTotalPower() -
                newUnit.getTotalPower();
            double newValue = getValue() *mulfac;
            if (powerDiff != 0) {
                newValue *= Math.pow(10, powerDiff);
            }
            return new QuantityImpl(newValue, newUnit);
        }
    }

    public QuantityImpl add(QuantityImpl q) {
        double val = q.convertTo(getUnit()).getValue();
        return new QuantityImpl(getValue() + val, getUnit());
    }

    public QuantityImpl subtract(QuantityImpl q) {
        double val = q.convertTo(getUnit()).getValue();
        return new QuantityImpl(getValue() - val, getUnit());
    }

    public QuantityImpl multiplyBy(QuantityImpl q) {
        double val = q.getValue();
        return new QuantityImpl(getValue() * val,
                                UnitImpl.multiply(getUnit(), q.getUnit()));
    }

    public QuantityImpl divideBy(QuantityImpl q) {
        double val = q.getValue();
        return new QuantityImpl(getValue() / val,
                                UnitImpl.divide(getUnit(), q.getUnit()));
    }

    public QuantityImpl multipliedByDbl(double f) {
        return new QuantityImpl(getValue() * f,
                                getUnit());
    }

    public QuantityImpl dividedByDbl(double f) {
        return new QuantityImpl(getValue() / f,
                                getUnit());
    }

    public QuantityImpl abs() {
        return new QuantityImpl(Math.abs(getValue()),
                                getUnit());
    }

    public QuantityImpl inverse() {
        return new QuantityImpl(1.0 / getValue(),
                                getUnit().inverse());
    }

    public String toString() {
        if (format == null) {
            return getValue()+" "+getUnit();
        } else {
            return format.format(getValue())+" "+getUnit();
        }
    }
    
    public Duration toDuration() {
        return Duration.ofNanos(Math.round(getValue(UnitImpl.NANOSECOND)));
    }

    public boolean greaterThan(QuantityImpl q) {
        double val = q.convertTo(getUnit()).getValue();
        return (getValue() > val);
    }

    public boolean greaterThanEqual(QuantityImpl q) {
        double val = q.convertTo(getUnit()).getValue();
        return (getValue() >= val);
    }

    public boolean lessThan(QuantityImpl q) {
        double val = q.convertTo(getUnit()).getValue();
        return (getValue() < val);
    }

    public boolean lessThanEqual(QuantityImpl q) {
        double val = q.convertTo(getUnit()).getValue();
        return (getValue() <= val);
    }
    public int hashCode(){
        int result = 22;
        long valBits = Double.doubleToLongBits(value);
        result = 37*result + (int)(valBits^(valBits>>>32));
        result = 37*result + the_units.hashCode();
        return result;
    }

    public boolean equals(Object o) {
        if (super.equals(o)) return true;
        if (o instanceof QuantityImpl) {
            QuantityImpl q = (QuantityImpl)o;
            if (getUnit().isConvertableTo(q.getUnit()))
                if(q.convertTo(getUnit()).getValue() == getValue())
                    return true;
        }
        return false;
    }

    protected NumberFormat format = null;

    public static void main(String[] args) {
        // testing
        QuantityImpl q = new QuantityImpl(5, UnitImpl.HOUR);
        QuantityImpl halfq = new QuantityImpl(2.5, UnitImpl.HOUR);
        System.out.println("q="+q);
        System.out.println("q.convertTo(UnitImpl.SECOND)="+q.convertTo(UnitImpl.SECOND));
        System.out.println("q.inverse="+q.inverse());
        System.out.println("q.add(q)="+q.add(q));
        System.out.println("q.subtract(halfq)="+q.subtract(halfq));

        System.out.println("q.convertTo(MICROSECOND)="+q.convertTo(UnitImpl.MICROSECOND));
        QuantityImpl p = new QuantityImpl(10, UnitImpl.METER);
        System.out.println("p="+p);
        System.out.println("p.divideBy(q)="+p.divideBy(q));
        System.out.println("p.multiplyBy(q)="+p.multiplyBy(q));
    }

} // QuantityImpl
