/**
 * AllTypeTemplate.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.status;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.seisFile.fdsnws.stationxml.Site;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.status.eventArm.EventTemplate;

public abstract class AllTypeTemplate implements SiteTemplate, StationTemplate,
        GenericTemplate, NetworkTemplate, ChannelTemplate, EventTemplate {

    public abstract String getResult();

    public String getResult(NetworkAttr network) {
        return getResult();
    }

    public String getResult(Station station) {
        return getResult();
    }

    public String getResult(Site site) {
        return getResult();
    }

    public String getResult(CacheEvent ev) {
        return getResult();
    }

    public String getResult(Channel chan) {
        return getResult();
    }
}
