/**
 * AllTypeTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;


import edu.sc.seis.sod.status.eventArm.EventTemplate;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.NetworkAccess;

public abstract class AllTypeTemplate implements SiteTemplate, StationTemplate,
    GenericTemplate, NetworkTemplate, ChannelTemplate, EventTemplate{
    
    public abstract String getResult();
    
    public String getResult(NetworkAccess network) { return getResult(); }
    
    public String getResult(Station station) { return getResult(); }
    
    public String getResult(Site site) { return getResult(); }
    
    public String getResult(EventAccessOperations ev) { return getResult(); }
    
    public String getResult(Channel chan) { return getResult(); }
}

