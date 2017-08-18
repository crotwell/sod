package edu.sc.seis.sod.subsetter.station;

import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.mock.MockNetworkSource;
import edu.sc.seis.sod.mock.station.MockNetworkAttr;
import edu.sc.seis.sod.mock.station.MockStation;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import junit.framework.TestCase;

public class BelongsToVirtualTest extends TestCase {
 
    private final class CountRetrieveStations extends MockNetworkSource {

        public int callCount;
        
        @Override
        public  List<? extends Station> getStations(Network net) {
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
        List<? extends Station> stations = na.getStations(MockNetworkAttr.createMultiSplendoredAttr());
        for (Station sta : stations) {
            assertTrue(btv.accept(sta, na).isSuccess());
        }
    }

    public void testStationsNotInNetwork() throws Exception {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(mockNetName, FORTNIGHT);
        assertFalse(btv.accept(MockStation.createStation(), na).isSuccess());
        assertFalse(btv.accept(MockStation.createOtherStation(), na).isSuccess());
    }
}
