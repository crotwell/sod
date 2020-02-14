package edu.sc.seis.sod.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.martiansoftware.jsap.ParseException;

public class TimeParserTest  {

	@BeforeEach
    public void setUp() {
        floor = new TimeParser(false);
        ceiling  = new TimeParser(true);
    }

	@Test
    public void testBasicTime() throws ParseException {
        assertEquals("2006-11-19T00:00:00.000Z", floor.parse("2006-11-19"));
        assertEquals("2006-11-19T23:59:59.999Z", ceiling.parse("2006-11-19"));
    }

	@Test
    public void testFullTime() throws ParseException {
        assertEquals("2002-07-02T12:30:15.000Z",
                     floor.parse("2002-07-02T12:30:15"));
        assertEquals("2002-07-02T12:30:15.999Z",
                     ceiling.parse("2002-07-02T12:30:15"));
    }

	@Test
    public void testCompactTime() throws ParseException {
        assertEquals("2002-07-02T12:30:15.000Z", floor.parse("20020702123015"));
        assertEquals("2002-07-02T12:30:15.999Z", ceiling.parse("20020702123015"));
    }

	@Test
    public void testBadTime() {
        try {
            System.out.println(floor.parse("2006-111-19"));
            assertTrue(false, "Shouldn't be able to parse a time like that");
        } catch(ParseException pe) {
            assertTrue(pe.getMessage().indexOf("2006-111-19") != -1);
        }
    }

	@Test
    public void testOnlyYear() throws ParseException {
        assertEquals("2006-01-01T00:00:00.000Z", floor.parse("2006"));
        assertEquals("2006-12-31T23:59:59.999Z", ceiling.parse("2006"));
    }

	@Test
    public void testNow() throws ParseException {
        assertEquals("<now/>", floor.parse("now"));
    }

    private TimeParser floor, ceiling;
}
