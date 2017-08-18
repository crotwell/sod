package edu.sc.seis.sod.subsetter.request;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractPrintlineProcess;

public class PrintlineRequest extends AbstractPrintlineProcess implements RequestSubsetter {

    public PrintlineRequest(Element config)
            throws ConfigurationException {
        super(config);
    }
    
    public static final String DEFAULT_TEMPLATE = "Request: $originalRequests.size() from $channel for $event #foreach($req in $request), $req.start_time.date_time to $req.end_time.date_time#end";

    public StringTree accept(CacheEvent event,
                          Channel channel,
                          RequestFilter[] request,
                          CookieJar cookieJar) throws Exception {
        velocitizer.evaluate(filename,
                             template,
                             event,
                             channel,
                             request,
                             cookieJar);
        return new Pass(this);
    }

    @Override
    public String getDefaultTemplate() {
        return DEFAULT_TEMPLATE;
    }
}
