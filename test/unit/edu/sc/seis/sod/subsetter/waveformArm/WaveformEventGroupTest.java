package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.InvalidDatabaseStateException;
import edu.sc.seis.sod.XMLConfigUtil;
import edu.sc.seis.sod.database.Status;
import edu.sc.seis.sod.subsetter.MockFissures;
import edu.sc.seis.sod.subsetter.waveFormArm.WaveformEventGroup;
import junit.framework.TestCase;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class WaveformEventGroupTest extends TestCase{
    public WaveformEventGroupTest(String name){ super(name); }

    public void setUp(){
        ect = new WaveformEventGroup();
        simpleECP = MockECP.getECP(MockFissures.createEvent());
        successify(simpleECP);
    }

    public void testNoChannels(){
        assertEquals("", ect.getResult());
    }

    public void testUpdateSingleChannel() throws Exception {
        ect.update(simpleECP);
        assertEquals("1\n", ect.getResult());
    }

    public void testUpdateSecondChannel() throws Exception {
        ect.update(simpleECP);
        ect.update(generateSecondChannelECP());
        assertEquals("2\n", ect.getResult());
    }

    public void testAddSecondEvent() throws Exception {
        ect.update(simpleECP);
        ect.update(generateDiffEventECP());
        assertEquals("1\n1\n", ect.getResult());
    }

    public void testComplexConfig() throws Exception {
        try {
            ect = new WaveformEventGroup(XMLConfigUtil.parse("<events><channelCount/>    <eventLabel><feRegionName/></eventLabel><sorting><time order=\"reverse\"/></sorting>\n</events>"));
        } catch (SAXException e) {} catch (ParserConfigurationException e) {} catch (IOException e) {}
        ect.update(simpleECP);
        ect.update(generateDiffEventECP());
        ect.update(generateSecondChannelECP());
        assertEquals("1    GERMANY\n2    CENTRAL ALASKA\n", ect.getResult());
    }

    public void testRejectMonitor() throws Exception {
        try{
            ect = new WaveformEventGroup(XMLConfigUtil.parse("<events><channelCount><COMPLETE_REJECT/><COMPLETE_SUCCESS/></channelCount>\n</events>"));
        } catch (SAXException e) {} catch (ParserConfigurationException e) {} catch (IOException e) {}
        ect.update(rejectify(MockECP.getECP(MockFissures.createChannel())));
        ect.update(simpleECP);
        assertEquals("2\n", ect.getResult());
    }

    private EventChannelPair generateSecondChannelECP() {
        return successify(MockECP.getECP(MockFissures.createOtherChan()));
    }

    private EventChannelPair generateDiffEventECP(){
        return successify(MockECP.getECP(MockFissures.createFallEvent()));
    }

    private EventChannelPair successify(EventChannelPair ecp){
        try {
            ecp.update("Testing Success", Status.COMPLETE_SUCCESS);
            return ecp;
        } catch (InvalidDatabaseStateException e) {
            throw new IllegalArgumentException("MockECP shouldn't throw an InvalidDatabaseState Exception");
        }
    }

    private EventChannelPair rejectify(EventChannelPair ecp) {
        try {
            ecp.update("Testing Reject", Status.COMPLETE_REJECT);
            return ecp;
        } catch (InvalidDatabaseStateException e) {
            throw new IllegalArgumentException("MockECP shouldn't throw an InvalidDatabaseState Exception");
        }
    }

    private EventChannelPair simpleECP;

    private WaveformEventGroup ect;
}
