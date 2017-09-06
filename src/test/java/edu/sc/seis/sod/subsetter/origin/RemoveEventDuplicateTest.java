package edu.sc.seis.sod.subsetter.origin;

import java.time.Duration;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import junit.framework.TestCase;



public class RemoveEventDuplicateTest extends TestCase {
    
    public void testSetTimeVariance() throws ConfigurationException {
        RemoveEventDuplicate r = new RemoveEventDuplicate(Duration.ofHours(42),
                                                          new QuantityImpl(3, UnitImpl.DEGREE),
                                                          new QuantityImpl(10, UnitImpl.KILOMETER));
    }
}
