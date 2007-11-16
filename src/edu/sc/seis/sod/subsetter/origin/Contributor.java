package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

/**
 * This tag is used to specify the value of the catalog.
 *<pre>
 * &lt;contributor&gt;&lt;value&gt;NEIC&lt;/value&gt;&lt;/contributor&gt;
 *</pre>
 */


public class Contributor implements OriginSubsetter{
    public Contributor (Element config){this.config = config;}

    /**
     * returns true if the contributor of the origin is same as the corresponding
     * contributor specified in the configuration file.
     */
    public StringTree accept(CacheEvent event, EventAttr eventAttr, Origin origin) {
        if(origin.contributor.equals(getContributor())) return new StringTreeLeaf(this, true);
        return new StringTreeLeaf(this, false);
    }

    /**
     * returns the contributor.
     */
    public String getContributor() {
        return SodUtil.getNestedText(config);
    }

    private Element config;
}// Contributor
