package edu.sc.seis.sod.hibernate;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.model.station.NetworkId;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.model.station.StationId;
import edu.sc.seis.sod.model.station.StationImpl;

public class NetworkDB extends AbstractHibernateDB {

    protected NetworkDB() {}
    
    public int put(NetworkAttrImpl net) {
        Session session = getSession();
        if(net.getDbid() != 0) {
            session.saveOrUpdate(net);
            return net.getDbid();
        }
        Iterator<NetworkAttrImpl> fromDB = getNetworkByCode(net.get_code()).iterator();
        if(fromDB.hasNext()) {
            if(NetworkIdUtil.isTemporary(net.get_code())) {
                while(fromDB.hasNext()) {
                    NetworkAttrImpl indb = fromDB.next();
                    if(net.get_code().equals(indb.get_code())
                            && NetworkIdUtil.getTwoCharYear(net.get_id())
                                    .equals(NetworkIdUtil.getTwoCharYear(indb.get_id()))) {
                        net.associateInDB(indb);
                        getSession().evict(indb);
                        getSession().saveOrUpdate(net);
                        return net.getDbid();
                    }
                    // our net is not in db, but others of same code are, just add it, below
                }
                // didn't find in database, is temp so this might be ok, put new
                // net below
            } else {
                // use first and only net
                NetworkAttrImpl indb = (NetworkAttrImpl)fromDB.next();
                net.associateInDB(indb);
                getSession().evict(indb);
                getSession().saveOrUpdate(net);
                return net.getDbid();
            }
        }
        return ((Integer)session.save(net)).intValue();
    }

    public int put(StationImpl sta) {
        Integer dbid;
        if(((NetworkAttrImpl)sta.getNetworkAttr()).getDbid() == 0) {
            // assume network info is already put, attach net
            try {
                sta.setNetworkAttr(getNetworkById(sta.getNetworkAttr().get_id()));
            } catch(NotFound ee) {
                // must not have been added yet
                put((NetworkAttrImpl)sta.getNetworkAttr());
            }
        }
        internUnit(sta);
        if(sta.getDbid() != 0) {
            getSession().saveOrUpdate(sta);
            return sta.getDbid();
        }
        try {
            // maybe station is already in db, so update
            StationImpl indb = getStationById(sta.get_id());
            sta.associateInDB(indb);
            getSession().evict(indb);
            getSession().evict(indb.getNetworkAttr());
            getSession().saveOrUpdate(sta);
            return sta.getDbid();
        } catch(NotFound e) {
            dbid = (Integer)getSession().save(sta);
            return dbid.intValue();
        }
    }

    /**
     * Puts a channel into the database. If there is an existing channel in the
     * database with the same database id, but different attributes (reflecting
     * a change at the server) the existing channel is expired and the new
     * channel is inserted. This preserves any existing objects that refer to
     * the old channel, while allowing future work to only access the new
     * channel.
     */
    public int put(ChannelImpl chan) {
        Integer dbid;
        internUnit(chan);
        if(((StationImpl)chan.getSite().getStation()).getDbid() == 0) {
            try {
                chan.getSite().setStation(getStationById(chan.getSite()
                        .getStation()
                        .get_id()));
            } catch(NotFound e) {
                int staDbid = put((StationImpl)chan.getSite().getStation());
            }
        }
        try {
            ChannelImpl indb = getChannel(chan.get_id());
            chan.associateInDB(indb);
            getSession().evict(indb);
            getSession().evict(indb.getSite().getStation());
            getSession().evict(indb.getSite().getStation().getNetworkAttr());
            getSession().saveOrUpdate(chan);
            dbid = chan.getDbid();
        } catch(NotFound nf) {
            dbid = (Integer)getSession().save(chan);
        }
        logger.debug("Put channel as "+dbid+" "+ChannelIdUtil.toStringFormatDates(chan.get_id())+"  sta dbid="+((StationImpl)chan.getSite().getStation()).getDbid());
        return dbid.intValue();
    }

    public int put(ChannelGroup cg) {
        ChannelGroup indb = getChannelGroup(cg.getChannel1(), 
                                            cg.getChannel2(),
                                            cg.getChannel3());
        ChannelImpl[] chans = cg.getChannels();
        for(int i = 0; i < chans.length; i++) {
            if(chans[i].getDbid() == 0) {
                try {
                    chans[i] = getChannel(chans[i].get_id());
                } catch(NotFound e) {
                    put(chans[i]);
                }
            }
        }
        if (indb != null) {
            cg.setDbid(indb.getDbid());
            getSession().evict(indb);
            getSession().saveOrUpdate(cg);
            return cg.getDbid();
        }
        int dbid = (Integer)getSession().save(cg);
        return dbid;
    }

    public List<ChannelGroup> getChannelGroup(ChannelImpl chan) {
        Query query = getSession().createQuery("from "
                + ChannelGroup.class.getName()
                + " where channel1 = :chan or channel2 = :chan or channel3 = :chan");
        query.setEntity("chan", chan);
        return query.list();
    }

    public ChannelGroup getChannelGroup(ChannelImpl chanA,
                                        ChannelImpl chanB,
                                        ChannelImpl chanC) {
        Query query = getSession().createQuery("from "
                + ChannelGroup.class.getName()
                + " where "
                + "    ( channel1 = :chanA and channel2 = :chanB and channel3 = :chanC )"
                + " or ( channel1 = :chanB and channel2 = :chanA and channel3 = :chanC )"
                + " or ( channel1 = :chanC and channel2 = :chanA and channel3 = :chanB )"
                + " or ( channel1 = :chanA and channel2 = :chanC and channel3 = :chanB )"
                + " or ( channel1 = :chanB and channel2 = :chanC and channel3 = :chanA )"
                + " or ( channel1 = :chanC and channel2 = :chanB and channel3 = :chanA )");
        query.setEntity("chanA", chanA);
        query.setEntity("chanB", chanB);
        query.setEntity("chanC", chanC);
        query.setMaxResults(1);
        List<ChannelGroup> l = query.list();
        if(l.size() != 0) {
            return l.get(0);
        }
        return null;
    }

    public List<StationImpl> getStationByCodes(String netCode, String staCode) {
        Query query = getSession().createQuery(getStationByCodes);
        query.setString("netCode", netCode);
        query.setString("staCode", staCode);
        return query.list();
    }

    public List<StationImpl> getAllStationsByCode(String staCode) {
        Query query = getSession().createQuery(getAllStationsByCode);
        query.setString("staCode", staCode);
        return query.list();
    }

    public StationImpl getStationById(StationId staId) throws NotFound {
        Query query = getSession().createQuery(getStationByIdString);
        query.setString("netCode", staId.network_id.network_code);
        query.setString("staCode", staId.station_code);
        query.setTimestamp("staBegin",
                           new MicroSecondDate(staId.begin_time).getTimestamp());
        query.setMaxResults(1);
        List<StationImpl> l = query.list();
        logger.debug("getStationById("+staId.network_id.network_code+"."+staId.station_code+"."+staId.begin_time.getISOString()+"  return size: "+l.size());
        if(l.size() != 0) {
            return l.get(0);
        }
        throw new NotFound();
    }

    public List<NetworkAttrImpl> getNetworkByCode(String netCode) {
        Query query = getSession().createQuery(getNetworkByCodeString);
        query.setString("netCode", netCode);
        return query.list();
    }

    public NetworkAttrImpl getNetworkById(NetworkId netId) throws NotFound {
        List<NetworkAttrImpl> result = getNetworkByCode(netId.network_code);
        if(NetworkIdUtil.isTemporary(netId)) {
            Iterator<NetworkAttrImpl> it = result.iterator();
            while(it.hasNext()) {
                NetworkAttrImpl n = it.next();
                if(NetworkIdUtil.areEqual(netId, n.get_id())) {
                    return n;
                }
            }
            throw new NotFound();
        } else {
            if(result.size() > 0) {
                return result.get(0);
            }
            throw new NotFound();
        }
    }

    public NetworkAttrImpl getNetwork(int dbid) throws NotFound {
        NetworkAttrImpl out = (NetworkAttrImpl)getSession().get(NetworkAttrImpl.class,
                                                                new Integer(dbid));
        if(out == null) {
            throw new NotFound();
        }
        return out;
    }

    public List<NetworkAttrImpl> getAllNetworks() {
        Query query = getSession().createQuery(getAllNetsString);
        return query.list();
    }

    public StationImpl getStation(int dbid) throws NotFound {
        StationImpl out = (StationImpl)getSession().get(StationImpl.class,
                                                        new Integer(dbid));
        if(out == null) {
            throw new NotFound();
        }
        return out;
    }

    public StationImpl[] getAllStations() {
        Query query = getSession().createQuery(getAllStationsString);
        List result = query.list();
        return (StationImpl[])result.toArray(new StationImpl[0]);
    }

    public List<StationImpl> getStationForNet(NetworkAttrImpl attr) {
        Query query = getSession().createQuery(getStationForNetwork);
        query.setEntity("netAttr", attr);
        return query.list();
    }

    public List<StationImpl> getStationForNet(NetworkAttrImpl attr,
                                              String staCode) {
        Query query = getSession().createQuery(getStationForNetworkStation);
        query.setEntity("netAttr", attr);
        query.setString("staCode", staCode);
        return query.list();
    }

    public List<ChannelImpl> getChannelsForNet(NetworkAttrImpl attr) {
        Query query = getSession().createQuery(getChannelForNetwork);
        query.setEntity("netAttr", attr);
        return query.list();
    }

    public ChannelImpl getChannel(int dbid) throws NotFound {
        ChannelImpl out = (ChannelImpl)getSession().get(ChannelImpl.class,
                                                        new Integer(dbid));
        if(out == null) {
            throw new NotFound();
        }
        return out;
    }

    public List<ChannelImpl> getAllChannels() {
        return getSession().createQuery("from " + ChannelImpl.class.getName())
                .list();
    }

    public List<ChannelImpl> getChannelsForStation(StationImpl station) {
        Query query = getSession().createQuery(getChannelForStation);
        query.setEntity("station", station);
        List<ChannelImpl> out = query.list();
        logger.debug("getChannelsForStation("+station.getDbid()+" found "+out.size()+"  query="+query);
        return out;
    }

    public List<ChannelGroup> getChannelGroupsForStation(StationImpl station) {
        Query query = getSession().createQuery(getChannelGroupForStation);
        query.setEntity("station", station);
        return query.list();
    }

    public List<ChannelImpl> getChannelsForStation(StationImpl station,
                                                   MicroSecondDate when) {
        Query query = getSession().createQuery(getChannelForStationAtTime);
        query.setEntity("station", station);
        query.setTimestamp("when", when.getTimestamp());
        return query.list();
    }

    public ChannelImpl getChannel(String net,
                                  String sta,
                                  String site,
                                  String chan,
                                  MicroSecondDate when) throws NotFound {
        return getChannel(net, sta, site, chan, when, getChannelByCode);
    }

    public List<ChannelImpl> getChannelsByCode(NetworkId net,
                                               String sta,
                                               String site,
                                               String chan) {
        String queryString = "From " + ChannelImpl.class.getName() + " WHERE "
                + chanCodeHQL
                + " AND site.station.networkAttr.beginTime.time = :when";
        Query query = getSession().createQuery(queryString);
        query.setString("netCode", net.network_code);
        query.setString("stationCode", sta);
        query.setString("siteCode", site);
        query.setString("channelCode", chan);
        query.setTimestamp("when",
                           new MicroSecondDate(net.begin_time).getTimestamp());
        return query.list();
    }

    public ChannelImpl getChannel(ChannelId id) throws NotFound {
        return getChannel(id.network_id.network_code,
                          id.station_code,
                          id.site_code,
                          id.channel_code,
                          new MicroSecondDate(id.begin_time),
                          getChannelById);
    }

    protected ChannelImpl getChannel(String net,
                                     String sta,
                                     String site,
                                     String chan,
                                     MicroSecondDate when,
                                     String queryString) throws NotFound {
        Query query = getSession().createQuery(queryString);
        query.setString("netCode", net);
        query.setString("stationCode", sta);
        String sc = site.trim();
        if (sc.equals("--") || sc.equals("") || sc.equals("  ")) {sc = edu.sc.seis.seisFile.fdsnws.stationxml.Channel.EMPTY_LOC_CODE;}
        query.setString("siteCode", sc);
        query.setString("channelCode", chan);
        query.setTimestamp("when", when.getTimestamp());
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() == 0) {
            throw new NotFound();
        }
        return (ChannelImpl)result.get(0);
    }

    public InstrumentationBlob getInstrumentationBlob(ChannelImpl chan) throws ChannelNotFound {
        Query query = getSession().createQuery("from "+InstrumentationBlob.class.getName()+" where channel = :chan");
        query.setEntity("chan", chan);
        Iterator it = query.iterate();
        if (it.hasNext()) {
            InstrumentationBlob ib = (InstrumentationBlob)it.next();
            return ib;
        }
        return null;    
    }

    
    public Response getResponse(ChannelImpl chan) throws ChannelNotFound {
        InstrumentationBlob ib = getInstrumentationBlob(chan);
        if (ib != null) {
            Response resp =  ib.getResponse(); // might be null, meaning no inst exists, but blob in DB so we tried before
            if (resp == null) { throw new ChannelNotFound(); }
            return resp;
        }
        return null; // instBlob null, so never seen this channel before
    }
    
    @Deprecated
    public Instrumentation getInstrumentation(ChannelImpl chan) throws ChannelNotFound {
        InstrumentationBlob ib = getInstrumentationBlob(chan);
        if (ib != null) {
            Instrumentation inst =  ib.getInstrumentation(); // might be null, meaning no inst exists, but blob in DB so we tried before
            if (inst == null) { throw new ChannelNotFound(); }
            return inst;
        }
        return null; // instBlob null, so never seen this channel before
    }
    
    public ChannelSensitivity getSensitivity(ChannelImpl chan) {
        Query query = getSession().createQuery("from "+ChannelSensitivity.class.getName()+" where channel = :chan");
        query.setEntity("chan", chan);
        Iterator it = query.iterate();
        if (it.hasNext()) {
            ChannelSensitivity sense = (ChannelSensitivity)it.next();
            return sense;
        }
        return null; 
    }
    
    public void putSensitivity(ChannelSensitivity sensitivity) {
        ChannelSensitivity inDb = getSensitivity(sensitivity.getChannel());
        if (inDb != null) {
            sensitivity.setDbid(inDb.getDbid());
            getSession().evict(inDb);
        }
        sensitivity.setInputUnits(intern(sensitivity.getInputUnits()));
        getSession().saveOrUpdate(sensitivity);
    }
    
    @Deprecated
    public void putInstrumentation(ChannelImpl chan, Instrumentation inst) {
        logger.debug("Put instrumentation: "+ChannelIdUtil.toStringNoDates(chan));
        InstrumentationBlob ib = null;
        try {
            ib = getInstrumentationBlob(chan);
        } catch(ChannelNotFound e) {
            // must be new
        }
        if (ib == null) {
            ib = new InstrumentationBlob(chan, inst);
        } else {
            int dbid = ib.getDbid();
            getSession().evict(ib);
            ib = new InstrumentationBlob(chan, inst);
            ib.setDbid(dbid);
        }
        getSession().saveOrUpdate(ib);
    }

    public void putResponse(ChannelImpl chan, Response inst) {
        logger.debug("Put response: "+ChannelIdUtil.toStringNoDates(chan));
        InstrumentationBlob ib = null;
        try {
            ib = getInstrumentationBlob(chan);
        } catch(ChannelNotFound e) {
            // must be new
        }
        if (ib == null) {
            ib = new InstrumentationBlob(chan, inst);
        } else {
            int dbid = ib.getDbid();
            getSession().evict(ib);
            ib = new InstrumentationBlob(chan, inst);
            ib.setDbid(dbid);
        }
        getSession().saveOrUpdate(ib);
    }

    public void internUnit(StationImpl sta) {
        internUnit(sta.getLocation());
    }

    /**
     * assumes station has aready been interned as this needs to happen to avoid
     * dup stations.
     */
    public void internUnit(ChannelImpl chan) {
        internUnit(chan.getSite().getLocation());
        internUnit(chan.getSite().getStation());
        internUnit(chan.getSamplingInfo().interval);
    }

    private static NetworkDB singleton;

    public static NetworkDB getSingleton() {
        if(singleton == null) {
            singleton = new NetworkDB();
        }
        return singleton;
    }

    static String getStationByCodes = "SELECT s From "
            + StationImpl.class.getName()
            + " s WHERE s.networkAttr.id.network_code = :netCode AND s.id.station_code = :staCode";

    static String getAllStationsByCode = "SELECT s From "
            + StationImpl.class.getName()
            + " s WHERE s.id.station_code = :staCode";

    static String getStationByIdString = getStationByCodes
            + " AND sta_begin_time = :staBegin";

    static String getStationForNetwork = "From " + StationImpl.class.getName()
            + " s WHERE s.networkAttr = :netAttr";

    static String getChannelForNetwork = "From " + ChannelImpl.class.getName()
            + " WHERE site.station.networkAttr = :netAttr";

    static String getStationForNetworkStation = getStationForNetwork
            + " and s.id.station_code = :staCode";

    static String getChannelForStation = "From " + ChannelImpl.class.getName()
            + " c WHERE c.site.station = :station";

    static String getChannelGroupForStation = "From "
            + ChannelGroup.class.getName()
            + " c WHERE c.channel1.site.station = :station";

    // often happens that a channel has end time of 2500-01-01 until it is ended and a new channel is created
    // no way for sod to know, but when this happens, you usually want the channel that overlaps the time
    // with the latest begin time, hence the order by desc
    static String getChannelForStationAtTime = getChannelForStation
            + " and :when between chan_begin_time and chan_end_time  order by chan_begin_time desc";

    static String chanCodeHQL = " id.channel_code = :channelCode AND id.site_code = :siteCode AND id.station_code = :stationCode AND site.station.networkAttr.id.network_code = :netCode ";

    // often happens that a channel has end time of 2500-01-01 until it is ended and a new channel is created
    // no way for sod to know, but when this happens, you usually want the channel that overlaps the time
    // with the latest begin time, hence the order by desc
    static String getChannelByCode = "From " + ChannelImpl.class.getName()
            + " WHERE " + chanCodeHQL
            + " AND :when between chan_begin_time and chan_end_time order by chan_begin_time desc";

    static String getChannelById = "From " + ChannelImpl.class.getName()
            + " WHERE " + chanCodeHQL + " AND chan_begin_time =  :when";

    static String getAllStationsString = "From edu.iris.Fissures.network.StationImpl s";

    static String getAllNetsString = "From edu.iris.Fissures.network.NetworkAttrImpl n";

    static String getNetworkByCodeString = getAllNetsString
            + " WHERE network_code = :netCode";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkDB.class);
}
