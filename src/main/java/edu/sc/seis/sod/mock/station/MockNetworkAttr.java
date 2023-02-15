package edu.sc.seis.sod.mock.station;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.mock.Defaults;

public class MockNetworkAttr{
    public static Network createNetworkAttr(){
        Network out = new Network("XX");
        out.setStartDateTime(Defaults.EPOCH_ZDT);
        out.setDescription("A network");
        return out;
    }
    public static Network createNetworkAttr(String code){
        Network out = new Network(code);
        out.setStartDateTime(Defaults.EPOCH_ZDT);
        out.setDescription(code+" network");
        return out;
    }

    public static Network createOtherNetworkAttr(){
        Network out = new Network("XX");
        out.setStartDateTime(Defaults.WALL_FALL_ZDT);
        out.setDescription("krowten A");
        return out;
    }
    
    public static Network createMultiSplendoredAttr(){
        Network out = new Network("MS");
        out.setStartDateTime(Defaults.WALL_FALL_ZDT);
        out.setDescription("A network with many stations");
        return out;
    }
}
