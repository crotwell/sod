package edu.sc.seis.sod.subsetter.origin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public class PrintlineEventProcess implements OriginSubsetter {

    public PrintlineEventProcess(Element config) {
        filename = DOMHelper.extractText(config, "filename", "");
        template = DOMHelper.extractText(config, "template", DEFAULT_TEMPLATE);
    }

    public boolean accept(EventAccessOperations event,
                          EventAttr attr,
                          Origin origin) throws IOException {
        String eventStr = velocitizer.evaluate(template, event);
        if(filename.equals("")) {
            System.out.println(eventStr);
        } else {
            FileWriter fwriter = new FileWriter(filename, true);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(eventStr);
            bwriter.newLine();
            bwriter.close();
        } // end of else
        return true;
    }

    private String template;

    private static final String DEFAULT_TEMPLATE = "$event.region Lat: $event.latitude Lon: $event.longitude $event.time $event.magnitude $event.depth";

    private SimpleVelocitizer velocitizer = new SimpleVelocitizer();

    private String filename;
}// PrintlineEventProcess
