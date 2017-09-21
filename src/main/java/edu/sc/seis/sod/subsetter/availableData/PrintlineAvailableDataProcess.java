package edu.sc.seis.sod.subsetter.availableData;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

public class PrintlineAvailableDataProcess implements AvailableDataSubsetter {

    public PrintlineAvailableDataProcess(Element config)
            throws ConfigurationException {
        filename = DOMHelper.extractText(config, "filename", "");
        template = DOMHelper.extractText(config, "template", DEFAULT_TEMPLATE);
        velocitizer = new PrintlineVelocitizer(new String[] {filename, template});
    }

    private PrintlineVelocitizer velocitizer;

    private String template, filename;

    public static final String DEFAULT_TEMPLATE = "Got $availableRequests.size() for $channel";

    public StringTree accept(CacheEvent event,
                             Channel channel,
                             RequestFilter[] request,
                             RequestFilter[] available,
                             MeasurementStorage cookieJar) throws Exception {
        velocitizer.evaluate(filename,
                             template,
                             event,
                             channel,
                             request,
                             available,
                             cookieJar);
        return new StringTreeLeaf(this, true);
    }
}
