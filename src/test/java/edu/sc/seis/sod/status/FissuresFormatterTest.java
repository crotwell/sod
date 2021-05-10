/**
 * FissuresFormatterTest.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.mock.station.MockChannel;
import edu.sc.seis.sod.mock.station.MockStation;
import edu.sc.seis.sod.velocity.network.VelocityStation;

public class FissuresFormatterTest  {


	@BeforeEach
    public void setUp() {
        chan = MockChannel.createChannel();
    }

    @Test
    public void testFormatNetwork() {
        assertEquals("XX70",
                     FissuresFormatter.formatNetwork(chan.getNetwork()));
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

    @Test
    public void testOneLineAndClean() {
        Station sta = MockStation.createStation();
        VelocityStation vsta = new VelocityStation(sta);
        assertEquals("Long name with newlines", vsta.getName());
    }
    
    private Channel chan;
}