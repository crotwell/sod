package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.ConfigurationException;
import java.util.Iterator;
import org.w3c.dom.Element;

/**
 * siteOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 *the max value of the sequence is unLimited.
 *
 * sample xml file
 *<body><pre><bold>
 *&lt;siteOR&gt;
 *&lt;/siteOR&gt;
 * </bold></pre></body>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public final class SiteOR extends  NetworkLogicalSubsetter
    implements SiteSubsetter {
    public SiteOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Site e) {
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            SiteSubsetter filter = (SiteSubsetter)it.next();
            if ( filter.accept(e)) {
                return true;
            }
        }
        return false;
    }

}// SiteOR
