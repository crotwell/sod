package edu.sc.seis.sod.hibernate;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.sod.model.station.NetworkId;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.model.station.StationId;

public class NetworkDB extends AbstractHibernateDB {

    protected NetworkDB() {}
    
    public int put(Network net) {
        Session session = getSession();
        if(net.getDbid() != 0) {
            session.saveOrUpdate(net);
            return net.getDbid();
        }
        Iterator<Network> fromDB = getNetworkByCode(net.getCode()).iterator();
        if(fromDB.hasNext()) {
            if(NetworkIdUtil.isTemporary(net.getCode())) {
                while(fromDB.hasNext()) {
                    Network indb = fromDB.next();
                    if(net.getCode().equals(indb.getCode())
                            && net.getStartYearString()
                                    .equals(indb.getStartYearString())) {
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
                Network indb = (Network)fromDB.next();
                net.associateInDB(indb);
                getSession().evict(indb);
                getSession().saveOrUpdate(net);
                return net.getDbid();
            }
        }
        return ((Integer)session.save(net)).intValue();
    }

    public int put(Station sta) {
        Integer dbid;
        if(((Network)sta.getNetworkAttr()).getDbid() == 0) {
            throw new IllegalArgumentException("Must put Network before put Station"+sta.toString()); 
        }
        internUnit(sta);
        if(sta.getDbid() != 0) {
            getSession().saveOrUpdate(sta);
            return sta.getDbid();
        }
        try {
            // maybe station is already in db, so update
            Station indb = getStationById(sta.get_id());
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
    public int put(Channel chan) {
        Integer dbid;
        internUnit(chan);
        if(((Station)chan.getSite().getStation()).getDbid() == 0) {
            try {
                chan.getSite().setStation(getStationById(chan.getSite()
                        .getStation()
                        .get_id()));
            } catch(NotFound e) {
                int staDbid = put((Station)chan.getSite().getStation());
            }
        }
        try {
            Channel indb = getChannel(chan.get_id());
            chan.associateInDB(indb);
            getSession().evict(indb);
            getSession().evict(indb.getSite().getStation());
            getSession().evict(indb.getSite().getStation().getNetworkAttr());
            getSession().saveOrUpdate(chan);
            dbid = chan.getDbid();
        } catch(NotFound nf) {
            dbid = (Integer)getSession().save(chan);
        }
        logger.debug("Put channel as "+dbid+" "+ChannelIdUtil.toStringFormatDates(chan)+"  sta dbid="+((Station)chan.getSite().getStation()).getDbid());
        return dbid.intValue();
    }

    public int put(ChannelGroup cg) {
        ChannelGroup indb = getChannelGroup(cg.getChannel1(), 
                                            cg.getChannel2(),
                                            cg.getChannel3());
        Channel[] chans = cg.getChannels();
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

    public List<ChannelGroup> getChannelGroup(Channel chan) {
        Query query = getSession().createQuery("from "
                + ChannelGroup.class.getName()
                + " where channel1 = :chan or channel2 = :chan or channel3 = :chan");
        query.setEntity("chan", chan);
        return query.list();
    }

    public ChannelGroup getChannelGroup(Channel chanA,
                                        Channel chanB,
                                        Channel chanC) {
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

    public List<Station> getStationByCodes(String netCode, String staCode) {
        Query query = getSession().createQuery(getStationByCodes);
        query.setString("netCode", netCode);
        query.setString("staCode", staCode);
        return query.list();
    }

    public List<Station> getAllStationsByCode(String staCode) {
        Query query = getSession().createQuery(getAllStationsByCode);
        query.setString("staCode", staCode);
        return query.list();
    }

    public Station getStationById(StationId staId) throws NotFound {
        Query query = getSession().createQuery(getStationByIdString);
        query.setString("netCode", staId.network_id.network_code);
        query.setString("staCode", staId.station_code);
        query.setTimestamp("staBegin",
                           staId.begin_time.getTimestamp());
        query.setMaxResults(1);
        List<Station> l = query.list();
        logger.debug("getStationById("+staId.network_id.network_code+"."+staId.station_code+"."+staId.begin_time.getISOString()+"  return size: "+l.size());
        if(l.size() != 0) {
            return l.get(0);
        }
        throw new NotFound();
    }

    public List<Network> getNetworkByCode(String netCode) {
        Query query = getSession().createQuery(getNetworkByCodeString);
        query.setString("netCode", netCode);
        return query.list();
    }

    public Network getNetwork(int dbid) throws NotFound {
        Network out = (Network)getSession().get(Network.class,
                                                                new Integer(dbid));
        if(out == null) {
            throw new NotFound();
        }
        return out;
    }

    public List<Network> getAllNetworks() {
        Query query = getSession().createQuery(getAllNetsString);
        return query.list();
    }

    public Station getStation(int dbid) throws NotFound {
        Station out = (Station)getSession().get(Station.class,
                                                        new Integer(dbid));
        if(out == null) {
            throw new NotFound();
        }
        return out;
    }

    public Station[] getAllStations() {
        Query query = getSession().createQuery(getAllStationsString);
        List result = query.list();
        return (Station[])result.toArray(new Station[0]);
    }

    public List<Station> getStationForNet(Network attr) {
        Query query = getSession().createQuery(getStationForNetwork);
        query.setEntity("netAttr", attr);
        return query.list();
    }

    public List<Station> getStationForNet(Network attr,
                                              String staCode) {
        Query query = getSession().createQuery(getStationForNetworkStation);
        query.setEntity("netAttr", attr);
        query.setString("staCode", staCode);
        return query.list();
    }

    public List<Channel> getChannelsForNet(Network attr) {
        Query query = getSession().createQuery(getChannelForNetwork);
        query.setEntity("netAttr", attr);
        return query.list();
    }

    public Channel getChannel(int dbid) throws NotFound {
        Channel out = (Channel)getSession().get(Channel.class,
                                                        new Integer(dbid));
        if(out == null) {
            throw new NotFound();
        }
        return out;
    }

    public List<Channel> getAllChannels() {
        return getSession().createQuery("from " + Channel.class.getName())
                .list();
    }

    public List<Channel> getChannelsForStation(Station station) {
        Query query = getSession().createQuery(getChannelForStation);
        query.setEntity("station", station);
        List<Channel> out = query.list();
        logger.debug("getChannelsForStation("+station.getDbid()+" found "+out.size()+"  query="+query);
        return out;
    }

    public List<ChannelGroup> getChannelGroupsForStation(Station station) {
        Query query = getSession().createQuery(getChannelGroupForStation);
        query.setEntity("station", station);
        return query.list();
    }

    public List<Channel> getChannelsForStation(Station station,
                                               Instant when) {
        Query query = getSession().createQuery(getChannelForStationAtTime);
        query.setEntity("station", station);
        query.setTimestamp("when", when);
        return query.list();
    }

    public Channel getChannel(String net,
                                  String sta,
                                  String site,
                                  String chan,
                                  Instant when) throws NotFound {
        return getChannel(net, sta, site, chan, when, getChannelByCode);
    }

    public List<Channel> getChannelsByCode(NetworkId net,
                                               String sta,
                                               String site,
                                               String chan) {
        String queryString = "From " + Channel.class.getName() + " WHERE "
                + chanCodeHQL
                + " AND site.station.networkAttr.beginTime.time = :when";
        Query query = getSession().createQuery(queryString);
        query.setString("netCode", net.network_code);
        query.setString("stationCode", sta);
        query.setString("siteCode", site);
        query.setString("channelCode", chan);
        query.setTimestamp("when",
                           net.begin_time.getTimestamp());
        return query.list();
    }

    public Channel getChannel(ChannelId id) throws NotFound {
        return getChannel(id.network_id.network_code,
                          id.station_code,
                          id.site_code,
                          id.channel_code,
                          id.begin_time,
                          getChannelById);
    }

    protected Channel getChannel(String net,
                                     String sta,
                                     String site,
                                     String chan,
                                     Instant when,
                                     String queryString) throws NotFound {
        Query query = getSession().createQuery(queryString);
        query.setString("netCode", net);
        query.setString("stationCode", sta);
        String sc = site.trim();
        if (sc.equals("--") || sc.equals("") || sc.equals("  ")) {sc = edu.sc.seis.seisFile.fdsnws.stationxml.Channel.EMPTY_LOC_CODE;}
        query.setString("siteCode", sc);
        query.setString("channelCode", chan);
        query.setTimestamp("when", when);
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() == 0) {
            throw new NotFound();
        }
        return (Channel)result.get(0);
    }

    public InstrumentationBlob getInstrumentationBlob(Channel chan) throws ChannelNotFound {
        Query query = getSession().createQuery("from "+InstrumentationBlob.class.getName()+" where channel = :chan");
        query.setEntity("chan", chan);
        Iterator it = query.iterate();
        if (it.hasNext()) {
            InstrumentationBlob ib = (InstrumentationBlob)it.next();
            return ib;
        }
        return null;    
    }

    
    public Response getResponse(Channel chan) throws ChannelNotFound {
        InstrumentationBlob ib = getInstrumentationBlob(chan);
        if (ib != null) {
            Response resp =  ib.getResponse(); // might be null, meaning no inst exists, but blob in DB so we tried before
            if (resp == null) { throw new ChannelNotFound(chan); }
            return resp;
        }
        return null; // instBlob null, so never seen this channel before
    }
    
    public ChannelSensitivity getSensitivity(Channel chan) {
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

    public void putResponse(Channel chan, Response inst) {
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

    public void internUnit(Station sta) {
        internUnit(sta.getLocation());
    }

    /**
     * assumes station has aready been interned as this needs to happen to avoid
     * dup stations.
     */
    public void internUnit(Channel chan) {
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
            + Station.class.getName()
            + " s WHERE s.networkAttr.id.network_code = :netCode AND s.id.station_code = :staCode";

    static String getAllStationsByCode = "SELECT s From "
            + Station.class.getName()
            + " s WHERE s.id.station_code = :staCode";

    static String getStationByIdString = getStationByCodes
            + " AND sta_begin_time = :staBegin";

    static String getStationForNetwork = "From " + Station.class.getName()
            + " s WHERE s.networkAttr = :netAttr";

    static String getChannelForNetwork = "From " + Channel.class.getName()
            + " WHERE site.station.networkAttr = :netAttr";

    static String getStationForNetworkStation = getStationForNetwork
            + " and s.id.station_code = :staCode";

    static String getChannelForStation = "From " + Channel.class.getName()
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
    static String getChannelByCode = "From " + Channel.class.getName()
            + " WHERE " + chanCodeHQL
            + " AND :when between chan_begin_time and chan_end_time order by chan_begin_time desc";

    static String getChannelById = "From " + Channel.class.getName()
            + " WHERE " + chanCodeHQL + " AND chan_begin_time =  :when";

    static String getAllStationsString = "From edu.iris.Fissures.network.StationImpl s";

    static String getAllNetsString = "From edu.iris.Fissures.network.NetworkAttrImpl n";

    static String getNetworkByCodeString = getAllNetsString
            + " WHERE network_code = :netCode";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkDB.class);
}
