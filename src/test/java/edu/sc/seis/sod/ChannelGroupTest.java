package edu.sc.seis.sod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.channelGroup.SiteMatchRule;
import edu.sc.seis.sod.mock.station.MockChannel;
import edu.sc.seis.sod.mock.station.MockStation;
import edu.sc.seis.sod.model.station.ChannelGroup;


public class ChannelGroupTest {


    @Test
    public void testGroupBandGain() throws ConfigurationException {

        List<Channel> chanList = new ArrayList<Channel>();
        List<Channel> failures = new ArrayList<Channel>();
        Station mockStation = MockStation.createStation();
        chanList.add(MockChannel.createChannel(mockStation, "00", "BHZ"));
        chanList.add(MockChannel.createChannel(mockStation, "00", "BHN"));
        chanList.add(MockChannel.createChannel(mockStation, "00", "BHE"));
        chanList.add(MockChannel.createChannel(mockStation, "00", "HHZ"));
        chanList.add(MockChannel.createChannel(mockStation, "00", "HHN"));
        chanList.add(MockChannel.createChannel(mockStation, "00", "HHE"));
        ChannelGrouper cger = new ChannelGrouper();
        
        HashMap<String, List<Channel>> map = cger.groupByNetStaBandGain(chanList);
        for (String s : map.keySet()) {
            List<Channel> subList = map.get(s);
            assertEquals(3, subList.size());
        }
    }
    @Test
    public void testBAndHGroup() throws ConfigurationException {
        List<Channel> chanList = new ArrayList<Channel>();
        List<Channel> failures = new ArrayList<Channel>();
        Station mockStation = MockStation.createStation();
        chanList.add(MockChannel.createChannel(mockStation, "00", "BHZ"));
        chanList.add(MockChannel.createChannel(mockStation, "00", "BHN"));
        chanList.add(MockChannel.createChannel(mockStation, "00", "BHE"));
        chanList.add(MockChannel.createChannel(mockStation, "00", "HHZ"));
        chanList.add(MockChannel.createChannel(mockStation, "00", "HHN"));
        chanList.add(MockChannel.createChannel(mockStation, "00", "HHE"));
        ChannelGrouper cger = new ChannelGrouper();
        
        List<ChannelGroup> cgList = cger.group(chanList, failures);
        assertEquals( 0, failures.size());
        assertEquals( 2, cgList.size());
        for (ChannelGroup cg : cgList) {
            assertEquals(cg.getChannel1().getChannelCode().substring(0, 1), cg.getChannel2().getChannelCode().substring(0, 1)); 
            assertEquals(cg.getChannel1().getChannelCode().substring(0, 1), cg.getChannel3().getChannelCode().substring(0, 1));     
        }
    }
    @Test
    public void testXK04Group() throws ConfigurationException {
        List<Channel> chanList = new ArrayList<Channel>();
        List<Channel> failures = new ArrayList<Channel>();
        Station mockStation = MockStation.createStation();
        chanList.add(MockChannel.createChannel(mockStation, "30", "BHZ"));
        chanList.add(MockChannel.createChannel(mockStation, "31", "BHN"));
        chanList.add(MockChannel.createChannel(mockStation, "32", "BHE"));
        chanList.add(MockChannel.createChannel(mockStation, "00", "LHZ"));
        chanList.add(MockChannel.createChannel(mockStation, "01", "LHN"));
        chanList.add(MockChannel.createChannel(mockStation, "02", "LHE"));
        ChannelGrouper cger = new ChannelGrouper();
        
        List<ChannelGroup> cgList = cger.group(chanList, failures);
        assertEquals( 0, failures.size());
        assertEquals( 2, cgList.size());
    }
    
    @Test
    public void testThreeCharRule() {
        List<Channel> chanList = new ArrayList<Channel>();
        List<Channel> failures = new ArrayList<Channel>();
        Station mockStation = MockStation.createStation();
        chanList.add(MockChannel.createChannel(mockStation, "00", "BHZ"));
        SiteMatchRule threeC = new SiteMatchRule("ZNE");

        assertEquals( 1, chanList.size());
        
        List<ChannelGroup> groupList = threeC.acceptable(chanList, failures);

        assertEquals( 0, groupList.size());
        assertEquals( 1, failures.size());
    }
}
