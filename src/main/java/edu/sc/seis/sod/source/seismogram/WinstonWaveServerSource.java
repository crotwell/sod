package edu.sc.seis.sod.source.seismogram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.seisFile.ChannelTimeWindow;
import edu.sc.seis.seisFile.earthworm.TraceBuf2;
import edu.sc.seis.seisFile.waveserver.WaveServer;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.seismogram.RequestFilterUtil;
import edu.sc.seis.sod.model.seismogram.SeismogramAttrImpl;
import edu.sc.seis.sod.model.station.ChannelId;

public class WinstonWaveServerSource implements SeismogramSource {
     
    public WinstonWaveServerSource(WaveServer ws) {
        this.ws = ws;
    }
    
    public static MicroSecondDate toDate(double d) {
        return new MicroSecondDate(Math.round(d*1000000));
    }
    
    public static double toEpochSeconds(MicroSecondDate d) {
        return d.getMicroSecondTime() / 1000000.0;
    }
    
    public static double toY2KSeconds(MicroSecondDate d) {
        return d.getMicroSecondTime() / 1000000.0 - 946728000;
    }
    
    public static MicroSecondDate y2kSecondsToDate(double d) {
        return toDate(d + 946728000);
    }

    @Override
    public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException {
        List<LocalSeismogramImpl> out = new ArrayList<LocalSeismogramImpl>();
        for (RequestFilter rf : request) {
            try {
                List<TraceBuf2> traceBufs = ws.getTraceBuf(new ChannelTimeWindow(rf.channel_id.getNetworkId(), 
                                                            rf.channel_id.getStationCode(), 
                                                           rf.channel_id.getLocCode(),
                                                           rf.channel_id.getChannelCode(), 
                                                           new MicroSecondDate(rf.start_time),
                                                           new MicroSecondDate(rf.end_time)));
                if (traceBufs == null) {
                    continue;
                }
                for (TraceBuf2 buf : traceBufs) {
                    out.add(toFissures(buf, rf.channel_id));
                }
            } catch(IOException e) {
                throw new SeismogramSourceException("unable to get TraceBuf from WaveServer("+ws.getHost()+", "+ws.getPort()+") for "+RequestFilterUtil.toString(rf), e);
            }
        }
        return out;
    }
    
    public static LocalSeismogramImpl toFissures(TraceBuf2 buf, ChannelId chan) {
        SeismogramAttrImpl seisAttr = new SeismogramAttrImpl("via WaveServer:"+Math.random(),
                                                         new MicroSecondDate(toDate(buf.getStartTime())),
                                                         buf.getNumSamples(),
                                                         new SamplingImpl(1,
                                                                          new TimeInterval(1/buf.getSampleRate(),
                                                                                           UnitImpl.SECOND)),
                                                         UnitImpl.COUNT,
                                                         chan);
        if (buf.isShortData()) {
            return new LocalSeismogramImpl(seisAttr,
                                           buf.getShortData());
        } else if (buf.isIntData()) {
            return new LocalSeismogramImpl(seisAttr,
                                           buf.getIntData());
        } else if (buf.isFloatData()) {
            return new LocalSeismogramImpl(seisAttr,
                                           buf.getFloatData());
        } else if (buf.isDoubleData()) {
            return new LocalSeismogramImpl(seisAttr,
                                           buf.getDoubleData());
        } else {
            throw new RuntimeException("Unknwon data type: "+buf.getDataType());
        }
    }
    
    private WaveServer ws;
}
