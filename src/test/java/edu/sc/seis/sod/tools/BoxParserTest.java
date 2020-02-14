package edu.sc.seis.sod.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.ParseException;

import edu.sc.seis.seisFile.client.BoxAreaParser;

public class BoxParserTest  {

	@Test
    public void testSimpleBoxArea() throws JSAPException {
        BoxAreaParser sp = new BoxAreaParser();
        Map box = (Map)sp.parse("-24/43/22/23");
        assertEquals("-24", box.get("west"));
        assertEquals("43", box.get("east"));
        assertEquals("22", box.get("south"));
        assertEquals("23", box.get("north"));
    }

	@Test
    public void testBadBox() throws JSAPException {
        BoxAreaParser sp = new BoxAreaParser();
        try {
            sp.parse("24/43/2223");
            assertTrue(false, "A box missing a slash should raise an exception!");
        } catch(ParseException pe) {
            assertTrue(pe.getMessage().indexOf("24/43/2223") != -1);
        }
    }
    
	@Test
    public void testDecimalBox() throws ParseException{
        BoxAreaParser sp = new BoxAreaParser();
        Map box = (Map)sp.parse("-122.5/-117.5/36.25/41.25");
        assertEquals("-122.5", box.get("west"));
        assertEquals("-117.5", box.get("east"));
        assertEquals("36.25", box.get("south"));
        assertEquals("41.25", box.get("north"));
    }
}
