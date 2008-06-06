package edu.sc.seis.sod.subsetter.availableData;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
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

    public static final String DEFAULT_TEMPLATE = "Got $availableRequests.size()";

    public StringTree accept(CacheEvent event,
                             ChannelImpl channel,
                             RequestFilter[] original,
                             RequestFilter[] available,
                             CookieJar cookieJar) throws Exception {
        velocitizer.evaluate(filename,
                             template,
                             event,
                             channel,
                             original,
                             available,
                             cookieJar);
        return new StringTreeLeaf(this, true);
    }
}
