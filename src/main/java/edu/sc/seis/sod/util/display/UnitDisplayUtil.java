/**
 * UnitDisplayUtil.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.util.display;

import java.text.NumberFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.UnitRangeImpl;

public class UnitDisplayUtil {

    public static final String mu = "\u03BC";

    public static QuantityImpl getBestForDisplay(QuantityImpl quantity) {
        UnitRangeImpl inRange = new UnitRangeImpl(quantity.getValue(),
                                                  quantity.getValue(),
                                                  quantity.getUnit());
        inRange = getBestForDisplay(inRange);
        return new QuantityImpl(inRange.getMinValue(), inRange.getUnit());
    }

    public static UnitRangeImpl getBestForDisplay(UnitRangeImpl inRange) {
        // just in case we don't have a case for this unit
        UnitRangeImpl outRange = inRange;
        if(inRange.getUnit().isConvertableTo(UnitImpl.METER_PER_SECOND)) {
            // velocity
            inRange = inRange.convertTo(UnitImpl.METER_PER_SECOND);
            if(Math.abs(inRange.getMinValue()) < .000001
                    && Math.abs(inRange.getMaxValue()) < .000001) {
                // use nanometer/sec
                outRange = inRange.convertTo(UnitImpl.NANOMETER_PER_SECOND);
            } else if(Math.abs(inRange.getMinValue()) < .001
                    && Math.abs(inRange.getMaxValue()) < .001) {
                // use micron/sec
                outRange = inRange.convertTo(UnitImpl.MICRON_PER_SECOND);
            } else if(Math.abs(inRange.getMinValue()) < 1
                    && Math.abs(inRange.getMaxValue()) < 1) {
                // use mm/sec
                outRange = inRange.convertTo(UnitImpl.MILLIMETER_PER_SECOND);
            }
        } else if(inRange.getUnit().isConvertableTo(UnitImpl.METER)) {
            // displacement
            inRange = inRange.convertTo(UnitImpl.METER);
            if(Math.abs(inRange.getMinValue()) < .000001
                    && Math.abs(inRange.getMaxValue()) < .000001) {
                // use nanometer
                outRange = inRange.convertTo(UnitImpl.NANOMETER);
            } else if(Math.abs(inRange.getMinValue()) < .001
                    && Math.abs(inRange.getMaxValue()) < .001) {
                // use micron
                outRange = inRange.convertTo(UnitImpl.MICRON);
            } else if(Math.abs(inRange.getMinValue()) < 1
                    && Math.abs(inRange.getMaxValue()) < 1) {
                // use mm
                outRange = inRange.convertTo(UnitImpl.MILLIMETER);
            }
        } else if(inRange.getUnit()
                .isConvertableTo(UnitImpl.METER_PER_SECOND_PER_SECOND)) {
            // acceleration
            inRange = inRange.convertTo(UnitImpl.METER_PER_SECOND_PER_SECOND);
            if(Math.abs(inRange.getMinValue()) < .000001
                    && Math.abs(inRange.getMaxValue()) < .000001) {
                // use nanometer/sec/sec
                outRange = inRange.convertTo(UnitImpl.NANOMETER_PER_SECOND_PER_SECOND);
            } else if(Math.abs(inRange.getMinValue()) < .001
                    && Math.abs(inRange.getMaxValue()) < .001) {
                // use micron/sec/sec
                outRange = inRange.convertTo(UnitImpl.MICROMETER_PER_SECOND_PER_SECOND);
            } else if(Math.abs(inRange.getMinValue()) < 1
                    && Math.abs(inRange.getMaxValue()) < 1) {
                // use mm/sec/sec
                outRange = inRange.convertTo(UnitImpl.MILLIMETER_PER_SECOND_PER_SECOND);
            }
        } else if(inRange.getUnit().isConvertableTo(UnitImpl.COUNT)) {
            // acceleration
            inRange = inRange.convertTo(UnitImpl.COUNT);
            if(Math.abs(inRange.getMinValue()) < .001
                    && Math.abs(inRange.getMaxValue()) < .001) {
                outRange = inRange.convertTo(UnitImpl.MICROCOUNT);
            } else if(Math.abs(inRange.getMinValue()) < 1
                    && Math.abs(inRange.getMaxValue()) < 1) {
                outRange = inRange.convertTo(UnitImpl.MILLICOUNT);
            } else if(Math.abs(inRange.getMinValue()) < 1000
                    && Math.abs(inRange.getMaxValue()) < 1000) {
                outRange = inRange.convertTo(UnitImpl.COUNT);
            } else if(Math.abs(inRange.getMinValue()) < 1000000
                    && Math.abs(inRange.getMaxValue()) < 1000000) {
                outRange = inRange.convertTo(UnitImpl.KILOCOUNT);
            } else {
                outRange = inRange.convertTo(UnitImpl.MEGACOUNT);
            }
        } else {
            // logger.debug("No case, using amp range of
            // "+outRange.getMinValue()+" to "
            // +outRange.getMaxValue()+" "+
            // outRange.getUnit());
        }
        return outRange;
    }

    /**
     * calculates a new UnitRangeImpl using the response of the given
     * seismogram. If seis does not have a response, then the input amp is used.
     */
    /*
    @Deprecated
    
    public static UnitRangeImpl getRealWorldUnitRange(UnitRangeImpl ur,
                                                      DataSetSeismogram seismo) {
        UnitRangeImpl out = ur;
        if(ur.getUnit().isConvertableTo(UnitImpl.COUNT)) {
            Object responseObj = seismo.getAuxillaryData(StdAuxillaryDataNames.RESPONSE);
            if(responseObj != null && responseObj instanceof Response) {
                Response response = (Response)responseObj;
                UnitImpl realWorldUnit = (UnitImpl)response.stages[0].input_units;
                // this is the constant to divide by to get real worl units (not
                // counts)
                float sensitivity = response.the_sensitivity.sensitivity_factor;
                // logger.debug("sensitivity is "+sensitivity+" to get to
                // "+realWorldUnit);
                if(sensitivity > 0) {
                    out = new UnitRangeImpl(ur.getMinValue() / sensitivity,
                                            ur.getMaxValue() / sensitivity,
                                            realWorldUnit);
                } else {
                    out = new UnitRangeImpl(ur.getMaxValue() / sensitivity,
                                            ur.getMinValue() / sensitivity,
                                            realWorldUnit);
                    seismo.addAuxillaryData("sensitivity",
                                            response.the_sensitivity);
                }
            } else if(seismo.getYUnit() != null) {
                UnitImpl y_unit = (UnitImpl)seismo.getYUnit();
                out = new UnitRangeImpl(ur.getMinValue(),
                                        ur.getMaxValue(),
                                        y_unit);
            }
        }
        return getBestForDisplay(out);
    }
    */

    /**
     * tries to come up with better names for some standard units than the
     * auto-generated versions.
     */
    public static String getNameForUnit(UnitImpl unit) {
        // most common
        if(unit.equals(UnitImpl.METER_PER_SECOND)) {
            return "m/s";
        }
        if(unit.equals(UnitImpl.MICRON_PER_SECOND)) {
            return "microns/sec";
        }
        if(unit.equals(UnitImpl.MILLIMETER_PER_SECOND)) {
            return "mm/s";
        }
        if(unit.equals(UnitImpl.NANOMETER_PER_SECOND)) {
            return "nm/s";
        }
        if(unit.equals(UnitImpl.KILOMETER_PER_SECOND)) {
            return "km/s";
        }
        if(unit.equals(UnitImpl.KILOMETER)) {
            return "km";
        }
        if(unit.equals(UnitImpl.METER)) {
            return "m";
        }
        if(unit.equals(UnitImpl.MILLIMETER)) {
            return "mm";
        }
        if(unit.equals(UnitImpl.MICROMETER)) {
            return "micrometers";
        }
        if(unit.equals(UnitImpl.NANOMETER)) {
            return "nanometers";
        }
        if(unit.equals(UnitImpl.METER_PER_SECOND_PER_SECOND)) {
            return "m/s/s";
        }
        if(unit.equals(UnitImpl.MILLIMETER_PER_SECOND_PER_SECOND)) {
            return "mm/s/s";
        }
        if(unit.equals(UnitImpl.MICROMETER_PER_SECOND_PER_SECOND)) {
            return "microns/s/s";
        }
        if(unit.equals(UnitImpl.NANOMETER_PER_SECOND_PER_SECOND)) {
            return "nm/s/s";
        }
        if(unit.equals(UnitImpl.SECOND)) {
            return "s";
        }
        if(unit.equals(UnitImpl.DEGREE)) {
            return "deg";
        }
        if(unit.equals(UnitImpl.COUNT)) {
            return "COUNTS";
        }
        if(unit.equals(UnitImpl.MILLICOUNT)) {
            return "COUNTS x 10^-3";
        }
        if(unit.equals(UnitImpl.MICROCOUNT)) {
            return "COUNTS x 10^-6";
        }
        if(unit.equals(UnitImpl.KILOCOUNT)) {
            return "COUNTS x 10^3";
        }
        if(unit.equals(UnitImpl.MEGACOUNT)) {
            return "COUNTS x 10^6";
        }
        if(unit.equals(UnitImpl.DIMENSONLESS)) {
            return "";
        }
        // not a unit we have a friendly name for
        // logger.debug("not a unit we have a friendly name
        // for"+unit.toString());
        return unit.toString();
    }

    public static String formatQuantityImpl(QuantityImpl quantity) {
        return formatQuantityImpl(quantity, quantityFormat);
    }

    public static String formatQuantityImpl(QuantityImpl quantity,
                                            NumberFormat format,
                                            UnitImpl preferredUnit) {
        if (((UnitImpl)quantity.getUnit()).isConvertableTo(preferredUnit)) {
            quantity = ((QuantityImpl)quantity).convertTo(preferredUnit);
        }
        return formatQuantityImpl(quantity, format);
    }
    
    public static String formatQuantityImpl(QuantityImpl quantity,
                                            NumberFormat format) {
        if(quantity != null) {
            return format.format(quantity.getValue())
                    + " "
                    + getNameForUnit((UnitImpl)quantity.getUnit()).toLowerCase();
        }
        return "...";
    }

    static final ThreadSafeDecimalFormat quantityFormat = new ThreadSafeDecimalFormat("#,###,##0.0##; -#,###,##0.0##");

    static Logger logger = LoggerFactory.getLogger(UnitDisplayUtil.class);
}
