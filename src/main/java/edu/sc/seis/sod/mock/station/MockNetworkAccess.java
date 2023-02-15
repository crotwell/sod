/**
 * MyNetworkAccess.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.mock.station;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.StationId;
import edu.sc.seis.sod.model.station.StationIdUtil;

public class MockNetworkAccess  {

    public static MockNetworkAccess createNetworkAccess() {
        return new MockNetworkAccess();
    }

    public static MockNetworkAccess createOtherNetworkAccess() {
        return new MockNetworkAccess(MockNetworkAttr.createOtherNetworkAttr(),
                                     MockStation.createOtherStation(),
                                     new Channel[] {MockChannel.createOtherNetChan()});
    }

    public static MockNetworkAccess createManySplendoredNetworkAccess() {
        Station[] stations = MockStation.createMultiSplendoredStations();
        Channel[][] channels = new Channel[stations.length][];
        for(int i = 0; i < stations.length; i++) {
            channels[i] = MockChannel.createMotionVector(stations[i]);
        }
        return new MockNetworkAccess(MockNetworkAttr.createMultiSplendoredAttr(),
                                     stations,
                                     channels);
    }

    private Network attributes;

    public MockNetworkAccess(Network attributes,
                             Station station,
                             Channel[] channels) {
        this(attributes, new Station[] {station}, make2DArray(channels));
    }

    private static Channel[][] make2DArray(Channel[] channels) {
        Channel[][] channels2d = new Channel[1][];
        channels2d[0] = channels;
        return channels2d;
    }

    public MockNetworkAccess() {
        this(MockNetworkAttr.createNetworkAttr(),
             MockStation.createStation(),
             new Channel[] {MockChannel.createChannel(),
                            MockChannel.createNorthChannel(),
                            MockChannel.createEastChannel()});
    }

    private MockNetworkAccess(Network attributes,
                              Station station[],
                              Channel[][] channels) {
        this.attributes = attributes;
        this.stations = station;
        this.channels = channels;
    }

    public Station[] retrieve_stations() {
        return stations;
    }

    public Channel retrieve_channel(ChannelId p1) {
        for(int j = 0; j < channels.length; j++) {
            for(int i = 0; i < channels[j].length; i++) {
                if(ChannelIdUtil.areEqual(new ChannelId(channels[j][i]), p1)) {
                    return channels[j][i];
                }
            }
        }
        return null;
    }

    public Channel[] retrieve_for_station(StationId p1) {
        for(int i = 0; i < stations.length; i++) {
            if(StationIdUtil.areEqual(p1, new StationId(stations[i]))) {
                return channels[i];
            }
        }
        return new Channel[] {};
    }

    public Network get_attributes() {
        return attributes;
    }

    private Station[] stations;

    private Channel[][] channels;
}