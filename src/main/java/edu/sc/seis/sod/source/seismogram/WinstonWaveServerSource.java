package edu.sc.seis.sod.source.seismogram;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import gov.usgs.earthworm.Menu;
import gov.usgs.earthworm.MenuItem;
import gov.usgs.earthworm.TraceBuf;
import gov.usgs.winston.server.WWSClient;

import java.util.ArrayList;
import java.util.List;

public class WinstonWaveServerSource implements SeismogramSource {
     
    public WinstonWaveServerSource(WWSClient ws) {
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
    public List<RequestFilter> available_data(List<RequestFilter> request) {
        List<RequestFilter> out = new ArrayList<RequestFilter>();
        for (RequestFilter rf : request) {
            Menu menu = ws.getMenuSCNL();
            for (int i = 0; i < menu.numItems(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getChannel().equals(rf.channel_id.channel_code) &&
                        item.getStation().equals(rf.channel_id.station_code) &&
                        item.getNetwork().equals(rf.channel_id.network_id.network_code) &&
                        (item.getLocation().equals(rf.channel_id.site_code) || (item.getLocation().equals("--") && rf.channel_id.site_code.equals("  ")))) {
                    MicroSecondDate start = toDate(item.getStartTime());
                    MicroSecondDate end = toDate(item.getEndTime());
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
    }

    @Override
    public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws FissuresException {
        List<LocalSeismogramImpl> out = new ArrayList<LocalSeismogramImpl>();
        for (RequestFilter rf : request) {
            List<TraceBuf> traceBufs = ws.getTraceBufs(rf.channel_id.station_code, 
                                                       rf.channel_id.channel_code, 
                                                       rf.channel_id.network_id.network_code, 
                                                       rf.channel_id.site_code,
                                                       toEpochSeconds(rf.start_time),
                                                       toEpochSeconds(rf.end_time));
            if (traceBufs == null) {
                continue;
            }
            for (TraceBuf buf : traceBufs) {
                out.add(toFissures(buf, rf.channel_id));
            }
        }
        return out;
    }
    
    public static LocalSeismogramImpl toFissures(TraceBuf buf, ChannelId chan) {
        return new LocalSeismogramImpl(new SeismogramAttrImpl("via WaveServer:"+Math.random(),
                                       toDate(buf.startTime).getFissuresTime(),
                                       buf.numSamples,
                                       new SamplingImpl(1,
                                                        new TimeInterval(1/buf.samplingRate,
                                                                         UnitImpl.SECOND)),
                                       UnitImpl.COUNT,
                                       chan),
                                       buf.data);
    }
    private WWSClient ws;
}
