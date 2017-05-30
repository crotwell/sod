package edu.sc.seis.sod.subsetter.station;

import java.util.List;

import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.hibernate.NetworkNotFound;
import edu.sc.seis.sod.mock.MockNetworkSource;
import edu.sc.seis.sod.mock.station.MockNetworkAttr;
import edu.sc.seis.sod.mock.station.MockStation;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.station.CacheNetworkAccess;
import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.model.station.StationImpl;
import junit.framework.TestCase;

public class BelongsToVirtualTest extends TestCase {

    private final class ExplodesOnRetByName  extends NetworkFinder {

        public ExplodesOnRetByName() {
            super(FissuresNamingService.MOCK_DNS, NamedNetDC.EVERYBODY, 1);
            this.netDC = new VestingNetworkDC(FissuresNamingService.MOCK_DNS, NamedNetDC.EVERYBODY, new FissuresNamingService());
        }
        
        @Override
        public List<CacheNetworkAccess> getNetworkByName(String name)
                throws NetworkNotFound {
            throw new NetworkNotFound("You lose!");
        }
    }


    
    private final class CountRetrieveStations extends MockNetworkSource {

        public int callCount;
        
        @Override
        public  List<? extends StationImpl> getStations(NetworkAttrImpl net) {
            callCount++;
            return super.getStations(net);
        }
        
    }

    private static final TimeInterval FORTNIGHT = new TimeInterval(1,
                                                                   UnitImpl.FORTNIGHT);
    
    private static String mockNetName = MockNetworkAttr.createMultiSplendoredAttr().getName();

    public void testRefresh() throws Exception {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(mockNetName, FORTNIGHT);
        btv.accept(MockStation.createStation(), na);
        assertEquals(1, na.callCount);
        btv.accept(MockStation.createStation(), na);
        assertEquals(1, na.callCount);
    }

    public void testZeroRefreshTime() throws InterruptedException, Exception {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(mockNetName,
                                                    new TimeInterval(0,
                                                                     UnitImpl.SECOND));
        btv.accept(MockStation.createStation(), na);
        assertEquals(1, na.callCount);
        Thread.sleep(1);
        btv.accept(MockStation.createStation(), na);
        assertEquals(2, na.callCount);
    }

    public void testAcceptsAllStationsInAssignedNetwork() throws Exception {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(mockNetName, FORTNIGHT);
        List<? extends StationImpl> stations = na.getStations(MockNetworkAttr.createMultiSplendoredAttr());
        for (StationImpl sta : stations) {
            assertTrue(btv.accept(sta, na).isSuccess());
        }
    }

    public void testStationsNotInNetwork() throws Exception {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(mockNetName, FORTNIGHT);
        assertFalse(btv.accept(MockStation.createStation(), na).isSuccess());
        assertFalse(btv.accept(MockStation.createOtherStation(), na).isSuccess());
    }

    public void testBadVirtualName() throws Exception {
        try {
            ExplodesOnRetByName na = new ExplodesOnRetByName();
            BelongsToVirtual btv = new BelongsToVirtual(mockNetName, FORTNIGHT);
            btv.accept(MockStation.createStation(), na);
            fail();
        } catch(UserConfigurationException uce) {
            assertTrue(true);
        }
    }
}
