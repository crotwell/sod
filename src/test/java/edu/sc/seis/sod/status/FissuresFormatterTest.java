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

    public void testFormatWithDirectories() {
        assertEquals("/2005.265.12/__.BHZ",
                     FissuresFormatter.filize("/2005.265.12/  .BHZ"));
        assertEquals("C:\\home\\_\\__.BHZ",
                     FissuresFormatter.filize("C:\\home\\:\\  .BHZ"));
        assertEquals("_\\__.BHZ", FissuresFormatter.filize(":\\  .BHZ"));
        assertEquals("12442/ham/cheese/__.BHZ",
                     FissuresFormatter.filize("12442/ham/cheese/  .BHZ"));
    }

    private Channel chan;
}