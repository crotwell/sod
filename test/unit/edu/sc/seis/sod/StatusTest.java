/**
 * StatusTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import junit.framework.TestCase;

public class StatusTest extends TestCase {

    public void testGetFromInt() throws NoSuchFieldException{
        for (int i = 0; i < Status.ALL.length; i++) {
            for (int j = 0; j < Status.ALL[i].length; j++) {
                System.out.println("Check status for "+i+", "+j+ " "+
                                       Status.ALL[i][j].getAsShort() +" "
                                       + Status.ALL[i][j].getStage().getVal()+
                                       (Status.ALL[i][j].getStage().getVal()<<8)+
                                       ((Status.ALL[i][j].getStage().getVal()<<8)>>>8));
                assertEquals("Check status for "+i+", "+j,
                             Status.ALL[i][j],
                             Status.getFromShort(Status.ALL[i][j].getAsShort()));
            }
        }
    }
}

