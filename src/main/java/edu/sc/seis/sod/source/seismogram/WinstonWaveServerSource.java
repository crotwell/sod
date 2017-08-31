package edu.sc.seis.sod.source.seismogram;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.seisFile.ChannelTimeWindow;
import edu.sc.seis.seisFile.earthworm.TraceBuf2;
import edu.sc.seis.seisFile.waveserver.WaveServer;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.seismogram.RequestFilterUtil;
import edu.sc.seis.sod.model.seismogram.SeismogramAttrImpl;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.util.time.ClockUtil;

public class WinstonWaveServerSource implements SeismogramSource {
     
    public WinstonWaveServerSource(WaveServer ws) {
        this.ws = ws;
    }
    
    public static Instant toDate(double d) {
        return ClockUtil.instantFromEpochSeconds(d);
    }
    
    public static double toEpochSeconds(Instant d) {
        return d.getMicroSecondTime() / 1000000.0;
    }
    
    public static double toY2KSeconds(Instant d) {
        return d.getMicroSecondTime() / 1000000.0 - 946728000;
    }
    
    public static Instant y2kSecondsToDate(double d) {
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
                                                           rf.start_time,
                                                           rf.end_time));
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
                                                         toDate(buf.getStartTime()),
                                                         buf.getNumSamples(),
                                                         SamplingImpl.ofSamplesSeconds(1, 1/buf.getSampleRate()),
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
