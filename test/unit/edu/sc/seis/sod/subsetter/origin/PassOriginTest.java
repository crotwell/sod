package edu.sc.seis.sod.subsetter.origin;

import junit.framework.TestCase;

public class PassOriginTest extends TestCase {

    public void testAccept() throws Exception {
        assertTrue(new PassOrigin().accept(null, null, null));
    }
}