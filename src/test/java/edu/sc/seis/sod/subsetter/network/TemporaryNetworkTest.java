package edu.sc.seis.sod.subsetter.network;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.mock.station.MockNetworkAttr;
import junit.framework.TestCase;


public class TemporaryNetworkTest extends TestCase {
    
    public void testTempNet() {
        TemporaryNetwork subsetter = new TemporaryNetwork();
        Network x = MockNetworkAttr.createNetworkAttr();
        x.get_id().network_code = "X";
        assertTrue(subsetter.accept(x).isSuccess());
        Network xa = MockNetworkAttr.createNetworkAttr();
        xa.get_id().network_code = "XA";
        assertTrue(subsetter.accept(xa).isSuccess());
        Network ax = MockNetworkAttr.createNetworkAttr();
        ax.get_id().network_code = "AX";
        assertFalse(subsetter.accept(ax).isSuccess());
        Network other = MockNetworkAttr.createOtherNetworkAttr();
        assertFalse(subsetter.accept(other).isSuccess());
    }
}
