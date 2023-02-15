package edu.sc.seis.sod.mock.station;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.Orientation;
import edu.sc.seis.sod.model.station.ChannelGroup;

public class MockChannel {
    
    public static Channel createChannel() {
        return createChannel(MockStation.createStation(), "00", "BHZ",
                             20,
                             0, -90,
                             0,
                             "Vertical Channel");
    }

    public static Channel createNorthChannel() {
        return createChannel(MockStation.createStation(), "00", "BHN",
                20,
                0, 0,
                0,
                "North Channel");
    }

    public static Channel createEastChannel() {
        return createChannel(MockStation.createStation(), "00", "BHE",
                20,
                90, 0,
                0,
                "East Channel");
    }

    public static Channel createOtherSiteSameStationChan() {
        Channel out = createChannel(MockStation.createStation(), "01", "BHZ",
                20,
                0, -90,
                0,
                "Nearby Vertical Channel");
        out.setLatitude(out.getLatitude().getValue() + .001f);
        return out;
    }

    public static Channel createOtherNetChan() {
        Channel out = createChannel(MockStation.createOtherStation(), "00", "BHZ",
                20,
                0, -90,
                0,
                "Vertical Channel");
        return out;
    }

    public static Channel[] createChannelsAtLocs(Location[] locs) {
        Channel[] chans = new Channel[locs.length];
        for(int i = 0; i < chans.length; i++) {
            chans[i] = createChannel(locs[i]);
        }
        return chans;
    }

    public static Channel createChannel(Location location) {
        return createChannel( MockStation.createStation(location));
    }

    
    public static Channel createChannel(Station station) {
        return createChannel(station, "00", "BHZ");
    }
    
    public static Channel createChannel(Station station, String siteCode, String chanCode) {
        float az = 0;
        float dip = -90;
        if (chanCode.endsWith("N")) {
            az = 0;
            dip = 0;
        } else if (chanCode.endsWith("E")) {
            az = 90;
            dip = 0;
        }
        return createChannel(MockStation.createStation(), "00", chanCode,
                20,
                az, dip,
                0,
                chanCode+" Channel");
    }
    
    public static Channel createChannel(Station station,
    		                                String locCode,
    		                                String chanCode,
    		                                float sampleRate,
    		                                float azimuth,
    		                                float dip,
    		                                float depth,
    		                                String desc) {
        Channel out = new Channel(station, locCode, chanCode);
        out.setAzimuth(azimuth);
        out.setDepth(depth);
        out.setDescription(desc);
        out.setDip(dip);
        out.setElevation(station.getElevation());
        out.setEndDateTime(station.getEndDateTime());
        out.setLatitude(station.getLatitude());
        out.setLongitude(station.getLongitude());
        out.setSampleRate(sampleRate);
        out.setStartDateTime(station.getStartDateTime());
        return out;
    }

    public static Channel[] createMotionVector() {
        return createMotionVector(MockStation.createStation());
    }

    public static Channel[] createMotionVector(Station station) {
        Channel[] channels = new Channel[3];
        String[] codes = {"BHZ", "BHN", "BHE"};
        String locCode = "00";
        for(int i = 0; i < codes.length; i++) {
            channels[i] = createChannel(station, locCode, codes[i]);
        }
        return channels;
    }

    public static ChannelGroup createGroup() {
        return new ChannelGroup( createMotionVector());
    }
    
    private static final Orientation VERTICAL = new Orientation(0, -90);

    private static final Orientation EAST = new Orientation(90, 0);

    private static final Orientation NORTH = new Orientation(0, 0);

    private static final Orientation[] ORIENTATIONS = {VERTICAL, NORTH, EAST};

}
