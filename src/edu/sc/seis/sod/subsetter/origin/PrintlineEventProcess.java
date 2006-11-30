package edu.sc.seis.sod.subsetter.origin;

import java.io.IOException;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AbstractPrintlineProcess;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

public class PrintlineEventProcess extends AbstractPrintlineProcess implements OriginSubsetter {

    public PrintlineEventProcess(Element config) throws ConfigurationException {
        super(config);
    }


    public StringTree accept(EventAccessOperations event,
                             EventAttr attr,
                             Origin origin) throws IOException {
        velocitizer.evaluate(filename, template, event);
        return new StringTreeLeaf(this, true);
    }

    public static final String DEFAULT_TEMPLATE = "$event.region ($event.latitude, $event.longitude) $event.time $event.magnitude";

    public String getDefaultTemplate() {
        return DEFAULT_TEMPLATE;
    }
}// PrintlineEventProcess
