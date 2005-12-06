package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

/**
 * This tag is used to specify the value of the catalog.
 *<pre>
 * &lt;catalog&gt;&lt;value&gt;BIGQUAKE&lt;/value&gt;&lt;/catalog&gt;
 *</pre>
 */
public class Catalog implements OriginSubsetter{
    public Catalog (Element config){ this.config = config; }

    public StringTree accept(EventAccessOperations event, EventAttr eventAttr, Origin origin) {
        if(origin.catalog.equals(getCatalog())) return new StringTreeLeaf(this, true);
        return new StringTreeLeaf(this, false);
    }

    public String getCatalog() { return SodUtil.getNestedText(config); }

    private Element config;
}// Catalog
