package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.ConfigurationException;
import org.w3c.dom.Element;

/**
 * siteXOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 *the max value of the sequence is unLimited.
 *
 * sample xml file
 *<body><pre><bold>
 *&lt;siteXOR&gt;
 *&lt;/siteXOR&gt;
 * </bold></pre></body>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public final class SiteXOR
    extends  NetworkLogicalSubsetter implements SiteSubsetter {

    public SiteXOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Site site) {
        SiteSubsetter filterA = (SiteSubsetter)filterList.get(0);
        SiteSubsetter filterB = (SiteSubsetter)filterList.get(1);
        return ( filterA.accept(site) != filterB.accept(site));

    }

}// SiteXOR
