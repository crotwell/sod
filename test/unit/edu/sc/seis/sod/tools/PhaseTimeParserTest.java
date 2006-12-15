package edu.sc.seis.sod.tools;

import java.util.Map;
import com.martiansoftware.jsap.ParseException;
import junit.framework.TestCase;

public class PhaseTimeParserTest extends TestCase {

    public void testBasicPhase() throws ParseException {
        PhaseTimeParser ptp = new PhaseTimeParser();
        Map phase = (Map)ptp.parse("-12.5ttp");
        assertEquals("-12.5", phase.get("offset"));
        assertEquals("ttp", phase.get("name"));
    }

    public void testOriginDefault() throws ParseException {
        PhaseTimeParser ptp = new PhaseTimeParser();
        Map phase = (Map)ptp.parse("2origin");
        assertEquals("2", phase.get("offset"));
        assertEquals("origin", phase.get("name"));
    }

    public void testBadOffset() throws ParseException {
        PhaseTimeParser ptp = new PhaseTimeParser();
        try {
            ptp.parse("+7origin");
            fail("SHOULDA THROWN PARSE EXCEPTION");
        } catch(ParseException pe) {
           assertTrue(pe.getMessage().contains("+7origin"));
        }
    }
}
