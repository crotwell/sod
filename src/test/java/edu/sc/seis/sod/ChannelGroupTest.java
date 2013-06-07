package edu.sc.seis.sod;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import com.mchange.util.AssertException;

import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;
import edu.sc.seis.sod.channelGroup.SiteMatchRule;


public class ChannelGroupTest {

    static {BasicConfigurator.configure();}
    
    @Test
    public void testXK04Group() throws ConfigurationException {
        List<ChannelImpl> chanList = new ArrayList<ChannelImpl>();
        List<ChannelImpl> failures = new ArrayList<ChannelImpl>();
        StationImpl mockStation = MockStation.createStation();
        chanList.add(MockChannel.createChannel(mockStation, "30", "BHZ"));
        chanList.add(MockChannel.createChannel(mockStation, "31", "BHN"));
        chanList.add(MockChannel.createChannel(mockStation, "32", "BHE"));
        chanList.add(MockChannel.createChannel(mockStation, "00", "LHZ"));
        chanList.add(MockChannel.createChannel(mockStation, "01", "LHN"));
        chanList.add(MockChannel.createChannel(mockStation, "02", "LHE"));
        ChannelGrouper cger = new ChannelGrouper();
        
        List<ChannelGroup> cgList = cger.group(chanList, failures);
        assertEquals("failures", 0, failures.size());
        assertEquals("grouped", 1, cgList.size());
    }
    
    @Test
    public void testThreeCharRule() {
        List<ChannelImpl> chanList = new ArrayList<ChannelImpl>();
        List<ChannelImpl> failures = new ArrayList<ChannelImpl>();
        StationImpl mockStation = MockStation.createStation();
        chanList.add(MockChannel.createChannel(mockStation, "30", "BHZ"));
        SiteMatchRule threeC = new SiteMatchRule("ZNE");

        assertEquals("initial", 1, chanList.size());
        
        List<ChannelGroup> groupList = threeC.acceptable(chanList, failures);

        assertEquals("grouped", 0, groupList.size());
        assertEquals("failure", 1, failures.size());
    }
}
