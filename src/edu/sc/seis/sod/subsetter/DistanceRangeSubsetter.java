package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

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

    /**
     * Creates a new <code>DistanceRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public DistanceRangeSubsetter (Element config) throws ConfigurationException{
        processConfig(config);
    }

    /**
     * Describe <code>processConfig</code> method here.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public void processConfig(Element config) throws ConfigurationException{
    unitRange = SodUtil.loadUnitRange(config);

    }

    /**
     * Describe <code>getDistanceRange</code> method here.
     *
     * @return an <code>edu.iris.Fissures.UnitRange</code> value
     */
    public edu.iris.Fissures.UnitRange  getDistanceRange() {

    return unitRange;

    }

    /**
     * Describe <code>getMinDistance</code> method here.
     *
     * @return a <code>Quantity</code> value
     */
    public QuantityImpl getMinDistance() {

    return new QuantityImpl(getDistanceRange().min_value, getDistanceRange().the_units);

    }


    /**
     * Describe <code>getMaxDistance</code> method here.
     *
     * @return a <code>Quantity</code> value
     */
    public QuantityImpl getMaxDistance() {

    return new QuantityImpl(getDistanceRange().max_value, getDistanceRange().the_units);
    }

    private edu.iris.Fissures.UnitRange unitRange = null;

}// DistanceRange
