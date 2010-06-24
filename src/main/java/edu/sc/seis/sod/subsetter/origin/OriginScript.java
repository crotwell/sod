package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;


public class OriginScript extends AbstractScriptSubsetter implements OriginSubsetter {

    public OriginScript(Element config) {
        super(config);
        // TODO Auto-generated constructor stub
    }

    @Override
    public StringTree accept(CacheEvent eventAccess, EventAttr eventAttr, Origin preferredOrigin) throws Exception {
        engine.put("eventAccess", eventAccess);
        engine.put("eventAttr", eventAttr);
        engine.put("preferredOrigin", preferredOrigin);
        return eval();
    }
}
