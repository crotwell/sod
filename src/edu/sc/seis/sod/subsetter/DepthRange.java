package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.model.QuantityImpl;
import org.w3c.dom.Element;

/**
 * DepthRange.java Utility class for Depth Range. Created: Tue Apr 2 13:34:59
 * 2002
 * 
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla </a>
 * @version
 */
public class DepthRange extends edu.sc.seis.sod.subsetter.UnitRange {

    public DepthRange(Element config) throws Exception {
        super(config);
    }

    public QuantityImpl getMinDepth() {
        return new QuantityImpl(getUnitRange().min_value,
                                getUnitRange().the_units);
    }

    public QuantityImpl getMaxDepth() {
        return new QuantityImpl(getUnitRange().max_value,
                                getUnitRange().the_units);
    }
}// DepthRange
