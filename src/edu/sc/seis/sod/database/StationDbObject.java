package edu.sc.seis.sod.database;

import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.StationIdUtil;

/**
 * StationDbObject.java
 *
 *
 * Created: Tue Oct 22 14:33:44 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class StationDbObject extends DbObject{
    public StationDbObject (int dbid, Station station){
        super(dbid);
        this.station = station;
    }
    
    public Station getStation() {
        return this.station;
    }
    
    public String toString(){
        return StationIdUtil.toString(station.get_id());
    }
    
    public SiteDbObject[] siteDbObjects = null;
    
    private Station station;
    
}// StationDbObject
