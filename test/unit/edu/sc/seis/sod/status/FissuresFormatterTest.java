/**
 * FissuresFormatterTest.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.XMLConfigUtil;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import junit.framework.TestCase;

public class FissuresFormatterTest extends TestCase{

    public FissuresFormatterTest(String name){ super(name); }

    public void setUp(){
        chan = MockChannel.createChannel();
    }

    public void testNetworkYear(){
        assertEquals("XX1969", FissuresFormatter.formatNetworkYear(chan.get_id().network_id));
    }

    private Channel chan;
}

