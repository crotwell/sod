package edu.sc.seis.sod.subsetter.channel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.mock.station.MockChannel;
import edu.sc.seis.sod.mock.station.MockStation;

public class TestChannelCode  {

	@Test
    public void testStraightUpBHZ() throws UserConfigurationException {
        cc = new ChannelCode("BHZ");
        assertAccepted(BHZ);
        assertRejected(BHE);
        assertRejected(LHZ);
        assertRejected(LLN);
    }

	@Test
    public void testLowerCaseBHZ() throws UserConfigurationException {
        cc = new ChannelCode("bhz");
        assertAccepted(BHZ);
        assertRejected(BHE);
        assertRejected(LHZ);
        assertRejected(LLN);
    }

	@Test
    public void testBHQuestion() throws UserConfigurationException {
        doBHWild("?");
    }

	@Test
    public void testBHStar() throws UserConfigurationException {
        doBHWild("*");
    }

    public void doBHWild(String lastChar) throws UserConfigurationException {
        cc = new ChannelCode("BH"+lastChar);
        assertAccepted(BHZ);
        assertAccepted(BHE);
        assertRejected(LHZ);
        assertRejected(LLN);
    }

	@Test
    public void testStarhStar() throws UserConfigurationException {
        cc = new ChannelCode("*h*");
        assertAccepted(BHZ);
        assertAccepted(BHE);
        assertAccepted(LHZ);
        assertRejected(LLN);
    }

	@Test
    public void testStar() throws UserConfigurationException {
        cc = new ChannelCode("*");
        assertAccepted(BHZ);
        assertAccepted(BHE);
        assertAccepted(LHZ);
        assertAccepted(LLN);
    }

	@Test
    public void testBStar() throws UserConfigurationException {
        cc = new ChannelCode("B*");
        assertAccepted(BHZ);
        assertAccepted(BHE);
        assertRejected(LHZ);
        assertRejected(LLN);
    }

	@Test
    public void testStarLStar() throws UserConfigurationException {
        cc = new ChannelCode("*L*");
        assertRejected(BHZ);
        assertRejected(BHE);
        assertRejected(LHZ);
        assertAccepted(LLN);
    }

	@Test
    public void testTooFewCharsNotEnoughStars() {
        try {
            cc = new ChannelCode("AB");
            assertTrue(false, "'AB' doesn't completly specify a channel code so it should provke a UserConfigurationException");
        } catch(UserConfigurationException uce) {}
    }

    private void assertAccepted(Channel chan) {
        assertTrue(cc.accept(chan, null).isSuccess());
    }

    private void assertRejected(Channel chan) {
        assertFalse(cc.accept(chan, null).isSuccess());
    }

    private static final Channel BHZ = MockChannel.createChannel();

    private static final Channel BHE = MockChannel.createEastChannel();

    private static final Channel LHZ = MockChannel.createChannel(MockStation.createStation(), "00", "LHZ");
    
    private static final Channel LLN = MockChannel.createChannel(MockStation.createStation(), "00", "LLN");

    private ChannelCode cc;
}
