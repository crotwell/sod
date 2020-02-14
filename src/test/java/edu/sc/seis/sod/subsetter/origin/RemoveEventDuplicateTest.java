package edu.sc.seis.sod.subsetter.origin;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;



public class RemoveEventDuplicateTest  {
    
	@Test
    public void testSetTimeVariance() throws ConfigurationException {
        RemoveEventDuplicate r = new RemoveEventDuplicate(Duration.ofHours(42),
                                                          new QuantityImpl(3, UnitImpl.DEGREE),
                                                          new QuantityImpl(10, UnitImpl.KILOMETER));
    }
}
