package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;


public class OriginScript extends AbstractScriptSubsetter implements OriginSubsetter {

    public OriginScript(Element config) {
        super(config);
        // TODO Auto-generated constructor stub
    }

    @Override
    public StringTree accept(CacheEvent eventAccess, EventAttrImpl eventAttr, OriginImpl preferredOrigin) throws Exception {
        engine.put("eventAccess", new VelocityEvent(eventAccess));
        engine.put("eventAttr", eventAttr);
        engine.put("preferredOrigin", preferredOrigin);
        return eval();
    }
}
