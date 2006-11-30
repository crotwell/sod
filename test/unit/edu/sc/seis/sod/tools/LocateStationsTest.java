package edu.sc.seis.sod.tools;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.velocity.VelocityContext;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.ParseException;

public class LocateStationsTest extends TestCase {

    public void testDefaultServerArg() throws JSAPException {
        LocateStations ls = new LocateStations();
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("server"));
        Map server = (HashMap)vc.get("server");
        checkServerMap(server);
    }

    public void testDefaultStationArg() throws JSAPException {
        LocateStations ls = new LocateStations();
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("stations"));
        Object[] codes = (Object[])vc.get("stations");
        assertEquals(0, codes.length);
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

    public void testDefaultServerParser() throws ParseException {
        LocateStations.ServerParser sp = new LocateStations.ServerParser();
        checkServerMap((Map)sp.parse("edu/iris/dmc/IRIS_NetworkDC"));
    }

    public void testBadServerParser() {
        LocateStations.ServerParser sp = new LocateStations.ServerParser();
        try {
            checkServerMap((Map)sp.parse("IRIS_NetworkDC"));
            fail("A server without a DNS should raise an exception!");
        } catch(ParseException pe) {
            assertTrue(pe.getMessage().contains("IRIS_NetworkDC"));
        }
    }

    private void checkServerMap(Map server) {
        assertEquals("edu/iris/dmc", server.get("dns"));
        assertEquals("IRIS_NetworkDC", server.get("name"));
    }
}
