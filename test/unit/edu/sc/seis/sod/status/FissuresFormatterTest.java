/**
 * FissuresFormatterTest.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.status;

import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;

public class FissuresFormatterTest extends TestCase {

    public FissuresFormatterTest(String name) {
        super(name);
    }

    public void setUp() {
        chan = MockChannel.createChannel();
    }

    public void testFormatNetwork() {
        assertEquals("XX70",
                     FissuresFormatter.formatNetwork(chan.get_id().network_id));
    }

    private Channel chan;
}