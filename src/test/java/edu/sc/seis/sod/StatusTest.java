/**
 * StatusTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import edu.sc.seis.sod.model.status.Status;
import junit.framework.TestCase;

public class StatusTest  {

	@Test
    public void testGetFromInt(){
        for (int i = 0; i < Status.ALL.length; i++) {
            for (int j = 0; j < Status.ALL[i].length; j++) {
                assertEquals(Status.ALL[i][j],
                             Status.getFromShort(Status.ALL[i][j].getAsShort()),
                             "Check status for "+i+", "+j);
            }
        }
    }
}

