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
