package edu.sc.seis.sod.status.waveFormArm;
import edu.sc.seis.sod.subsetter.waveFormArm.*;

import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.InvalidDatabaseStateException;
import edu.sc.seis.sod.XMLConfigUtil;
import edu.sc.seis.sod.database.Status;
import edu.sc.seis.sod.database.waveform.EventChannelCondition;
import edu.sc.seis.sod.status.waveFormArm.WaveformEventGroup;
import java.io.IOException;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.xml.sax.SAXException;

public class WaveformEventGroupTest extends TestCase{
    public WaveformEventGroupTest(String name){ super(name); }
    
    public void setUp() throws NotFound, SQLException{
        ect = new WaveformEventGroup();
        simpleECP = MockECP.getECP(MockEventAccessOperations.createEvent());
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
            ect = new WaveformEventGroup(XMLConfigUtil.parse("<events><channelCount><FAILURE/><SUCCESS/></channelCount>\n</events>"));
        } catch (SAXException e) {} catch (ParserConfigurationException e) {} catch (IOException e) {}
        ect.update(rejectify(MockECP.getECP(MockChannel.createChannel())));
        ect.update(simpleECP);
        assertEquals("2\n", ect.getResult());
    }
    
    private EventChannelPair generateSecondChannelECP() throws NotFound, SQLException {
        return successify(MockECP.getECP(MockChannel.createOtherChan()));
    }
    
    private EventChannelPair generateDiffEventECP() throws NotFound, SQLException{
        return successify(MockECP.getECP(MockEventAccessOperations.createFallEvent()));
    }
    
    private EventChannelPair successify(EventChannelPair ecp) throws NotFound, SQLException{
        ecp.update("Testing Success", EventChannelCondition.SUCCESS);
        return ecp;
    }
    
    private EventChannelPair rejectify(EventChannelPair ecp) throws NotFound, SQLException {
        ecp.update("Testing Reject", EventChannelCondition.FAILURE);
        return ecp;
    }
    
    private EventChannelPair simpleECP;
    
    private WaveformEventGroup ect;
}
