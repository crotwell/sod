package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;

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
    public DepthRange (Element config){
	super(config);
	System.out.println("IN DEPTH RANGE minimum depth is "+getUnitRange().min_value);
    }
 
    /**
     * Describe <code>getMinDepth</code> method here.
     *
     * @return a <code>Quantity</code> value
     */
    public Quantity getMinDepth() {
	System.out.println("minimum depth is "+getUnitRange().min_value);
	return new QuantityImpl(getUnitRange().min_value, getUnitRange().the_units);
    }

    /**
     * Describe <code>getMaxDepth</code> method here.
     *
     * @return a <code>Quantity</code> value
     */
    public Quantity getMaxDepth() {
	System.out.println("maximum depth is "+getUnitRange().max_value);
	return new QuantityImpl(getUnitRange().max_value, getUnitRange().the_units);
    }
  
}// DepthRange
