package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * This tag is used to specify the value of the catalog.
 *<pre>
 * &lt;catalog&gt;&lt;value&gt;BIGQUAKE&lt;/value&gt;&lt;/catalog&gt;
 *</pre>
 */
public class Catalog implements OriginSubsetter{
    public Catalog (Element config){ this.config = config; }

    public StringTree accept(CacheEvent event, EventAttrImpl eventAttr, OriginImpl origin) {
        if(origin.getCatalog().equals(getCatalog())) return new StringTreeLeaf(this, true);
        return new StringTreeLeaf(this, false);
    }

    public String getCatalog() { return SodUtil.getNestedText(config); }

    private Element config;
}// Catalog
