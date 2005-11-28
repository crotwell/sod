package edu.sc.seis.sod.database;

import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;

/**
 * NetworkDbObject.java
 * 
 * 
 * Created: Tue Oct 22 14:31:30 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class NetworkDbObject extends DbObject {

    public NetworkDbObject(int dbid, ProxyNetworkAccess networkAccess) {
        super(dbid);
        this.networkAccess = networkAccess;
    }

    public ProxyNetworkAccess getNetworkAccess() {
        return this.networkAccess;
    }

    public String toString() {
        return networkAccess.get_attributes().get_code();
    }

    public StationDbObject[] stationDbObjects = null;

    private ProxyNetworkAccess networkAccess;
}// NetworkDbObject
