package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

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
    extends  NetworkLogicalSubsetter
    implements SiteSubsetter {

    /**
     * Creates a new <code>SiteXOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public SiteXOR (Element config) throws ConfigurationException {
    super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param e a <code>Site</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAccess network, Site site,  CookieJar cookies) {
        SiteSubsetter filterA = (SiteSubsetter)filterList.get(0);
        SiteSubsetter filterB = (SiteSubsetter)filterList.get(1);
        return ( filterA.accept(network, site, cookies) != filterB.accept(network, site, cookies));

    }

}// SiteXOR
