package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

/**
 * DistanceRange.java
 *
 *
 * Created: Mon Apr  8 16:09:49 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DistanceRangeSubsetter implements SodElement{
    public DistanceRangeSubsetter (Element config) throws ConfigurationException{
        processConfig(config);
    }

    public void processConfig(Element config) throws ConfigurationException{
        unitRange = SodUtil.loadUnitRange(config);
    }

    public edu.iris.Fissures.UnitRange  getUnitRange() { return unitRange; }

    public QuantityImpl getMin() {
        return new QuantityImpl(getUnitRange().min_value,
                                getUnitRange().the_units);
    }

    public QuantityImpl getMax() {
        return new QuantityImpl(getUnitRange().max_value,
                                getUnitRange().the_units);
    }

    private edu.iris.Fissures.UnitRange unitRange;
}// DistanceRange
