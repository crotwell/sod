/**
 * JDBCNetworkUnifier.java
 *
 * allows access to all of the underlying networkDb interfaces from a single
 * class
 */

package edu.sc.seis.sod.database.network;

import edu.iris.Fissures.IfNetwork.*;

import edu.sc.seis.fissuresUtil.cache.BulletproofNetworkAccess;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCLocation;
import edu.sc.seis.fissuresUtil.database.JDBCQuantity;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.database.network.JDBCNetwork;
import edu.sc.seis.fissuresUtil.database.network.JDBCSite;
import edu.sc.seis.fissuresUtil.database.network.JDBCStation;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.SiteDbObject;
import java.sql.Connection;
import java.sql.SQLException;

public class JDBCNetworkUnifier{
    public JDBCNetworkUnifier() throws SQLException{
        this(ConnMgr.createConnection());
    }

    public JDBCNetworkUnifier(Connection conn) throws SQLException{
        JDBCTime timeDb = new JDBCTime();
        this.netDb = new JDBCNetwork(conn, timeDb);
        JDBCQuantity quantDb = new JDBCQuantity(conn);
        JDBCLocation locDb = new JDBCLocation(conn, quantDb);
        this.stationDb = new JDBCStation(conn, locDb, netDb, timeDb);
        this.siteDb = new JDBCSite(conn, locDb, stationDb, timeDb);
        this.chanDb = new JDBCChannel(conn, quantDb, siteDb, timeDb);
    }

    public ChannelDbObject getChannel(int chanDbId) throws NotFound, SQLException{
        return new ChannelDbObject(chanDbId, chanDb.get(chanDbId));
    }

    public int put(Channel chan) throws SQLException{ return chanDb.put(chan); }

    public SiteDbObject getSite(int siteDbId) throws NotFound, SQLException{
        return new SiteDbObject(siteDbId, siteDb.get(siteDbId));
    }

    public int put(Site site) throws SQLException{ return siteDb.put(site); }

    public Station getStation(int stationDbId) throws NotFound, SQLException{
        return stationDb.get(stationDbId);
    }

    public int put(Station station) throws SQLException{
        return stationDb.put(station);
    }

    public NetworkAttr getNet(int netDbId) throws NotFound, SQLException{
        return netDb.get(netDbId);
    }

    public NetworkDbObject getNet(int netDbId, NetworkDCOperations ndc) throws NetworkNotFound, NotFound, SQLException{
        NetworkId id = netDb.get(netDbId).get_id();
        NetworkAccess na = new BulletproofNetworkAccess(ndc.a_finder().retrieve_by_id(id), ndc, id);
        return new NetworkDbObject(netDbId, na);
    }

    public NetworkDbObject getNet(ChannelDbObject chan, NetworkFinder nf) throws NotFound, SQLException, NetworkNotFound{
        int netDbId = netDb.getDBId(chan.getChannel().get_id().network_id);
        return new NetworkDbObject(netDbId, nf.retrieve_by_id(chan.getChannel().get_id().network_id));
    }

    public int put(NetworkAttr net) throws SQLException{
        return netDb.put(net);
    }

    public NetworkDbObject[] getAllNets(NetworkDCOperations ndc) throws SQLException, NotFound, NetworkNotFound {
        int[] netIds = netDb.getAllNetworkDBIds();
        NetworkDbObject[] out = new NetworkDbObject[netIds.length];
        for (int i = 0; i < netIds.length; i++) {
            out[i] = getNet(netIds[i], ndc);
        }
        return out;
    }

    private JDBCNetwork netDb;
    private JDBCChannel chanDb;
    private JDBCSite siteDb;
    private JDBCStation stationDb;
}

