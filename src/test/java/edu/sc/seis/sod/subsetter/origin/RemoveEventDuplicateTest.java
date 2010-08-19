package edu.sc.seis.sod.subsetter.origin;

import junit.framework.TestCase;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.ConfigurationException;



public class RemoveEventDuplicateTest extends TestCase {
    
    public void testSetTimeVariance() throws ConfigurationException {
        RemoveEventDuplicate r = new RemoveEventDuplicate(new QuantityImpl(42, UnitImpl.HOUR),
                                                          new QuantityImpl(3, UnitImpl.DEGREE),
                                                          new QuantityImpl(10, UnitImpl.KILOMETER));
    }
}
