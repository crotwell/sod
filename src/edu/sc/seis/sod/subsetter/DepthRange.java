package edu.sc.seis.sod.subsetter;



import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * DepthRange.java Utility class for Depth Range.
 *
 *
 * Created: Tue Apr  2 13:34:59 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public class DepthRange extends edu.sc.seis.sod.subsetter.UnitRange {
    /**
     * Creates a new <code>DepthRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public DepthRange (Element config) throws Exception{
	super(config);
    }
 
    /**
     * Describe <code>getMinDepth</code> method here.
     *
     * @return a <code>Quantity</code> value
     */
    public Quantity getMinDepth() {
	return new QuantityImpl(getUnitRange().min_value, getUnitRange().the_units);
    }

    /**
     * Describe <code>getMaxDepth</code> method here.
     *
     * @return a <code>Quantity</code> value
     */
    public Quantity getMaxDepth() {
	return new QuantityImpl(getUnitRange().max_value, getUnitRange().the_units);
    }
  
}// DepthRange
