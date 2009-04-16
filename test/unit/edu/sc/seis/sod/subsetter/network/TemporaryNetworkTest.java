package edu.sc.seis.sod.subsetter.network;

import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkAttr;


public class TemporaryNetworkTest extends TestCase {
    
    public void testTempNet() {
        TemporaryNetwork subsetter = new TemporaryNetwork();
        NetworkAttr x = MockNetworkAttr.createNetworkAttr();
        x.get_id().network_code = "X";
        assertTrue(subsetter.accept(x).isSuccess());
        NetworkAttr xa = MockNetworkAttr.createNetworkAttr();
        xa.get_id().network_code = "XA";
        assertTrue(subsetter.accept(xa).isSuccess());
        NetworkAttr ax = MockNetworkAttr.createNetworkAttr();
        ax.get_id().network_code = "AX";
        assertFalse(subsetter.accept(ax).isSuccess());
        NetworkAttr other = MockNetworkAttr.createOtherNetworkAttr();
        assertFalse(subsetter.accept(other).isSuccess());
    }
}
