/**
 * StatusTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import edu.sc.seis.sod.model.status.Status;
import junit.framework.TestCase;

public class StatusTest extends TestCase {

    public void testGetFromInt(){
        for (int i = 0; i < Status.ALL.length; i++) {
            for (int j = 0; j < Status.ALL[i].length; j++) {
                assertEquals("Check status for "+i+", "+j,
                             Status.ALL[i][j],
                             Status.getFromShort(Status.ALL[i][j].getAsShort()));
            }
        }
    }
}

