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

public class DistanceRange extends edu.sc.seis.sod.subsetter.UnitRange implements Subsetter{
    public DistanceRange (Element config){
	super(config);
	//have to process to get the unit Type....
    }

    public Quantity getMinDistance() {

	return new QuantityImpl(getUnitRange().min_value, getUnitRange().the_units);

    }


    public Quantity getMaxDistance() {
	
	return new QuantityImpl(getUnitRange().max_value, getUnitRange().the_units);
    }
    
        
}// DistanceRange
