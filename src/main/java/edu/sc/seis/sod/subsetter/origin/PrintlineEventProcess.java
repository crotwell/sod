package edu.sc.seis.sod.subsetter.origin;

import java.io.IOException;

import org.w3c.dom.Element;

import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AbstractPrintlineProcess;

public class PrintlineEventProcess extends AbstractPrintlineProcess implements OriginSubsetter {

    public PrintlineEventProcess(Element config) throws ConfigurationException {
        super(config);
    }


    public StringTree accept(CacheEvent event,
                             EventAttrImpl attr,
                             OriginImpl origin) throws IOException {
        velocitizer.evaluate(filename, template, event);
        return new StringTreeLeaf(this, true);
    }

    public static final String DEFAULT_TEMPLATE = "$event.region ($event.latitude, $event.longitude) $event.depth $event.time $event.magnitude";

    public String getDefaultTemplate() {
        return DEFAULT_TEMPLATE;
    }
}// PrintlineEventProcess
