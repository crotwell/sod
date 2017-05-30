package edu.sc.seis.sod.hibernate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import edu.sc.seis.sod.bag.Cut;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.MicroSecondTimeRange;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.seismogram.SeismogramAttrImpl;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.util.time.ReduceTool;

public class SeismogramFileRefDB extends AbstractHibernateDB {

    public void saveSeismogramToDatabase(ChannelImpl channel,
                                         SeismogramAttrImpl seis,
                                         String fileLocation,
                                         SeismogramFileTypes filetype) {
        saveSeismogramToDatabase(new SeismogramFileReference(channel, seis, fileLocation, filetype));
    }

    public void saveSeismogramToDatabase(SeismogramFileReference seisRef) {
        getSession().save(seisRef);
    }

    public void saveSeismogramToDatabase(CacheEvent event,
                                         ChannelImpl channel,
                                         SeismogramAttrImpl seis,
                                         String fileLocation,
                                         SeismogramFileTypes filetype) {
        saveSeismogramToDatabase(new EventSeismogramFileReference(event, channel, seis, fileLocation, filetype));
    }

    public void saveSeismogramToDatabase(EventSeismogramFileReference seisRef) {
        getSession().save(seisRef);
    }

    public List<EventSeismogramFileReference> getSeismogramsForEvent(CacheEvent event) {
        String query = "from " + EventSeismogramFileReference.class.getName() + " where event = :event";
        Query q = getSession().createQuery(query);
        q.setEntity("event", event);
        return q.list();
    }

    public List<EventSeismogramFileReference> getSeismogramsForEventForChannel(CacheEvent event, ChannelId chan) {
        String query = "from "
                + EventSeismogramFileReference.class.getName()
                + " where event = :event and "
                + " networkCode = :netCode and stationCode = :staCode and siteCode = :siteCode and channelCode = :chanCode ";
        Query q = getSession().createQuery(query);
        q.setEntity("event", event);
        q.setString("netCode", chan.network_id.network_code);
        q.setString("staCode", chan.station_code);
        q.setString("siteCode", chan.site_code);
        q.setString("chanCode", chan.channel_code);
        logger.debug("Before query for event: " + event.getDbid() + "  " + ChannelIdUtil.toStringNoDates(chan));
        List<EventSeismogramFileReference> esRefList = q.list();
        logger.debug("After query for event: " + event.getDbid() + "  " + ChannelIdUtil.toStringNoDates(chan)
                + "  found:" + esRefList.size());
        return esRefList;
    }

    public URLDataSetSeismogram getDataSetSeismogram(ChannelId chan, CacheEvent event, RequestFilter rf) {
        logger.debug("getDataSetSeismogram: " + getTXID() + "  " + ChannelIdUtil.toStringNoDates(chan) + "  " + event
                + "  " + rf.start_time.getISOTime() + "  " + rf.end_time.getISOTime());
        PrintIfNotCalledOff delayMessage = new PrintIfNotCalledOff(" long running getDataSetSeismogram: " + getTXID()
                + "  " + ChannelIdUtil.toStringNoDates(chan) + "  " + event + "  " + rf.start_time.getISOTime() + "  "
                + rf.end_time.getISOTime());
        List<EventSeismogramFileReference> esRefList = getSeismogramsForEventForChannel(event, chan);
        List<URL> urlList = new ArrayList<URL>();
        List<SeismogramFileTypes> ftList = new ArrayList<SeismogramFileTypes>();
        for (EventSeismogramFileReference esRef : esRefList) {
            urlList.add(esRef.getFilePathAsURL());
            try {
                ftList.add(SeismogramFileTypes.fromInt(esRef.getFileType()));
            } catch(UnsupportedFileTypeException e) {
                throw new RuntimeException("Should not happen as only valid file types should get into the db via hibernate.",
                                           e);
            }
        }
        delayMessage.callOff();
        return new URLDataSetSeismogram(urlList.toArray(new URL[0]),
                                        ftList.toArray(new SeismogramFileTypes[0]),
                                        null,
                                        ChannelIdUtil.toStringFormatDates(chan),
                                        rf);
    }

    public RequestFilter[] findMatchingSeismograms(RequestFilter[] requestArray, boolean ignoreNetworkTimes) {
        List results = queryDatabaseForSeismograms(requestArray, false, ignoreNetworkTimes);
        RequestFilter[] request = (RequestFilter[])results.toArray(new RequestFilter[results.size()]);
        RequestFilter[] reduced = ReduceTool.merge(request);
        return reduced;
    }

    public LocalSeismogramImpl[] getMatchingSeismograms(RequestFilter[] requestArray, boolean ignoreNetworkTimes) {
        List results = queryDatabaseForSeismograms(requestArray, true, ignoreNetworkTimes);
        LocalSeismogramImpl[] seis = (LocalSeismogramImpl[])results.toArray(new LocalSeismogramImpl[0]);
        LocalSeismogramImpl[] reduced = ReduceTool.merge(seis);
        return reduced;
    }

    public List queryDatabaseForSeismograms(RequestFilter[] request,
                                            boolean returnSeismograms,
                                            boolean ignoreNetworkTimes) {
        RequestFilter[] minimalRequest = ReduceTool.merge(request);
        List resultCollector = new ArrayList();
        for (int i = 0; i < minimalRequest.length; i++) {
            queryDatabaseForSeismogram(resultCollector, minimalRequest[i], returnSeismograms, ignoreNetworkTimes);
        }
        return resultCollector;
    }

    private void queryDatabaseForSeismogram(List resultCollector,
                                            RequestFilter request,
                                            boolean returnSeismograms,
                                            boolean ignoreNetworkTimes) {
        // Retrieve channel ID, begin time, and end time from the request
        // and place the times into a time table while
        // buffering the query by one second on each end.
        ChannelImpl chanId;
        Cut cutter = new Cut(request);
        try {
            if (ignoreNetworkTimes) {
                chanId = chanTable.getChannel(request.channel_id.network_id.network_code,
                                              request.channel_id.station_code,
                                              request.channel_id.site_code,
                                              request.channel_id.channel_code,
                                              new MicroSecondDate(request.start_time));
            } else {
                chanId = chanTable.getChannel(request.channel_id);
            }
        } catch(NotFound e) {
            logger.warn("Can not find channel ID in database.");
            return;
        }
        MicroSecondDate adjustedBeginTime = new MicroSecondDate(request.start_time).subtract(ONE_SECOND);
        MicroSecondDate adjustedEndTime = new MicroSecondDate(request.end_time).add(ONE_SECOND);
        String query = "from "
                + SeismogramFileReference.class.getName()
                + " where networkCode = :netCode and stationCode = :staCode and siteCode = :siteCode and channelCode = :chanCode "
                + " and beginTime < :end and endTime >= :begin";
        // Populate databaseResults with all of the matching seismograms
        // from the database.
        Query q = getSession().createQuery(query);
        ChannelId chanIdxxx = chanId.getId();
        q.setString("netCode", chanIdxxx.network_id.network_code);
        q.setString("staCode", chanIdxxx.station_code);
        q.setString("siteCode", chanIdxxx.site_code);
        q.setString("chanCode", chanIdxxx.channel_code);
        q.setTimestamp("end", adjustedEndTime.getTimestamp());
        q.setTimestamp("begin", adjustedBeginTime.getTimestamp());
        List<SeismogramFileReference> databaseResults = q.list();
        if (returnSeismograms) {
            for (SeismogramFileReference seisRef : databaseResults) {
                try {
                    File seismogramFile = new File(seisRef.getFilePath());
                    SeismogramFileTypes filetype = SeismogramFileTypes.fromInt(seisRef.getFileType());
                    LocalSeismogramImpl[] curSeis;
                    
                    URLDataSetSeismogram urlSeis = new URLDataSetSeismogram(seismogramFile.toURL(), filetype);
                    curSeis = urlSeis.getSeismograms();
                    
                    for (int j = 0; j < curSeis.length; j++) {
                        LocalSeismogramImpl seis = cutter.applyEncoded(curSeis[j]);
                        if (seis != null) {
                            resultCollector.add(seis);
                        }
                    }
                } catch(Exception e) {
                    GlobalExceptionHandler.handle("Problem occured while returning seismograms from the database."
                            + "\n" + "The problem file is located at " + seisRef.getFilePath(), e);
                }
            }
        } else {
            for (SeismogramFileReference seisRef : databaseResults) {
                RequestFilter req = new RequestFilter(chanIdxxx,
                                                      new MicroSecondDate(seisRef.getBeginTime()),
                                                      new MicroSecondDate(seisRef.getEndTime()));
                req = cutter.apply(req);
                if (req != null) {
                    resultCollector.add(req);
                }
            }
        }
    }

    /**
     * removes the seismogram reference from the database for the given file
     * name.
     */
    public int removeSeismogramFromDatabase(String seisFile) {
        String query = "delete  " + SeismogramFileReference.class.getName() + " where filePath = " + seisFile;
        return getSession().createQuery(query).executeUpdate();
    }

    private static SeismogramFileRefDB singleton;

    public static SeismogramFileRefDB getSingleton() {
        if (singleton == null) {
            singleton = new SeismogramFileRefDB();
        }
        return singleton;
    }

    protected NetworkDB chanTable = NetworkDB.getSingleton();

    private static final TimeInterval ONE_SECOND = new TimeInterval(1, UnitImpl.SECOND);

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SeismogramFileRefDB.class);
}
