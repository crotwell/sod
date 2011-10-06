package edu.sc.seis.sod.source.network;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.seismogram.WinstonWaveServerSource;
import gov.usgs.winston.server.WWSClient;


public class WinstonNetworkSource extends CSVNetworkSource {

    public WinstonNetworkSource(Element config) throws ConfigurationException {
        super(config);
    }

    @Override
    protected void initChannels(Element config) throws ConfigurationException {
        host = SodUtil.loadText(config, "host", defaultHost);
        port = SodUtil.loadInt(config, "port", 16022);
        List<gov.usgs.winston.Channel> winstonChannels = getWaveServer().getChannels();
        channels = new ArrayList<ChannelImpl>();
        for (gov.usgs.winston.Channel channel : winstonChannels) {
            try {
            String[] scnl = channel.getCode().split("\\$");
            String netCode = scnl[2];
            String staCode = scnl[0];
            String chanCode = scnl[1];
            String siteCode = scnl.length==3?"  ":scnl[3]; // if no 'l', just scn, then use space-space for site code
            try {
            StationImpl curStation = getStationForChannel(netCode, staCode);
            if (curStation == null) {
                logger.warn("Can't find station for "+netCode+"."+ staCode+", skipping");
                continue;
            }
            float azimuth = ChannelImpl.getAzimuth(chanCode);
            float dip = ChannelImpl.getDip(chanCode);
            
            SamplingImpl sampling = new SamplingImpl(1, new TimeInterval(1, UnitImpl.SECOND));
            Time chanStart = curStation.getBeginTime();
            if (channel.getMinTime() < WinstonWaveServerSource.toY2KSeconds(ClockUtil.now())) {
                // sometime non-seismic channels are messed up in winston and have really bizarre times
                // only use if start time is before now
                chanStart = WinstonWaveServerSource.y2kSecondsToDate(channel.getMinTime()).getFissuresTime();
            }
            TimeRange chanTime = new TimeRange(chanStart,
                                               DEFAULT_END);
            ChannelImpl channelImpl = new ChannelImpl(new ChannelId(curStation.get_id().network_id,
                                                                staCode,
                                                                siteCode,
                                                                chanCode,
                                                                chanTime.start_time),
                                                  "",
                                                  new Orientation(azimuth, dip),
                                                  sampling,
                                                  chanTime,
                                                  new SiteImpl(new SiteId(curStation.get_id().network_id,
                                                                          staCode,
                                                                          siteCode,
                                                                          chanTime.start_time), curStation, ""));
            channels.add(channelImpl);
            } catch (Throwable t) {
                logger.warn("problem with channel, "+netCode+"."+staCode+"."+siteCode+"."+chanCode+" skipping", t);
            }
            } catch (Throwable t) {
                logger.warn("problem with channel, skipping", t);
            }
        }
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
    
    private String defaultHost = "eeyore.seis.sc.edu";
    private String host;
    private int port;

    private WWSClient ws;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WinstonNetworkSource.class);
}
