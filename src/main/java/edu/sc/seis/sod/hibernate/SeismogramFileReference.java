package edu.sc.seis.sod.hibernate;


import java.time.Instant;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.seismogram.SeismogramAttrImpl;

public class SeismogramFileReference extends AbstractSeismogramFileReference {

    /** just for hibernate */
    protected SeismogramFileReference() {}
    
    public SeismogramFileReference(Channel channel,
                                   SeismogramAttrImpl seis,
                                   String fileLocation,
                                   SeismogramFileTypes filetype) {
        super(channel.getNetwork().getNetworkCode(), 
              channel.getStationCode(),
              channel.getLocCode(),
              channel.getChannelCode(),
              seis.getBeginTime(),
              seis.getEndTime(),
              fileLocation,
              filetype.getIntValue());
    }

    public SeismogramFileReference(String netCode,
                                   String staCode,
                                   String siteCode,
                                   String chanCode,
                                   Instant beginTime,
                                   Instant endTime,
                                   String filePath,
                                   int fileType) {
        super(netCode, staCode, siteCode, chanCode, beginTime, endTime, filePath, fileType);
    }
    
}
