package edu.sc.seis.sod.subsetter.station;

import java.util.List;

import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.VestingNetworkDC;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkAccess;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkAttr;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkDC;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkFinder;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.NamedNetDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.source.network.NetworkFinder;

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


    
    private final class CountRetrieveStations extends NetworkFinder {

        public int callCount;
        
        public CountRetrieveStations() {
            super(FissuresNamingService.MOCK_DNS, NamedNetDC.EVERYBODY, 1);
            this.netDC = new VestingNetworkDC(FissuresNamingService.MOCK_DNS, NamedNetDC.EVERYBODY, new FissuresNamingService());
        }
        
        public  List<StationImpl> getStations(NetworkId net) {
            callCount++;
            return super.getStations(net);
        }
        
    }

    private static final TimeInterval FORTNIGHT = new TimeInterval(1,
                                                                   UnitImpl.FORTNIGHT);
    
    private static String mockNetName = MockNetworkAttr.createMultiSplendoredAttr().getName();

    public void testRefresh() throws ConfigurationException {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(mockNetName, FORTNIGHT);
        btv.accept(MockStation.createStation(), na);
        assertEquals(1, na.callCount);
        btv.accept(MockStation.createStation(), na);
        assertEquals(1, na.callCount);
    }

    public void testZeroRefreshTime() throws InterruptedException, ConfigurationException {
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

    public void testAcceptsAllStationsInAssignedNetwork() throws ConfigurationException {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(mockNetName, FORTNIGHT);
        List<StationImpl> stations = na.getStations(MockNetworkAttr.createMultiSplendoredAttr().getId());
        for (StationImpl sta : stations) {
            assertTrue(btv.accept(sta, na).isSuccess());
        }
    }

    public void testStationsNotInNetwork() throws ConfigurationException {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(mockNetName, FORTNIGHT);
        assertFalse(btv.accept(MockStation.createStation(), na).isSuccess());
        assertFalse(btv.accept(MockStation.createOtherStation(), na).isSuccess());
    }

    public void testBadVirtualName() throws ConfigurationException {
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
