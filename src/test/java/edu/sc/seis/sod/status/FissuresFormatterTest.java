/**
 * FissuresFormatterTest.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.status;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.mock.station.MockChannel;
import edu.sc.seis.sod.mock.station.MockStation;
import edu.sc.seis.sod.velocity.network.VelocityStation;
import junit.framework.TestCase;

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

    public void testOneLineAndClean() {
        Station sta = MockStation.createStation();
        sta.setName("  Long name\nwith\r\nnewlines  ");
        VelocityStation vsta = new VelocityStation(sta);
        assertEquals("Long name with newlines", vsta.getName());
    }
    
    private Channel chan;
}