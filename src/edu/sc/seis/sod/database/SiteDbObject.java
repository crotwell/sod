package edu.sc.seis.sod.database;

import edu.iris.Fissures.IfNetwork.*;

/**
 * SiteDbObject.java
 *
 *
 * Created: Tue Oct 22 14:35:35 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class SiteDbObject extends DbObject{
    public SiteDbObject (int dbid, Site site){
	super(dbid);
	this.site = site;
    }

    public Site getSite() {
	return this.site;
    }

    public ChannelDbObject[] channelDbObjects = null;

    private Site site;
    
}// SiteDbObject
