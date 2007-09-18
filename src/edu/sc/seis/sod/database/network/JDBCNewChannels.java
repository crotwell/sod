/**
 * JDBCNewChannels.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database.network;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
import edu.sc.seis.sod.database.SodJDBC;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class JDBCNewChannels extends JDBCTable {

    public JDBCNewChannels() throws SQLException {
        super("newchannels", ConnMgr.createConnection());
        channelTable = new JDBCChannel();
        TableSetup.setup(this, "edu/sc/seis/sod/database/props/default.props");
        put = conn.prepareStatement("INSERT INTO newchannels (channelid) values (?)");
        getNext =  conn.prepareStatement("SELECT TOP 1 channelid FROM newchannels");
        remove = conn.prepareStatement("DELETE FROM newchannels WHERE channelid = ?");
    }

    public void put(int chanDBId) throws SQLException{
        put.setInt(1, chanDBId);
        put.executeUpdate();
    }

    public int put(Channel chan) throws SQLException, NotFound{
        int dbId = channelTable.getDBId(chan.get_id());
        put(dbId);
        return dbId;
    }

    public Channel getNext() throws SQLException, NotFound{
        ResultSet idRS = getNext.executeQuery();
        if (idRS.next()){
            return channelTable.get(idRS.getInt(1));
        }
        return null;
    }

    public void remove(Channel chan) throws SQLException, NotFound{
        int dbId = channelTable.getDBId(chan.get_id());
        remove.setInt(1, dbId);
        remove.executeUpdate();
    }

    private PreparedStatement put, getNext, remove;
    private JDBCChannel channelTable;
}

