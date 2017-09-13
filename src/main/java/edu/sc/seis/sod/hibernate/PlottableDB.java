package edu.sc.seis.sod.hibernate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.seismogram.PlottableChunk;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.util.time.RangeTool;
import edu.sc.seis.sod.util.time.ReduceTool;


public class PlottableDB extends AbstractHibernateDB {
    
    protected PlottableDB() {}
    
    private static PlottableDB singleton;

    public static PlottableDB getSingleton() {
        if(singleton == null) {
            singleton = new PlottableDB();
        }
        return singleton;
    }

    public List<PlottableChunk> get(TimeRange requestRange,
                                ChannelId channel,
                                int pixelsPerDay) {
        return get(requestRange, channel.getNetworkId(),
                   channel.getStationCode(),
                   channel.getLocCode(),
                   channel.getChannelCode(),
                   pixelsPerDay);
    }


    public List<PlottableChunk> get(TimeRange requestRange,
                                String network,
                                String station,
                                String site,
                                String channel,
                                int pixelsPerDay) {
        Query q = getSession().createQuery("from "+PlottableChunk.class.getName()+" where "+
        " networkCode = :net and stationCode = :sta and siteCode = :site and channelCode = :chan "+
        " and pixelsPerDay = :pixelsPerDay "+
        " and ( beginTimestamp <= :end and endTimestamp >= :begin )");
        q.setString("net", network);
        q.setString("sta", station);
        q.setString("site", site);
        q.setString("chan", channel);
        q.setInteger("pixelsPerDay", pixelsPerDay);
        q.setParameter("end", requestRange.getEndTime());
        q.setParameter("begin", requestRange.getBeginTime());
        List<PlottableChunk> chunks = q.list();
        return chunks;
    }

    public void put(List<PlottableChunk> chunks) {
        if (chunks.size() == 0) {return;}
        TimeRange stuffInDB = getDroppingRange(chunks);
        List<PlottableChunk> dbChunks = get(stuffInDB,
                                        chunks.get(0).getNetworkCode(),
                                        chunks.get(0).getStationCode(),
                                        chunks.get(0).getSiteCode(),
                                        chunks.get(0).getChannelCode(),
                                        chunks.get(0).getPixelsPerDay());
        List<PlottableChunk> everything = new ArrayList<PlottableChunk>();
        everything.addAll(dbChunks);
        everything.addAll(chunks);
        // scrutinizeEverything(everything, "unmerged");
        everything = ReduceTool.merge(everything);
        // scrutinizeEverything(everything, "merged");
        everything = breakIntoDays(everything);
        // scrutinizeEverything(everything, "split into days");
        PlottableChunk first = chunks.get(0);
        int rowsDropped = drop(stuffInDB,
                               first.getNetworkCode(),
                               first.getStationCode(),
                               first.getSiteCode(),
                               first.getChannelCode(),
                               first.getPixelsPerDay());
        for (PlottableChunk plottableChunk : everything) {
            getSession().save(plottableChunk);
        }
    }
    

    public int drop(TimeRange requestRange,
                    String network,
                    String station,
                    String site,
                    String channel,
                    int samplesPerDay) {
        List<PlottableChunk> indb = get(requestRange, network, station, site, channel, samplesPerDay);
        for (PlottableChunk plottableChunk : indb) {
            getSession().delete(plottableChunk);
        }
        return indb.size();
    }
    
    protected PlottableChunk[] getSmallChunks(TimeRange requestRange,
                                              String network,
                                              String station,
                                              String site,
                                              String channel,
                                int pixelsPerDay) {
        throw new RuntimeException("Not yet implemented");
    }

    private List<PlottableChunk> breakIntoDays(List<PlottableChunk> everything) {
        List<PlottableChunk> results = new ArrayList<PlottableChunk>();
        for (PlottableChunk chunk : everything) {
            results.addAll(chunk.breakIntoDays());
        }
        return results;
    }

    private static TimeRange getDroppingRange(List<PlottableChunk> chunks) {
        TimeRange stuffInDB = RangeTool.getFullTime(chunks);
        Instant startTime = PlottableChunk.stripToDay(stuffInDB.getBeginTime());
        Instant strippedEnd = PlottableChunk.stripToDay(stuffInDB.getEndTime());
        if(!strippedEnd.equals(stuffInDB.getEndTime())) {
            strippedEnd = strippedEnd.plus(PlottableChunk.ONE_DAY);
        }
        return new TimeRange(startTime, strippedEnd);
    }

    protected static int MIN_CHUNK_SIZE = 100;
    
    static String configFile = "edu/sc/seis/fissuresUtil/hibernate/Plottable.hbm.xml";
    
    public static void configHibernate(Configuration config) {
        logger.debug("adding to HibernateUtil   "+configFile);
        config.addResource(configFile);
    }
    

    private static Logger logger = LoggerFactory.getLogger(PlottableDB.class);
}
