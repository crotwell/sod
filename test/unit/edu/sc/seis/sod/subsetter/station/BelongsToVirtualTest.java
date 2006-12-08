package edu.sc.seis.sod.subsetter.station;

import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkAccess;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkFinder;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.UserConfigurationException;

public class BelongsToVirtualTest extends TestCase {

    private final class ExplodesOnRetByName extends MockNetworkFinder {

        public NetworkAccess[] retrieve_by_name(String name)
                throws NetworkNotFound {
            throw new NetworkNotFound("You lose!");
        }
    }

    private final class CountRetrieveStations extends MockNetworkAccess {

        public int callCount;

        public Station[] retrieve_stations() {
            callCount++;
            return super.retrieve_stations();
        }
    }

    private static final TimeInterval FORTNIGHT = new TimeInterval(1,
                                                                   UnitImpl.FORTNIGHT);

    public void testRefresh() {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(na, FORTNIGHT);
        btv.accept(MockStation.createStation(), na);
        assertEquals(1, na.callCount);
        btv.accept(MockStation.createStation(), na);
        assertEquals(1, na.callCount);
    }

    public void testZeroRefreshTime() throws InterruptedException {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(na,
                                                    new TimeInterval(0,
                                                                     UnitImpl.SECOND));
        btv.accept(MockStation.createStation(), na);
        assertEquals(1, na.callCount);
        Thread.sleep(1);
        btv.accept(MockStation.createStation(), na);
        assertEquals(2, na.callCount);
    }

    public void testAcceptsAllStationsInAssignedNetwork() {
        NetworkAccess na = MockNetworkAccess.createNetworkAccess();
        BelongsToVirtual btv = new BelongsToVirtual(na, FORTNIGHT);
        Station[] stations = na.retrieve_stations();
        for(int i = 0; i < stations.length; i++) {
            assertTrue(btv.accept(stations[i], na).isSuccess());
        }
    }

    public void testStationsNotInNetwork() {
        NetworkAccess na = MockNetworkAccess.createNetworkAccess();
        BelongsToVirtual btv = new BelongsToVirtual(na, FORTNIGHT);
        Station[] stations = MockStation.createMultiSplendoredStations();
        for(int i = 0; i < stations.length; i++) {
            assertFalse(btv.accept(stations[i], na).isSuccess());
        }
    }

    public void testBadVirtualName() throws ConfigurationException {
        try {
            new BelongsToVirtual(new ExplodesOnRetByName(),
                                 "ALL_NAMES_THROW_NETWORK_NOT_FOUND");
            fail();
        } catch(UserConfigurationException uce) {
            assertTrue(true);
        }
    }
}
