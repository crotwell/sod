package edu.sc.seis.sod.subsetter.networkArm;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * SiteSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:05:33 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface SiteSubsetter extends Subsetter{

    public boolean accept(Site site);

}// SiteSubsetter
