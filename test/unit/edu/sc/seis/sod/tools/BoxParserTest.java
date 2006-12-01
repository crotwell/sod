package edu.sc.seis.sod.tools;

import java.util.Map;
import junit.framework.TestCase;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.ParseException;

public class BoxParserTest extends TestCase {

    public void testSimpleBoxArea() throws JSAPException {
        BoxAreaParser sp = new BoxAreaParser();
        Map box = (Map)sp.parse("-24/43/22/23");
        assertEquals("-24", box.get("west"));
        assertEquals("43", box.get("east"));
        assertEquals("22", box.get("south"));
        assertEquals("23", box.get("north"));
    }

    public void testBadBox() throws JSAPException {
        BoxAreaParser sp = new BoxAreaParser();
        try {
            sp.parse("24/43/2223");
            fail("A box missing a slash should raise an exception!");
        } catch(ParseException pe) {
            assertTrue(pe.getMessage().contains("24/43/2223"));
        }
    }
}
