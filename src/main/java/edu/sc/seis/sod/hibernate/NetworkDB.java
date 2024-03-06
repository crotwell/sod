package edu.sc.seis.sod.hibernate;

import java.io.IOException;
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
                        net.associateInDb(indb);
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
                net.associateInDb(indb);
                getSession().evict(indb);
                getSession().saveOrUpdate(net);
                return net.getDbid();
            }
        }
        return ((Integer)session.save(net)).intValue();
    }

    public int put(Station sta) {
        Integer dbid;
        if(((Network)sta.getNetwork()).getDbid() == 0) {
            throw new IllegalArgumentException("Must put Network before put Station"+sta.toString()); 
        }
        internUnit(sta);
        if(sta.getDbid() != 0) {
            getSession().saveOrUpdate(sta);
            return sta.getDbid();
        }
        try {
            // maybe station is already in db, so update
            Station indb = getStationById(sta);
            sta.associateInDb(indb);
            getSession().evict(indb);
            getSession().evict(indb.getNetwork());
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
        if(((Station)chan.getStation()).getDbid() == 0) {
            try {
                chan.setStation(getStationById(chan.getStation()));
            } catch(NotFound e) {
                int staDbid = put((Station)chan.getStation());
            }
        }
        try {
            Channel indb = getChannelById(chan);
            chan.associateInDb(indb);
            getSession().evict(indb);
            getSession().evict(indb.getStation());
            getSession().evict(indb.getStation().getNetwork());
            getSession().saveOrUpdate(chan);
            dbid = chan.getDbid();
        } catch(NotFound nf) {
            dbid = (Integer)getSession().save(chan);
        }
        logger.debug("Put channel as "+dbid+" "+ChannelIdUtil.toStringFormatDates(chan)+"  sta dbid="+((Station)chan.getStation()).getDbid());
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
                    chans[i] = getChannelById(chans[i]);
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
        query.setParameter("chan", chan);
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
        query.setParameter("chanA", chanA);
        query.setParameter("chanB", chanB);
        query.setParameter("chanC", chanC);
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

    public Station getStationById(Station sta) throws NotFound {
        return getStationById(StationId.of(sta));
    }
    
    public Station getStationById(StationId staId) throws NotFound {
        Query query = getSession().createQuery(getStationByIdString);
        query.setString("netCode", staId.getNetworkId());
        query.setString("staCode", staId.getStationCode());
        query.setParameter("staStart",
                           staId.getStartTime());
        query.setMaxResults(1);
        List<Station> l = query.list();
        logger.debug("getStationById("+staId.getNetworkId()+"."+staId.getStationCode()+"."+staId.getStartTime()+"  return size: "+l.size());
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
        query.setParameter("netAttr", attr);
        return query.list();
    }

    public List<Station> getStationForNet(Network attr,
                                              String staCode) {
        Query query = getSession().createQuery(getStationForNetworkStation);
        query.setParameter("netAttr", attr);
        query.setString("staCode", staCode);
        return query.list();
    }

    public List<Channel> getChannelsForNet(Network attr) {
        Query query = getSession().createQuery(getChannelForNetwork);
        query.setParameter("netAttr", attr);
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
        query.setParameter("station", station);
        List<Channel> out = query.list();
        logger.debug("getChannelsForStation("+station.getDbid()+" found "+out.size()+"  query="+query);
        return out;
    }

    public List<ChannelGroup> getChannelGroupsForStation(Station station) {
        if (! getSession().contains(station) ) {
            getSession().saveOrUpdate(station);
        }
        Query query = getSession().createQuery(getChannelGroupForStation);
        query.setParameter("station", station);
        List<ChannelGroup> out =  query.list();
        // because station is in each of 3 channels but only queried on station in first channel,
        // hibernate may not set station so do it manually, this might be bad, but   :(
logger.info("NetworkDB replace station to avoid null "+station);
        for (ChannelGroup cg: out) {
            for (Channel c: cg.getChannels()) {
                c.setStation(station);
            }
        }
        return out;
    }

    public List<Channel> getChannelsForStation(Station station,
                                               Instant when) {
        Query query = getSession().createQuery(getChannelForStationAtTime);
        query.setParameter("station", station);
        query.setParameter("when", when);
        return query.list();
    }

    public Channel getChannel(String net,
                                  String sta,
                                  String loc,
                                  String chan,
                                  Instant when) throws NotFound {
        return getChannel(net, sta, loc, chan, when, getChannelByCode);
    }

    public List<Channel> getChannelsByCode(NetworkId net,
                                               String sta,
                                               String loc,
                                               String chan) {
        String queryString = "From " + Channel.class.getName() + " WHERE "
                + chanCodeHQL
                + " AND station.network.startDateTime = :when";
        Query query = getSession().createQuery(queryString);
        query.setString("netCode", net.getNetworkCode());
        query.setString("stationCode", sta);
        query.setString("locCode", loc);
        query.setString("channelCode", chan);
        query.setParameter("when",
                           net.getStartYear());
        return query.list();
    }

    public Channel getChannelById(Channel chan) throws NotFound {
        return getChannel(ChannelId.of(chan));
    }

    public Channel getChannel(ChannelId id) throws NotFound {
        return getChannel(id.getNetworkId(),
                          id.getStationCode(),
                          id.getLocCode(),
                          id.getChannelCode(),
                          id.getStartTime(),
                          getChannelById);
    }

    protected Channel getChannel(String net,
                                     String sta,
                                     String loc,
                                     String chan,
                                     Instant when,
                                     String queryString) throws NotFound {
        Query query = getSession().createQuery(queryString);
        query.setString("netCode", net);
        query.setString("stationCode", sta);
        String sc = loc.trim();
        if (sc.equals("--") || sc.equals("") || sc.equals("  ")) {sc = edu.sc.seis.seisFile.fdsnws.stationxml.Channel.EMPTY_LOC_CODE;}
        query.setString("locCode", sc);
        query.setString("channelCode", chan);
        query.setParameter("when", when);
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() == 0) {
            throw new NotFound();
        }
        return (Channel)result.get(0);
    }


    public InstrumentationBlob getInstrumentationBlob(Channel chan) throws ChannelNotFound {
        String queryString = "WHERE channel = :chan";
        logger.warn("NetworkDB.getInstrumentationBlob : "+queryString);
        Query query = getSession().createQuery(queryString, InstrumentationBlob.class);
        query.setParameter("chan", chan);
        Iterator it = query.iterate();
        if (it.hasNext()) {
            InstrumentationBlob ib = (InstrumentationBlob)it.next();
            return ib;
        }
        return null;    
    }

    
    public Response getResponse(Channel chan) throws ChannelNotFound {
        try {
            return instrumentationDB.load(chan);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ChannelSensitivity getSensitivity(Channel chan) {
        Query query = getSession().createQuery("FROM "+ChannelSensitivity.class.getName()+" WHERE channel = :chan");
        query.setParameter("chan", chan);
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
        try {
            instrumentationDB.save(chan, inst);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public void internUnit(Station sta) {
       // System.err.println("Intern Units Station???");
       // throw new RuntimeException("intern units!!!");
//        internUnit(sta.getLocation());
    }

    /**
     * assumes station has already been interned as this needs to happen to avoid
     * dup stations.
     */
    @Deprecated
    public void internUnit(Channel chan) {
      //  System.err.println("Intern Units Channel???");
      //  throw new RuntimeException("intern units!!!");
//        internUnit(chan.getSite().getLocation());
//        internUnit(chan.getSite().getStation());
//        internUnit(chan.getSamplingInfo().interval);
    }

    private static NetworkDB singleton;

    public static NetworkDB getSingleton() {
        if(singleton == null) {
            singleton = new NetworkDB();
        }
        return singleton;
    }

    public static InstrumentationDB instrumentationDB;

    static String getStationByCodes = "SELECT s From "
            + Station.class.getName()
            + " s WHERE s.network.networkCode = :netCode AND s.stationCode = :staCode";

    static String getAllStationsByCode = "SELECT s From "
            + Station.class.getName()
            + " s WHERE s.stationCode = :staCode";

    static String getStationByIdString = getStationByCodes
            + " AND s.startDateTime = :staStart";

    static String getStationForNetwork = "From " + Station.class.getName()
            + " s WHERE s.network = :netAttr";

    static String getChannelForNetwork = "From " + Channel.class.getName()
            + " WHERE station.network = :netAttr";

    static String getStationForNetworkStation = getStationForNetwork
            + " and s.stationCode = :staCode";

    static String getChannelForStation = "From " + Channel.class.getName()
            + " c WHERE c.station = :station";

    static String getChannelGroupForStation = "From "
            + ChannelGroup.class.getName()
            + " c WHERE c.channel1.station = :station";

    // often happens that a channel has end time of 2500-01-01 until it is ended and a new channel is created
    // no way for sod to know, but when this happens, you usually want the channel that overlaps the time
    // with the latest start time, hence the order by desc
    static String getChannelForStationAtTime = getChannelForStation
            + " and :when between c.startDateTime and c.endDateTime  order by c.startDateTime desc";

    static String chanCodeHQL = " channelCode = :channelCode AND locCode = :locCode AND station.stationCode = :stationCode AND station.network.networkCode = :netCode ";

    // often happens that a channel has end time of 2500-01-01 until it is ended and a new channel is created
    // no way for sod to know, but when this happens, you usually want the channel that overlaps the time
    // with the latest start time, hence the order by desc
    static String getChannelByCode = "From " + Channel.class.getName()
            + " WHERE " + chanCodeHQL
            + " AND :when between startDateTime and endDateTime order by startDateTime desc";

    static String getChannelById = "From " + Channel.class.getName()
            + " WHERE " + chanCodeHQL + " AND startDateTime =  :when";

    static String getAllStationsString = "From edu.sc.seis.seisFile.fdsnws.stationxml.Station s";

    static String getAllNetsString = "From edu.sc.seis.seisFile.fdsnws.stationxml.Network n";

    static String getNetworkByCodeString = getAllNetsString
            + " WHERE networkCode = :netCode";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkDB.class);
}
