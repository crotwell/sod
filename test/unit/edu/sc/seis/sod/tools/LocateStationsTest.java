package edu.sc.seis.sod.tools;

import junit.framework.TestCase;
import org.apache.velocity.VelocityContext;
import com.martiansoftware.jsap.JSAPException;

public class LocateStationsTest extends TestCase {

    public void testDefaultServerArg() throws JSAPException {
        LocateStations ls = new LocateStations();
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("server"));
    }

    public void testDefaultStationArg() throws JSAPException {
        LocateStations ls = new LocateStations();
        VelocityContext vc = ls.getContext();
        assertFalse(vc.containsKey("stations"));
    }

    public void testSingleStationShortArg() throws JSAPException {
        LocateStations ls = new LocateStations(new String[] {"-s", "ANMO"});
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("stations"));
        Object[] codes = (Object[])vc.get("stations");
        assertEquals(1, codes.length);
        assertEquals("ANMO", codes[0]);
    }

    public void testMultipleStationLongArg() throws JSAPException {
        LocateStations ls = new LocateStations(new String[] {"--stations",
                                                             "CHICKENS,HORSES,COWS,PIGS,SHEEP"});
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("stations"));
        Object[] codes = (Object[])vc.get("stations");
        assertEquals(5, codes.length);
        assertEquals("CHICKENS", codes[0]);
    }

    public void testMultipleNetworkLongArg() throws JSAPException {
        LocateStations ls = new LocateStations(new String[] {"--networks",
                                                             "CHICKENS,HORSES,COWS,PIGS,SHEEP"});
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("networks"));
        Object[] codes = (Object[])vc.get("networks");
        assertEquals(5, codes.length);
        assertEquals("CHICKENS", codes[0]);
    }

    public void testDefaultBoxArea() throws JSAPException {
        LocateStations ls = new LocateStations(new String[] {});
        VelocityContext vc = ls.getContext();
        assertFalse(vc.containsKey("box"));
    }

    public void testSuppliedBoxArea() throws JSAPException {
        LocateStations ls = new LocateStations(new String[] {"-R", "12/32/32/12"});
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("box"));
    }

    public void testDefaultDonutArea() throws JSAPException {
        LocateStations ls = new LocateStations(new String[] {});
        VelocityContext vc = ls.getContext();
        assertFalse(vc.containsKey("donut"));
    }

    public void testSuppliedDonut() throws JSAPException {
        LocateStations ls = new LocateStations(new String[] {"--donut", "12/32/32/58"});
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("donut"));
    }
}
