package edu.sc.seis.sod.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.martiansoftware.jsap.ParseException;

public class PhaseTimeParserTest  {

    @Test
    public void testBasicPhase() throws ParseException {
        Map phase = (Map)ptp.parse("-12.5ttp");
        assertEquals("-12.5", phase.get("offset"));
        assertEquals("ttp", phase.get("name"));
    }

    @Test
    public void testOriginDefault() throws ParseException {
        Map phase = (Map)ptp.parse("2origin");
        assertEquals("2", phase.get("offset"));
        assertEquals("origin", phase.get("name"));
    }

    @Test
    public void testPositiveOffset() throws ParseException {
        Map phase = (Map)ptp.parse("+7origin");
        assertEquals("7", phase.get("offset"));
        assertEquals("origin", phase.get("name"));
    }

    @Test
    public void testNoPhase() throws ParseException {
        try {
            ptp.parse("2006");
            assertTrue(false, "PhaseTimeParsing should fail without a phase given");
        } catch(ParseException pe) {
            assertTrue(pe.getMessage().indexOf("2006") != -1);
        }
    }

    @Test
    public void testNoOffset() throws ParseException {
        try {
            ptp.parse("ttp");
            assertTrue(false, "PhaseTimeParsing should fail without a offset given");
        } catch(ParseException pe) {
            assertTrue(pe.getMessage().indexOf("ttp") != -1);
        }
    }

    PhaseTimeParser ptp = new PhaseTimeParser();
}
