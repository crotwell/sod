package edu.sc.seis.sod.subsetter.station;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.mock.MockNetworkSource;
import edu.sc.seis.sod.mock.station.MockNetworkAttr;
import edu.sc.seis.sod.mock.station.MockStation;

public class BelongsToVirtualTest  {
 
    private final class CountRetrieveStations extends MockNetworkSource {

        public int callCount;
        
        @Override
        public  List<? extends Station> getStations(Network net) {
            callCount++;
            return super.getStations(net);
        }
        
    }

    
    private static String mockNetName = MockNetworkAttr.createMultiSplendoredAttr().getNetworkId();

    @Test
    public void testRefresh() throws Exception {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(mockNetName, TimeUtils.ONE_FORTNIGHT);
        btv.accept(MockStation.createStation(), na);
        assertEquals(1, na.callCount);
        btv.accept(MockStation.createStation(), na);
        assertEquals(1, na.callCount);
    }

    @Test
    public void testZeroRefreshTime() throws InterruptedException, Exception {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(mockNetName,
                                                    Duration.ofSeconds(0));
        btv.accept(MockStation.createStation(), na);
        assertEquals(1, na.callCount);
        Thread.sleep(1);
        btv.accept(MockStation.createStation(), na);
        assertEquals(2, na.callCount);
    }

    @Test
    public void testAcceptsAllStationsInAssignedNetwork() throws Exception {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(mockNetName, TimeUtils.ONE_FORTNIGHT);
        List<? extends Station> stations = na.getStations(MockNetworkAttr.createMultiSplendoredAttr());
        for (Station sta : stations) {
            assertTrue(btv.accept(sta, na).isSuccess());
        }
    }

    @Test
    public void testStationsNotInNetwork() throws Exception {
        CountRetrieveStations na = new CountRetrieveStations();
        BelongsToVirtual btv = new BelongsToVirtual(mockNetName, TimeUtils.ONE_FORTNIGHT);
        assertFalse(btv.accept(MockStation.createStation(), na).isSuccess());
        assertFalse(btv.accept(MockStation.createOtherStation(), na).isSuccess());
    }
}
