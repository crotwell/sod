
package edu.sc.seis.sod.model.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



/** encapsulates the meaning of a unit. All units are products of SI
 *  primitive units.<p>
 *  It has 4 main parts and can be viewed as<br>
 *  (m * 10^p u) ^ e<br>
 *  where:<br>
 *  m is the multiplicative factor,<br>
 *  p is the power of ten,
 *  u is either a SI base unit, or a composite unit,
 *  e is the exponent.<P>
 *  For example, the unit <em>per inch</em> might be represented as<br>
 *  (2.54 * 10^0 (1.0 * 10^-2 METER) ) ^ -1<br>
 *  as one inch is 2.54 centimeters, which is 10-2 METERS.
 *  The -1 takes care of the <em>per</em> part.
 *
 */
public class UnitImpl implements Serializable {

    /** The base, if a base unit, or COMPOSITE in not. */
    public UnitBase the_unit_base;

    /** The subunits if COMPOSITE, zero length array if not. */

    public UnitImpl[] elements;

    /** The power of ten to prefix. */

    public int power;

    /** A name for the unit, may be empty if the name should be
     *automatically generated. */
    public String name;

    /** A constant multiplier. */
    public double multi_factor;

    /** An exponent for the entire unit, ie Hertz would be SECOND with
     *an exponent of -1. */

    public int exponent;
    
    /** The CORBA struct that contains the unit data. It is composed of
     <p>
     the_unit_base - the base unit, if this is of type BASE, or COMPOSITE if not
     <br>
     elements - the elements that make up this composite unit,
     if this is of type COMPOSITE<br>
     power - the power of ten for this unit, for example 3 for KILO<br>
     name - a string name for the unit, for example millisecond or foot<br>
     multi_factor - multiplicative factor, for example 2.54 together
     with centimeters would give inches<br>
     exponent - the exponent of the unit, for example -1 for per second
     */
    protected static int numPrimitives = UnitImpl.getNumPrimitives();

    protected UnitImpl() {}

    public static java.io.Serializable createEmpty() {
        return new UnitImpl();
    }

    protected UnitImpl(int power, String name, double multi, int exponent) {
        this.power = power;
        this.name = name;
        this.multi_factor = multi;
        this.exponent = exponent;
    }

    /** creates a simple unit, ie only one type of base unit to a exponent
     *  with a power for the power of ten. */
    public UnitImpl(UnitBase baseUnit, int exponent, int power) {
        this(power, "", 1.0f, exponent);
        the_unit_base = baseUnit;
        elements = new UnitImpl[0];
    }

    public UnitImpl(UnitImpl[] subunits, int power, String name, double multi,
                    int exponent) {
        this(power, name, multi, exponent);
        if (subunits == null) {
            throw new IllegalArgumentException("Cannot create composite Unit with null subunits.");
        }
        if (subunits.length == 0) {
            throw new IllegalArgumentException("Cannot create composite Unit with 0 subunits.");
        }
        the_unit_base = UnitBase.COMPOSITE;
        elements = subunits;
    }

    /** creates a simple unit, ie only one type of base unit to a exponent
     *  with a power for the power of ten and with a multiplicative factor
     *  and a name. */
    public UnitImpl(UnitBase base, int power, String name, double multi,
                    int exponent) {
        this(power, name, multi, exponent);
        if (base == null) {
            throw new IllegalArgumentException("Cannot create base Unit with null UnitBase.");
        }
        this.the_unit_base = base;
        elements = new UnitImpl[0];
    }

    /** A factory method to make sure that the input edu.iris.Fissures.unit
     is in fact a edu.iris.Fissurs.model.UnitImpl, to avoid class cast
     errors. The implementation will only create a new object if the
     inUnit is not an instance of UnitImpl.
     */
    public static UnitImpl createUnitImpl(UnitImpl inUnit) {
        if (inUnit instanceof UnitImpl) {
            return (UnitImpl)inUnit;
        } else {
            UnitImpl newUnit;
            if (inUnit.the_unit_base == UnitBase.COMPOSITE) {
                UnitImpl[] newSubUnits = new UnitImpl[inUnit.elements.length];
                for (int i=0; i<newSubUnits.length; i++) {
                    newSubUnits[i] = createUnitImpl(inUnit.elements[i]);
                } // end of for (int i=0; i<newSubUnits.length; i++)
                newUnit = new UnitImpl(newSubUnits,
                                       inUnit.power,
                                       inUnit.name,
                                       inUnit.multi_factor,
                                       inUnit.exponent);
            } else {
                newUnit = new UnitImpl(inUnit.the_unit_base,
                                       inUnit.power,
                                       inUnit.name,
                                       inUnit.multi_factor,
                                       inUnit.exponent);
            }
            return newUnit;
        } // end of else
    }

    /** Returns the number of subunits. For example meters per second would
     *  have 2 subunits and meters per second per second could have either
     *  2 or 3 depending on how it was constructed. A base unit has 0 subunits.
     *  @return the number of subunits. */
    public int getNumSubUnits() {
        if (the_unit_base.equals(UnitBase.COMPOSITE)) return elements.length;
        else return 0;
    }

    /** Returns the ith subunit of this Unit.
     *  @return the ith subunit. */
    public UnitImpl getSubUnit(int i) { return (UnitImpl)elements[i]; }

    public UnitImpl[] getSubUnits() {
        if (elements == null || elements.length == 0)  return new UnitImpl[0];
        UnitImpl[] temp = new UnitImpl[elements.length];
        System.arraycopy(elements, 0, temp, 0, temp.length);
        return temp;
    }
    
    /** for use by hibernate */
    protected void setSubUnits(UnitImpl[] u) {
        elements = u;
    }

    public boolean isBaseUnit() {
        if (the_unit_base == UnitBase.COMPOSITE)  return false;
        else  return true;
    }

    public UnitBase getBaseUnit() { return the_unit_base; }

    /** for use by hibernate */
    public int getBaseUnitInt() {
        return getBaseUnit().value();
    }
    
    /** for use by hibernate */
    protected void setBaseUnitInt(int base) {
        the_unit_base = UnitBase.from_int(base);
    }
    
    /** convoluted way to find out how many different unit primitives
     *  there are, since edu.iris.Fissures.UnitBase
     *  provides no accessor method and to avoid hard coding the value. */
    public static int getNumPrimitives() {
        int numPrimitives = 0;
        try {
            while (true) {
                UnitBase.from_int(numPrimitives);
                numPrimitives++;
            }
        } catch(Exception e) {
            // caught the exception, so
            // numPrimitives = the number of primitives
        }
        return numPrimitives;
    }

    /** Reduces the unit to its most basic SI form, combining all terms
     possible and bringing any constant factors out.
     */
    public UnitImpl getReducedUnit() {
        int primitiveExponent[] = getBaseExponents();

        // create a temp unit with the total multiplicative factor and power
        // of ten. The correct unit base will be put in during the first pass
        // through the loop
        UnitImpl unitTemp = new UnitImpl(UnitBase.from_int(0), getTotalPower(),
                                         "", getTotalMultiFactor(), 1);
        UnitImpl unitTotal = new UnitImpl(new UnitImpl[primitiveExponent.length],
                                          getTotalPower(), "",
                                          getTotalMultiFactor(), 1);

        int numSubUnits = 0;
        for (int i=0; i<primitiveExponent.length; i++) {
            if (primitiveExponent[i] != 0) {
                unitTemp.the_unit_base = UnitBase.from_int(i);
                unitTemp.exponent = primitiveExponent[i];
                unitTotal.elements[numSubUnits] = unitTemp;
                numSubUnits++;
                // create a new temp unit for the next pass
                unitTemp = new UnitImpl(UnitBase.from_int(0), getTotalPower(),
                                        "", getTotalMultiFactor(), 1);
            }
        }

        if (numSubUnits == 1) {
            // just a base unit, so just return the first element
            unitTotal = (UnitImpl)unitTotal.elements[0];
        }
        return unitTotal;
    }

    /** Returns the exponents of the subunits of this unit. They are indexed by
     *  the integers defined in
     *  edu.iris.Fissures.UnitImplBase.
     *  This is used primarily to determine if to units can be converted from
     *  one to the other.
     */
    public int[] getBaseExponents() {
        // now fill out the exponents of the "dimensions"
        // all are initialized to 0
        int[] baseExponents = new int[numPrimitives];
        if (the_unit_base != UnitBase.COMPOSITE) {
            baseExponents[the_unit_base.value()] = exponent;
            return baseExponents;
        }
        // not a BASE unit, so loop over all subunits
        for (int i=0; i< elements.length; i++) {
            UnitImpl subunit = (UnitImpl)elements[i];
            int[] subExponents = subunit.getBaseExponents();
            for (int j=0; j<baseExponents.length; j++) {
                baseExponents[j] += subExponents[j];
            }
        }
        // all subunits exponents must be multiplied by the unit's exponent,
        // EX meters per second squared might be represented as a METER
        // times a composite unit composed of two SECOND units, with the
        // additional factor of -1 in its exponent. In other words
        // M * ( S * S ) ^-1
        for (int j=0; j<baseExponents.length; j++) {
            baseExponents[j] *= exponent;
        }
        return baseExponents;
    }

    /** Returns the exponent for this unit. This ignores any exponents
     *  from any subunits.
     */
    public int getExponent() { return exponent; }

    /** Calcultes the total power of ten for this unit relative to the
     *  MKS base units. Used to convert from one set of units to another. */
    public int getTotalPower() {
        if (the_unit_base != UnitBase.COMPOSITE) {
            return getPower()*getExponent();
        } else {
            int power = 0;
            UnitImpl temp;
            for (int i=0; i< elements.length; i++) {
                temp = (UnitImpl)elements[i];
                power += temp.getTotalPower();
            }
            // don't forget that there might be a power for the whole composite
            // unit, so add it. Also, the exponent multiplies the power of ten
            return (power+getPower())*getExponent();
        }
    }

    /** Returns the power of ten for this unit. This ignores any powers
     *  from any subunits.
     */
    public int getPower() { return power; }

    /** Calcultes the total power of ten for this unit relative to the
     *  base units. Used to convert from one set of units to another. */
    public double getTotalMultiFactor() {
        if (the_unit_base != UnitBase.COMPOSITE) {
            return getMultiFactor();
        } else {
            double multi = 1.0;
            UnitImpl temp;
            for (int i=0; i< elements.length; i++) {
                temp = (UnitImpl)elements[i];
                multi *= temp.getTotalMultiFactor();
            }

            // don't forget that there might be a multiplicative factor
            // for the whole composite unit, so multiply it.

            return Math.pow(multi * getMultiFactor(), getExponent());
        }
    }

    /** Returns the multiplicative factor for this unit. This ignores
     *  any multiplicative factors from any subunits.
     */
    public double getMultiFactor() {
        return multi_factor;
    }

    /** decides whether this unit is convertable to another unit. */
    public boolean isConvertableTo(UnitImpl otherUnits) {
        int[] ourExponents = getBaseExponents();
        int[] otherExponents = otherUnits.getBaseExponents();
        for (int i=0; i< ourExponents.length; i++) {
            if (ourExponents[i] != otherExponents[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean isNamed() {
        if (name == null ||
            name.length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static String baseToString(UnitBase b) {
        switch (b.value()) {
            case UnitBase._METER: return "METER";
            case UnitBase._GRAM: return "GRAM";
            case UnitBase._SECOND: return "SECOND";
            case UnitBase._AMPERE: return "AMPERE";
            case UnitBase._KELVIN: return "KELVIN";
            case UnitBase._MOLE: return "MOLE";
            case UnitBase._CANDELA: return "CANDELA";
            case UnitBase._COUNT: return "COUNT";
            default: return null;
        }
    }

    public void toLongString(){
        System.out.println("*****"+ this +"*****");
        System.out.println("Base Unit: " + getBaseUnit());
        System.out.println("Exponents: " + getExponent());
        System.out.println("Power: " + getPower());
        System.out.println("Multifactor: " + getMultiFactor());
        System.out.println("IsBaseUnit: " + isBaseUnit());
        System.out.println("NumSubUnits: " + getNumSubUnits());
        System.out.println("Subunits: ");
        for (int i = 0; i < getNumSubUnits(); i++){
            getSubUnit(i).toLongString();
        }
    }

    public String toString() {
        if (isNamed()) {
            return name;
        } else {
            String s;

            if (multi_factor != 1.0f) {
                s = multi_factor+" * ";
            } else {
                s = "";
            }

            if (getPrefix(getPower()) != null) {
                s+=getPrefix(getPower());
            } else {
                s+="10^"+getPower()+" ";
            }

            if (the_unit_base == UnitBase.COMPOSITE) {
                if (elements.length != 1) {
                    s+="(";
                }
                String tempS = "";
                UnitImpl tempUnit;
                for (int i=0; i< elements.length; i++) {
                    tempUnit = (UnitImpl)elements[i];
                    if (tempUnit.isNamed()) {
                        tempS = tempUnit.toString()+" ";
                    } else {
                        tempS = "("+tempUnit.toString()+") ";
                    }
                    s += tempS;
                }
            } else {
                String tempS = baseToString(the_unit_base);
                if (tempS != null && tempS != "") {
                    s += tempS;
                } else {
                    s += "UNKNOWN"+the_unit_base.value();
                }
            }


            if (the_unit_base == UnitBase.COMPOSITE && elements.length != 1) {
                s+=")";
            }

            if (exponent != 1) {
                s += "^"+exponent;
            }
            return s;
        }
    }

    public int hashCode(){
        int result = 27;
        result = result * 37 + getPower();
        result = result * 37 + getHashValue(getMultiFactor());
        result = result * 37 + getExponent();
        result = result * 37 + (isBaseUnit() ? 0 : 1);
        return result;
    }

    private static int getHashValue(double d){
        long l = Double.doubleToLongBits(d);
        return (int)(l^(l>>>32));
    }

    public boolean equals(Object o) {
        if (super.equals(o)) return true;
        if (o instanceof UnitImpl) {
            UnitImpl u = (UnitImpl)o;
            if (u.getPower() != getPower() ||
                u.getMultiFactor() != getMultiFactor() ||
                u.getExponent() != getExponent() ||
                u.isBaseUnit() != isBaseUnit()) {
                return false;
            }
            if (isBaseUnit()) {
                // check for temp units as celsuis and fahrenheit only differ in name from kelvin
                if (getBaseUnit() == UnitBase.KELVIN && u.getBaseUnit() == UnitBase.KELVIN) {
                    return name != null && name.equalsIgnoreCase(u.name);
                }
                if (u.getBaseUnit() != getBaseUnit()) {
                    return false;
                } else {
                    return true;
                }
            } else {
                if (u.getNumSubUnits() != getNumSubUnits()) {
                    // warning: this doesn't work if different representations,
                    // ie m/s/s and m/s^2
                    return false;
                }
                for (int i=0; i<getNumSubUnits(); i++) {
                    if ( ! u.getSubUnit(i).equals(getSubUnit(i))) {
                        // WARNING: this doesn't work if they are the same but ordered differently
                        return false;
                    }
                }
                return true;
            }
        } else {
            return false;
        } // end of else

    }

    /** creates and inverse to this unit with a given name.
     * @return a new unit that is the inverse (^-1) of this unit.
     */
    public UnitImpl inverse() {
        if (name == null ||
            name.length() == 0) {
            return inverse("");
        } else {
            return inverse(name+"^-1");
        }
    }

    /** @return a new unit that is the inverse (^-1) of this unit.
     */
    public UnitImpl inverse(String name) {
        if (the_unit_base != UnitBase.COMPOSITE) {
            return new UnitImpl(the_unit_base,
                                power,
                                name,
                                multi_factor,
                                exponent * -1);
        } else {
            return new UnitImpl(elements,
                                power,
                                name,
                                multi_factor,
                                exponent * -1);
        }
    }

    /** creates a new unit that is the product of a float multiplicative
     factor and a unit, without a given name. */
    public static UnitImpl multiply(double f, UnitImpl u) {
        return multiply(f, u, "");
    }

    /** creates a new unit that is the product of a float multiplicative
     factor and a unit with a name. */
    public static UnitImpl multiply(double f, UnitImpl u, String name) {
        UnitImpl[] elements = new UnitImpl[1];
        elements[0] = u;
        return new UnitImpl( elements,
                            0,
                            name,
                            f,
                            1);
    }

    /** creates a new unit that is the product of the two units. */
    public static UnitImpl multiply(UnitImpl a, UnitImpl b) {
        return multiply(a, b, "");
    }

    /** creates a new unit that is the product of the two units. */
    public static UnitImpl multiply(UnitImpl a, UnitImpl b, String name) {
        UnitImpl[] elements = new UnitImpl[2];
        elements[0] = a;
        elements[1] = b;
        return new UnitImpl ( elements,
                             0,
                             name,
                             1.0f,
                             1);
    }

    /** creates a new unit that is the product of the units. */
    public static UnitImpl multiply(UnitImpl[] units) {
        return multiply(units, "");
    }

    /** creates a new unit that is the product of the units. */
    public static UnitImpl multiply(UnitImpl[] units, String name) {
        return new UnitImpl( units,
                            0,
                            name,
                            1.0f,
                            1);
    }

    /** creates a new unit that is the product of the first unit and the
     * inverse of the second unit. */
    public static UnitImpl divide(UnitImpl a, UnitImpl b) {
        return divide(a, b, "");
    }

    /** creates a new unit that is the product of the first unit and the
     * inverse of the second unit. */
    public static UnitImpl divide(UnitImpl a, UnitImpl b, String name) {
        UnitImpl[] elements = new
            UnitImpl[2];
        elements[0] = a;
        elements[1] = b.inverse();
        return new UnitImpl( elements,
                            0,
                            name,
                            1.0f,
                            1);
    }

    /* the powers of common prefixes.
     *  @return the string prefix for common powers of ten, or null if
     *  there is no prefix.
     */
    public static final String getPrefix(int power) {
        switch (power) {
            case YOTTA: return "yotta";
            case ZETTA: return "zetta";
            case EXA: return "exa";
            case PETA: return "peta";
            case TERA: return "tera";
            case GIGA: return "giga";
            case MEGA: return "mega";
            case KILO: return "kilo";
            case HECTO: return "hecto";
            case DEKA: return "deka";
            case 0: return "";
            case DECI: return "deci";
            case CENTI: return "centi";
            case MILLI: return "milli";
            case MICRO: return "micro";
            case NANO: return "nano";
            case PICO: return "pico";
            case FEMTO: return "femto";
            case ATTO: return "atto";
            case ZEPTO: return "zepto";
            case YOCTO: return "yocto";
            default: return null;
        }
    }

    public static final UnitImpl getUnitFromString(String unitName) throws NoSuchFieldException{
        try {
            return (UnitImpl)UnitImpl.class.getField(unitName.toUpperCase()).get(null);
        } catch (IllegalAccessException e) {
            //neither this nor the following exception should happen since the
            //NoSuchFieldException wasn't thrown by class.getField()
        } catch (IllegalArgumentException e) {}
        throw new RuntimeException("This was supposed to be unreachable");
    }

    public static final int YOTTA = 24;
    public static final int ZETTA = 21;
    public static final int EXA = 18;
    public static final int PETA = 15;
    public static final int TERA = 12;
    public static final int GIGA = 9;
    public static final int MEGA = 6;
    public static final int KILO = 3;
    public static final int HECTO = 2;
    public static final int DEKA = 1;

    public static final int NONE = 0;

    public static final int DECI = -1;
    public static final int CENTI = -2;
    public static final int MILLI = -3;
    public static final int MICRO = -6;
    public static final int NANO = -9;
    public static final int PICO = -12;
    public static final int FEMTO = -15;
    public static final int ATTO = -18;
    public static final int ZEPTO = -21;
    public static final int YOCTO = -24;

    /* common length units. */
    public static final UnitImpl METER = new UnitImpl(UnitBase.METER, 1, NONE);
    public static final UnitImpl LENGTH = METER;
    public static final UnitImpl KILOMETER = new UnitImpl(UnitBase.METER, 1, KILO);
    public static final UnitImpl CENTIMETER = new UnitImpl(UnitBase.METER, 1, CENTI);
    public static final UnitImpl MILLIMETER = new UnitImpl(UnitBase.METER, 1, MILLI);
    public static final UnitImpl MICROMETER = new UnitImpl(UnitBase.METER, 1, MICRO);
    public static final UnitImpl MICRON = MICROMETER;
    public static final UnitImpl NANOMETER = new UnitImpl(UnitBase.METER, 1, NANO);
    public static final UnitImpl PICOMETER = new UnitImpl(UnitBase.METER, 1, PICO);
    public static final UnitImpl INCH = UnitImpl.multiply(2.54, CENTIMETER, "INCH");
    public static final UnitImpl FOOT = UnitImpl.multiply(12, INCH, "FOOT");
    public static final UnitImpl MILE = UnitImpl.multiply(5280, FOOT, "MILE");
    public static final UnitImpl YARD = UnitImpl.multiply(3, FOOT, "YARD");
    public static final UnitImpl ROD = UnitImpl.multiply(5.5, YARD, "ROD");
    public static final UnitImpl FURLONG = UnitImpl.multiply(40, ROD, "FURLONG");
    public static final UnitImpl LEAGUE = UnitImpl.multiply(3, MILE, "LEAGUE");


    /* common time units. */
    public static final UnitImpl SECOND = new UnitImpl(UnitBase.SECOND, 1, NONE);
    public static final UnitImpl TIME = SECOND;
    public static final UnitImpl MINUTE = UnitImpl.multiply(60, SECOND, "MINUTE");
    public static final UnitImpl HOUR = UnitImpl.multiply(60, MINUTE, "HOUR");
    public static final UnitImpl DAY = UnitImpl.multiply(24, HOUR, "DAY");
    public static final UnitImpl WEEK = UnitImpl.multiply(7, DAY, "WEEK");
    public static final UnitImpl FORTNIGHT = UnitImpl.multiply(14, DAY, "FORTNIGHT");
    /** normal year of 365 days */
    public static final UnitImpl YEAR = UnitImpl.multiply(365, DAY, "YEAR");
    /** leap year of 366 days */
    public static final UnitImpl LEAP_YEAR = UnitImpl.multiply(366, DAY, "LEAP_YEAR");
    /** 365.2425 days */
    public static final UnitImpl GREGORIAN_YEAR = UnitImpl.multiply(365.2425, DAY, "GREGORIAN_YEAR");
    public static final UnitImpl MILLISECOND = new UnitImpl(UnitBase.SECOND, 1, MILLI);
    /** tenth of a milliseconds, useful for SEED time format.*/
    public static final UnitImpl TENTHMILLISECOND = new UnitImpl(UnitBase.SECOND, 1, -4);
    public static final UnitImpl MICROSECOND = new UnitImpl(UnitBase.SECOND, 1, MICRO);
    public static final UnitImpl NANOSECOND = new UnitImpl(UnitBase.SECOND, 1, NANO);

    public static final UnitImpl HERTZ = new UnitImpl(UnitBase.SECOND, -1, NONE);

    /* common area units. */
    public static final UnitImpl SQUARE_METER = new UnitImpl(UnitBase.METER, 2, NONE);
    public static final UnitImpl AREA = SQUARE_METER;
    public static final UnitImpl SQUARE_CENTIMETER = new UnitImpl(UnitBase.METER, 2, CENTI);

    /* common volume units. */
    public static final UnitImpl CUBIC_METER = new UnitImpl(UnitBase.METER, 3, NONE);
    public static final UnitImpl VOLUME = CUBIC_METER;
    public static final UnitImpl CUBIC_CENTIMETER = new UnitImpl(UnitBase.METER, 3, CENTI);
    public static final UnitImpl LITER = new UnitImpl(UnitBase.METER, 3, DECI);

    /* common mass units. */
    public static final UnitImpl GRAM = new UnitImpl(UnitBase.GRAM, 1, NONE);
    public static final UnitImpl KILOGRAM = new UnitImpl(UnitBase.GRAM, 1, KILO);
    public static final UnitImpl MASS = KILOGRAM;

    /* common density units. */
    public static final UnitImpl DENSITY = UnitImpl.divide(MASS, VOLUME);
    public static final UnitImpl GRAM_PER_CUBIC_CENTIMETER =
        UnitImpl.divide(GRAM, CUBIC_CENTIMETER);
    public static final UnitImpl KILOGRAM_PER_CUBIC_METER =
        UnitImpl.divide(KILOGRAM, CUBIC_METER);

    /* common velocity units. */
    public static final UnitImpl VELOCITY = UnitImpl.divide(LENGTH, TIME);
    public static final UnitImpl METER_PER_SECOND = UnitImpl.divide(METER, SECOND);
    public static final UnitImpl KILOMETER_PER_SECOND =
        UnitImpl.divide(KILOMETER, SECOND);
    public static final UnitImpl CENTIMETER_PER_SECOND =
        UnitImpl.divide(CENTIMETER, SECOND);
    public static final UnitImpl MILLIMETER_PER_SECOND =
        UnitImpl.divide(MILLIMETER, SECOND);
    public static final UnitImpl MICROMETER_PER_SECOND =
        UnitImpl.divide(MICROMETER, SECOND);
    public static final UnitImpl MICRON_PER_SECOND = MICROMETER_PER_SECOND;
    public static final UnitImpl NANOMETER_PER_SECOND =
        UnitImpl.divide(NANOMETER, SECOND);

    /* common acceleration units. */
    public static final UnitImpl METER_PER_SECOND_PER_SECOND =
        UnitImpl.divide(UnitImpl.divide(METER, SECOND), SECOND);
    public static final UnitImpl ACCELERATION = UnitImpl.divide(VELOCITY, TIME);
    public static final UnitImpl KILOMETER_PER_SECOND_PER_SECOND =
        UnitImpl.divide(UnitImpl.divide(KILOMETER, SECOND), SECOND);
    public static final UnitImpl CENTIMETER_PER_SECOND_PER_SECOND =
        UnitImpl.divide(UnitImpl.divide(CENTIMETER, SECOND), SECOND);
    public static final UnitImpl MILLIMETER_PER_SECOND_PER_SECOND =
        UnitImpl.divide(UnitImpl.divide(MILLIMETER, SECOND), SECOND);
    public static final UnitImpl MICROMETER_PER_SECOND_PER_SECOND =
        UnitImpl.divide(UnitImpl.divide(MICROMETER, SECOND), SECOND);
    public static final UnitImpl NANOMETER_PER_SECOND_PER_SECOND =
        UnitImpl.divide(UnitImpl.divide(NANOMETER, SECOND), SECOND);

    /* common force units. */
    public static final UnitImpl FORCE = UnitImpl.multiply(MASS, ACCELERATION);
    public static final UnitImpl NEWTON =
        UnitImpl.multiply(KILOGRAM, METER_PER_SECOND_PER_SECOND, "NEWTON");
    public static final UnitImpl PASCAL = UnitImpl.divide(NEWTON,SQUARE_METER,"PASCAL");
    public static final UnitImpl HECTOPASCAL = UnitImpl.multiply(100, PASCAL, "HECTOPASSCAL");
    public static final UnitImpl KILOPASCAL = UnitImpl.multiply(1000, PASCAL, "KILOPASSCAL");
    public static final UnitImpl BAR = UnitImpl.multiply(100000, PASCAL, "BAR");
    public static final UnitImpl MILLIBAR = UnitImpl.multiply(.001, BAR, "MILLIBAR");
    
    /* strain */
    public static final UnitImpl CUBIC_METER_PER_CUBIC_METER = UnitImpl.divide(CUBIC_METER, CUBIC_METER);
    public static final UnitImpl METER_PER_METER = UnitImpl.divide(METER, METER);
    
    /* common energy units. */
    public static final UnitImpl JOULE = UnitImpl.multiply(NEWTON, METER, "JOULE");
    public static final UnitImpl ENERGY = UnitImpl.multiply(FORCE, LENGTH);
    public static final UnitImpl DYNE = UnitImpl.multiply(UnitImpl.multiply(GRAM, CENTIMETER_PER_SECOND_PER_SECOND),
                                                          CENTIMETER, "DYNE");
    /* common electrical units. */
    public static final UnitImpl AMPERE = new UnitImpl(UnitBase.AMPERE, 1, NONE);
    public static final UnitImpl COULOMB = UnitImpl.multiply(AMPERE, SECOND, "COULOMB");
    public static final UnitImpl VOLT = UnitImpl.divide(JOULE, COULOMB, "VOLT");
    public static final UnitImpl WATT = UnitImpl.divide(JOULE, SECOND, "WATT");
    public static final UnitImpl WEBER = UnitImpl.multiply(VOLT, SECOND, "WEBER");
    public static final UnitImpl TESLA = UnitImpl.divide(WEBER, SQUARE_METER, "TESLA");
    public static final UnitImpl VOLT_PER_METER = UnitImpl.divide(VOLT, METER);
    public static final UnitImpl WATT_PER_SQUARE_METER = UnitImpl.divide(WATT, SQUARE_METER);

    /* common angular units. */
    public static final UnitImpl RADIAN = UnitImpl.divide(LENGTH, LENGTH, "RADIAN");
    public static final UnitImpl DEGREE = UnitImpl.multiply((180.0 / Math.PI),
                                                            RADIAN, "DEGREE");

    /* unit for counts */
    public static final UnitImpl COUNT = new UnitImpl(UnitBase.COUNT, 1, NONE);
    public static final UnitImpl MILLICOUNT = new UnitImpl(UnitBase.COUNT, 1, MILLI);
    public static final UnitImpl MICROCOUNT = new UnitImpl(UnitBase.COUNT, 1, MICRO);
    public static final UnitImpl KILOCOUNT = new UnitImpl(UnitBase.COUNT, 1, KILO);
    public static final UnitImpl MEGACOUNT = new UnitImpl(UnitBase.COUNT, 1, MEGA);

    /** This is a big dumb, butis the easiest way to create a non-unit. It is also
     *  different from COUNT because COUNT is a primitive base unit, and hence is not
     * convertable to this.*/
    public static final UnitImpl DIMENSONLESS = UnitImpl.divide(SECOND, SECOND, "");
    public static final UnitImpl DIMENSIONLESS = DIMENSONLESS; // I wish I could spell

    /** temperature */
    public static final String KELVIN_NAME = "KELVIN";
    public static final UnitImpl KELVIN = new UnitImpl(UnitBase.KELVIN, 1, KELVIN_NAME, 1, 1);
    public static final String CELSIUS_NAME = "CELSIUS";
    public static final UnitImpl CELSIUS = new UnitImpl(UnitBase.KELVIN, NONE, CELSIUS_NAME, 1, 1);
    public static final String FAHRENHEIT_NAME = "FAHRENHEIT";
    public static final UnitImpl FAHRENHEIT = new UnitImpl(UnitBase.KELVIN, NONE, FAHRENHEIT_NAME, 9.0/5.0, 1);
    
    /** just because we can */
    public static final UnitImpl CANDELA = new UnitImpl(UnitBase.CANDELA, 1, NONE);
    public static final UnitImpl MOLE = new UnitImpl(UnitBase.MOLE, 1, NONE);
    
    /** This is an even dumber unit, exists so that we can fudge places where a
     * unit is required, but the code can't create it, perhaps in loading from a
     * string. DMC uses this in responses when they get something like
     * TC - TEMPERATURE IN DEGREES CELSIUS
     * that is not parsible and has not been special-cased in their code.
     */
    public static final UnitImpl UNKNOWN = UnitImpl.divide(CANDELA, CANDELA, "UNKNOWN");
    
    /* angular velocity */
    public static final UnitImpl RADIAN_PER_SECOND = UnitImpl.divide(RADIAN, SECOND);

    // hibernate
    protected Integer dbid;
    protected void setDbid(Integer dbid) {
        this.dbid = dbid;
    }
    public Integer getDbid() {
        return dbid;
    }

    public List<UnitImpl> getSubUnitsList() {
        // hibernate needs same collection returned from get as it put in via set for dirty checking
        if(hibernateSubUnitList != null) {return hibernateSubUnitList;}
        ArrayList out = new ArrayList();
        for(int i = 0; i < elements.length; i++) {
            out.add(elements[i]);
        }
        return out;
    }
    
    protected void setSubUnitsList(List<UnitImpl> list) {
        hibernateSubUnitList = list;
        elements = new UnitImpl[list.size()];
        elements = (UnitImpl[])list.toArray(elements);
    }
    
    protected List<UnitImpl> hibernateSubUnitList = null;
}
