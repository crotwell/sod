package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Site;

/**
 * NullSiteSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class  NullSiteSubsetter implements SiteSubsetter{

    public boolean accept(Site site) { return true;}

}// NullSiteSubsetter
