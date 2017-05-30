/**
 * SiteTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import edu.sc.seis.seisFile.fdsnws.stationxml.Site;

public interface SiteTemplate {

    public String getResult(Site site);
    
}

