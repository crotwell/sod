package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.ConfigurationException;
import java.util.Iterator;
import org.w3c.dom.Element;

/**
 * siteNOT contains a sequence of channelSubsetters. The minimum value of the sequence is 1 and
 *the max value of the sequence is 1.
 *
 * sample xml file
 *<body><pre><bold>
 *&lt;siteNOT&gt;
 *  &lt;siteDepthRange&gt;
 *      &lt;unitRange&gt;
 *           &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *           &lt;min&gt;10&lt;/min&gt;
 *           &lt;max&gt;100&lt;/max&gt;
 *      &lt;/unitRange&gt;
 *  &lt;/siteDepthRange&gt;
 *&lt;/siteNOT&gt;
 * </bold></pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public final class SiteNOT extends  NetworkLogicalSubsetter
    implements SiteSubsetter {

    public SiteNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Site e) {
        Iterator it = filterList.iterator();
        if (it.hasNext()) {
            SiteSubsetter filter = (SiteSubsetter)it.next();
            if ( filter.accept(e)) { return false; }
        }
        return true;
    }
}// SiteNOT
