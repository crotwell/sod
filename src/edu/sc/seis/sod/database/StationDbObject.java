package edu.sc.seis.sod.database;

import edu.iris.Fissures.IfNetwork.*;

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

    public SiteDbObject[] siteDbObjects = null;

    private Station station;
    
}// StationDbObject
