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
    }

    @Override
    public StringTree accept(CacheEvent eventAccess, EventAttrImpl eventAttr, OriginImpl preferredOrigin) throws Exception {
        return runScript(new VelocityEvent(eventAccess), eventAttr, preferredOrigin);
    }

    /** Run the script with the arguments as predefined variables. */
    public StringTree runScript(VelocityEvent event, EventAttrImpl eventAttr, OriginImpl preferredOrigin) throws Exception {
        engine.put("eventAccess", event);
        engine.put("eventAttr", eventAttr);
        engine.put("preferredOrigin", preferredOrigin);
        return eval();
    }
}
