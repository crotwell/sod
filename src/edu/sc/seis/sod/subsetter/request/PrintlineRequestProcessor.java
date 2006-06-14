package edu.sc.seis.sod.subsetter.request;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

public class PrintlineRequestProcessor implements Request {

    public PrintlineRequestProcessor(Element config)
            throws ConfigurationException {
        filename = DOMHelper.extractText(config, "filename", "");
        template = DOMHelper.extractText(config, "template", DEFAULT_TEMPLATE);
        velocitizer = new PrintlineVelocitizer(new String[] {filename, template});
    }

    private PrintlineVelocitizer velocitizer;

    private String template, filename;

    public static final String DEFAULT_TEMPLATE = "Got $originalRequests.size()";

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] request,
                          CookieJar cookieJar) throws Exception {
        velocitizer.evaluate(filename,
                             template,
                             event,
                             channel,
                             request,
                             cookieJar);
        return true;
    }
}
