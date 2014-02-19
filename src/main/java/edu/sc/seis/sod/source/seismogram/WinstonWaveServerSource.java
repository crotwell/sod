package edu.sc.seis.sod.source.seismogram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfSeismogramDC.SeismogramAttr;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.RequestFilterUtil;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.seisFile.earthworm.TraceBuf2;
import edu.sc.seis.seisFile.waveserver.MenuItem;
import edu.sc.seis.seisFile.waveserver.WaveServer;

public class WinstonWaveServerSource implements SeismogramSource {
     
    public WinstonWaveServerSource(WaveServer ws) {
        this.ws = ws;
    }
    
    public static MicroSecondDate toDate(double d) {
        return new MicroSecondDate(Math.round(d*1000000));
    }
    
    public static double toEpochSeconds(Time d) {
        return new MicroSecondDate(d).getMicroSecondTime() / 1000000.0;
    }

    public static double toY2KSeconds(Time d) {
        return toY2KSeconds(new MicroSecondDate(d));
    }
    
    public static double toY2KSeconds(MicroSecondDate d) {
        return d.getMicroSecondTime() / 1000000.0 - 946728000;
    }
    
    public static MicroSecondDate y2kSecondsToDate(double d) {
        return toDate(d + 946728000);
    }
    
    @Override
    public List<RequestFilter> availableData(List<RequestFilter> request) throws SeismogramSourceException {
        List<RequestFilter> out = new ArrayList<RequestFilter>();
        List<MenuItem> menu;
        try {
            menu = ws.getMenu();
            for (RequestFilter rf : request) {
                for (MenuItem item : menu) {
                    if (item.getChannel().equals(rf.channel_id.channel_code) &&
                            item.getStation().equals(rf.channel_id.station_code) &&
                            item.getNetwork().equals(rf.channel_id.network_id.network_code) &&
                            (item.getLocation().equals(rf.channel_id.site_code) || (item.getLocation().equals("--") && rf.channel_id.site_code.equals("  ")))) {
                        MicroSecondDate start = toDate(item.getStart());
                        MicroSecondDate end = toDate(item.getEnd());
                        MicroSecondTimeRange menuRange = new MicroSecondTimeRange(start, end);
                        MicroSecondTimeRange rfRange = new MicroSecondTimeRange(rf);
                        if (menuRange.intersects(rfRange)) {
                            MicroSecondTimeRange intersection = menuRange.intersection(rfRange);
                            out.add(new RequestFilter(rf.channel_id, intersection.getBeginTime().getFissuresTime(), intersection.getEndTime().getFissuresTime()));
                        }
                    }
                }
            }
            return out;
        } catch(IOException e) {
            throw new SeismogramSourceException("unable to get menu from WaveServer("+ws.getHost()+", "+ws.getPort()+")", e);
        }
    }

    @Override
    public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException {
        List<LocalSeismogramImpl> out = new ArrayList<LocalSeismogramImpl>();
        for (RequestFilter rf : request) {
            try {
                List<TraceBuf2> traceBufs = ws.getTraceBuf(rf.channel_id.network_id.network_code, 
                                                            rf.channel_id.station_code, 
                                                           rf.channel_id.site_code,
                                                           rf.channel_id.channel_code, 
                                                           new MicroSecondDate(rf.start_time),
                                                           new MicroSecondDate(rf.end_time));
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
        SeismogramAttr seisAttr = new SeismogramAttrImpl("via WaveServer:"+Math.random(),
                                                         toDate(buf.getStartTime()).getFissuresTime(),
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
