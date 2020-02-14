package edu.sc.seis.sod.subsetter.origin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PassOriginTest  {

	@Test
    public void testAccept() throws Exception {
        assertTrue(new PassOrigin().accept(null, null, null).isSuccess());
    }
}