package edu.sc.seis.sod.tools;

import java.util.Map;

import junit.framework.TestCase;

import com.martiansoftware.jsap.ParseException;

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
            assertTrue(pe.getMessage().indexOf("IRIS_NetworkDC") != -1);
        }
    }

    private void checkServerMap(Map server) {
        assertEquals("edu/iris/dmc", server.get("dns"));
        assertEquals("IRIS_NetworkDC", server.get("name"));
    }
}
