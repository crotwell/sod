package edu.sc.seis.sod.source.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.waveserver.MenuItem;
import edu.sc.seis.seisFile.waveserver.WaveServer;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.Orientation;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.SiteId;
import edu.sc.seis.sod.model.station.SiteImpl;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.util.time.ClockUtil;


public class WinstonNetworkSource extends CSVNetworkSource {

    public WinstonNetworkSource(Element config) throws ConfigurationException {
        super(config);
    }

    @Override
    protected void initChannels(Element config) throws ConfigurationException {
        host = SodUtil.loadText(config, "host", defaultHost);
        port = SodUtil.loadInt(config, "port", 16022);
        try {
        List<MenuItem> winstonMenu = getWaveServer().getMenu();
        channels = new ArrayList<ChannelImpl>();
        for (MenuItem menuItem : winstonMenu) {
            try {
            String netCode = menuItem.getNetwork();
            String staCode = menuItem.getStation();
            String chanCode = menuItem.getChannel();
            String siteCode = menuItem.getLocation();
            try {
                StationImpl curStation = getStationForChannel(netCode, staCode);
            if (curStation == null) {
                logger.warn("Can't find station for "+netCode+"."+ staCode+", skipping");
                continue;
            }
            float azimuth = ChannelImpl.getAzimuth(chanCode);
            float dip = ChannelImpl.getDip(chanCode);
            
            SamplingImpl sampling = new SamplingImpl(1, new TimeInterval(1, UnitImpl.SECOND));
            MicroSecondDate chanStart = curStation.getBeginTime();
            if (menuItem.getStartDate().before(ClockUtil.now())) {
                // sometime non-seismic channels are messed up in winston and have really bizarre times
                // only use if start time is before now
                chanStart = new MicroSecondDate(Math.round(1000000 * menuItem.getStart()));
            }
            TimeRange chanTime = new TimeRange(chanStart,
                                               DEFAULT_END);
            ChannelImpl channelImpl = new ChannelImpl(new ChannelId(curStation.get_id().network_id,
                                                                staCode,
                                                                siteCode,
                                                                chanCode,
                                                                chanTime.getBeginTime()),
                                                  "",
                                                  new Orientation(azimuth, dip),
                                                  sampling,
                                                  chanTime,
                                                  new SiteImpl(new SiteId(curStation.get_id().network_id,
                                                                          staCode,
                                                                          siteCode,
                                                                          chanTime.getBeginTime()), curStation, ""));
            channels.add(channelImpl);
            } catch (Throwable t) {
                logger.warn("problem with channel, "+netCode+"."+staCode+"."+siteCode+"."+chanCode+" skipping", t);
            }
            } catch (Throwable t) {
                logger.warn("problem with channel, skipping", t);
            }
        }
        } catch(IOException e) {
            throw new ConfigurationException("Unable to get menu from waveserver: ("+host+", "+port+")", e);
        }
    }
    
    public WaveServer getWaveServer() {
        if (ws == null) {
            ws = new WaveServer(host, port);
        }
        return ws;
    }
    
    private String defaultHost = "eeyore.seis.sc.edu";
    private String host;
    private int port;

    private WaveServer ws;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WinstonNetworkSource.class);
}
