package edu.sc.seis.sod.subsetter;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.UnitRangeImpl;

/**
 * DistanceRange.java Created: Mon Apr 8 16:09:49 2002
 * 
 * @author Srinivasa Telukutla
 */
public class DistanceRangeSubsetter implements SodElement {

    public DistanceRangeSubsetter(Element config) throws ConfigurationException {
        processConfig(config);
    }

    public void processConfig(Element config) throws ConfigurationException {
        unitRange = SodUtil.loadUnitRange(config);
        if(((UnitImpl)unitRange.the_units).isConvertableTo(UnitImpl.KILOMETER)) {
            QuantityImpl min = new QuantityImpl(unitRange.getMinValue(),
                                                unitRange.getUnit());
            QuantityImpl max = new QuantityImpl(unitRange.getMaxValue(),
                                                unitRange.getUnit());
            unitRange = new UnitRangeImpl(DistAz.kilometersToDegrees(min.getValue(UnitImpl.KILOMETER)),
                                          DistAz.kilometersToDegrees(max.getValue(UnitImpl.KILOMETER)),
                                          UnitImpl.DEGREE);
        }
    }

    public UnitRangeImpl getUnitRange() {
        return unitRange;
    }

    public QuantityImpl getMin() {
        return new QuantityImpl(getUnitRange().min_value,
                                getUnitRange().the_units);
    }

    public QuantityImpl getMax() {
        return new QuantityImpl(getUnitRange().max_value,
                                getUnitRange().the_units);
    }

    private UnitRangeImpl unitRange;
}// DistanceRange
