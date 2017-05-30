package edu.sc.seis.sod.source.seismogram;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.waveserver.WaveServer;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;



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
    
    public WaveServer getWaveServer() {
        if (ws == null) {
            ws = new WaveServer(host, port);
        }
        return ws;
    }

    private WaveServer ws;
    String host;
    int port;
}
