package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import gov.usgs.earthworm.Menu;
import gov.usgs.earthworm.MenuItem;
import gov.usgs.earthworm.TraceBuf;
import gov.usgs.winston.server.WWSClient;



public class WinstonWaveServer implements SeismogramSourceLocator, SodElement {


    public WinstonWaveServer(Element config){
        this(config, "eeyore.seis.sc.edu");
    }
    
    public WinstonWaveServer(Element config, String defaultHost){
        host = SodUtil.loadText(config, "host", defaultHost);
        port = SodUtil.loadInt(config, "port", 16022);
    }
    
    public WinstonWaveServer(String host, int port){
        this.host = host;
        this.port = port;
    }
    
    @Override
    public SeismogramSource getSeismogramSource(CacheEvent event,
                                                ChannelImpl channel,
                                                RequestFilter[] infilters,
                                                CookieJar cookieJar) throws Exception {
        return new WinstonWaveServerSource(getWaveServer());
    }
    
    public WWSClient getWaveServer() {
        if (ws == null) {
            ws = new WWSClient(host, port);
            if (! ws.connect()) {
                System.out.println("Not connected to WWS");
            }
            
        }
        return ws;
    }

    private WWSClient ws;
    String host;
    int port;
}

class WinstonWaveServerSource implements SeismogramSource {
     
    WinstonWaveServerSource(WWSClient ws) {
        this.ws = ws;
    }
    
    public static MicroSecondDate toDate(double d) {
        return new MicroSecondDate(Math.round(d*1000000));
    }
    
    public static double toEpochSeconds(Time d) {
        return new MicroSecondDate(d).getMicroSecondTime() / 1000000.0;
    }
    
    public static double toY2KSeconds(Time d) {
        return new MicroSecondDate(d).getMicroSecondTime() / 1000000.0 - 946728000;
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
                        item.getLocation().equals(rf.channel_id.site_code)) {
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
