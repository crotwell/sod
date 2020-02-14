package edu.sc.seis.sod.subsetter.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.mock.station.MockNetworkAttr;


public class TemporaryNetworkTest  {
    
	@Test
    public void testTempNet() {
        TemporaryNetwork subsetter = new TemporaryNetwork();
        Network x = MockNetworkAttr.createNetworkAttr("X");
        assertTrue(subsetter.accept(x).isSuccess());
        Network xa = MockNetworkAttr.createNetworkAttr("XA");
        assertTrue(subsetter.accept(xa).isSuccess());
        Network ax = MockNetworkAttr.createNetworkAttr("AX");
        assertFalse(subsetter.accept(ax).isSuccess());
        Network other = MockNetworkAttr.createOtherNetworkAttr();
        assertTrue(subsetter.accept(other).isSuccess());
    }
}
