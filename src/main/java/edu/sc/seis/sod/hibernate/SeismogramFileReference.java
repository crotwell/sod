package edu.sc.seis.sod.hibernate;


import java.sql.Timestamp;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.seismogram.SeismogramAttrImpl;

public class SeismogramFileReference extends AbstractSeismogramFileReference {

    /** just for hibernate */
    protected SeismogramFileReference() {}
    
    public SeismogramFileReference(Channel channel,
                                   SeismogramAttrImpl seis,
                                   String fileLocation,
                                   SeismogramFileTypes filetype) {
        super(channel.getId().network_id.network_code, 
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
                                   Timestamp beginTime,
                                   Timestamp endTime,
                                   String filePath,
                                   int fileType) {
        super(netCode, staCode, siteCode, chanCode, beginTime, endTime, filePath, fileType);
    }
    
}
