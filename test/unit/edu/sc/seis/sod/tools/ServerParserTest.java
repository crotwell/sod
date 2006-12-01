package edu.sc.seis.sod.tools;

import java.util.Map;
import com.martiansoftware.jsap.ParseException;
import junit.framework.TestCase;

public class ServerParserTest extends TestCase {

    public void testDefaultServerParser() throws ParseException {
        ServerParser sp = new ServerParser();
        checkServerMap((Map)sp.parse("edu/iris/dmc/IRIS_NetworkDC"));
    }

    public void testBadServerParser() {
        ServerParser sp = new ServerParser();
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
