package edu.sc.seis.sod.subsetter.site;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

/**
 * PassSiteSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class PassSite implements SiteSubsetter{

    public StringTree accept(Site site, NetworkAccess network) { return new Pass(this);}

}// NullSiteSubsetter
