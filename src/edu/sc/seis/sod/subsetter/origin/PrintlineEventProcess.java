package edu.sc.seis.sod.subsetter.origin;

import java.io.IOException;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

public class PrintlineEventProcess implements OriginSubsetter {

    public PrintlineEventProcess(Element config) {
        filenameTemplate = extractFilename(config);
        template = extractTemplate(config);
    }

    private static String extractTemplate(Element config) {
        return DOMHelper.extractText(config, "template", DEFAULT_TEMPLATE);
    }

    public static String extractFilename(Element config) {
        return DOMHelper.extractText(config, "filename", "");
    }

    public boolean accept(EventAccessOperations event,
                          EventAttr attr,
                          Origin origin) throws IOException {
        velocitizer.evaluate(filenameTemplate, template, event);
        return true;
    }

    public static final String DEFAULT_TEMPLATE = "$event.region ($event.latitude, $event.longitude) $event.time $event.magnitude";

    private PrintlineVelocitizer velocitizer = new PrintlineVelocitizer();

    private String filenameTemplate, template;
}// PrintlineEventProcess
