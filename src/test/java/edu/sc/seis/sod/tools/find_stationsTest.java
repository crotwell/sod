package edu.sc.seis.sod.tools;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.velocity.VelocityContext;
import org.junit.jupiter.api.Test;

import com.martiansoftware.jsap.JSAPException;

public class find_stationsTest  {

	@Test
    public void testDefaultStationArg() throws JSAPException {
        find_stations ls = new find_stations();
        VelocityContext vc = ls.getContext();
        assertFalse(vc.containsKey("stations"));
    }

	@Test
    public void testSingleStationShortArg() throws JSAPException {
        find_stations ls = new find_stations(new String[] {"-s", "ANMO"});
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("stations"));
        Object[] codes = (Object[])vc.get("stations");
        assertEquals(1, codes.length);
        assertEquals("ANMO", codes[0]);
        assertTrue(vc.containsKey("needsStationAND"));
    }

	@Test
    public void testNeedsStationByDefault() throws JSAPException {
        find_stations ls = new find_stations();
        VelocityContext vc = ls.getContext();
        assertEquals(Boolean.TRUE, vc.get("needsStationAND"));
    }

	@Test
    public void testNeedsStationFalseWhenOutputIsNone() throws JSAPException {
        find_stations ls = new find_stations(new String[]{"-o", "none"});
        VelocityContext vc = ls.getContext();
        assertFalse(vc.containsKey("needsStationAND"));
    }


	@Test
    public void testMultipleStationLongArg() throws JSAPException {
        find_stations ls = new find_stations(new String[] {"--stations",
                                                             "CHICKENS,HORSES,COWS,PIGS,SHEEP"});
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("stations"));
        Object[] codes = (Object[])vc.get("stations");
        assertEquals(5, codes.length);
        assertEquals("CHICKENS", codes[0]);
        assertTrue(vc.containsKey("needsStationAND"));
    }

	@Test
    public void testMultipleNetworkLongArg() throws JSAPException {
        find_stations ls = new find_stations(new String[] {"--networks",
                                                             "CHICKENS,HORSES,COWS,PIGS,SHEEP"});
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("networks"));
        Object[] codes = (Object[])vc.get("networks");
        assertEquals(5, codes.length);
        assertEquals("CHICKENS", codes[0]);
    }

	@Test
    public void testDefaultBoxArea() throws JSAPException {
        find_stations ls = new find_stations(new String[] {});
        VelocityContext vc = ls.getContext();
        assertFalse( vc.containsKey("box"), "should not contain box");
    }

	@Test
    public void testSuppliedBoxArea() throws JSAPException {
        find_stations ls = new find_stations(new String[] {"-R", "12/32/32/12"});
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("box"));
        assertTrue(vc.containsKey("needsStationAND"));
    }

	@Test
    public void testDefaultDonutArea() throws JSAPException {
        find_stations ls = new find_stations(new String[] {});
        VelocityContext vc = ls.getContext();
        assertFalse(vc.containsKey("donut"));
    }

	@Test
    public void testSuppliedDonut() throws JSAPException {
        find_stations ls = new find_stations(new String[] {"--donut", "12/32/32/58"});
        VelocityContext vc = ls.getContext();
        assertTrue(vc.containsKey("donut"));
        assertTrue(vc.containsKey("needsStationAND"));
    }
}
