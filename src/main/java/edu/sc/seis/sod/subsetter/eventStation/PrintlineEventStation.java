package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractPrintlineProcess;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

public class PrintlineEventStation  extends AbstractPrintlineProcess implements EventStationSubsetter {

    public PrintlineEventStation(Element config)
            throws ConfigurationException {
        super(config);
    }
    
    public String getDefaultTemplate() {
        return DEFAULT_TEMPLATE;
    }

    public static final String DEFAULT_TEMPLATE = "Event: $event Station: $station";


    public StringTree accept(CacheEvent event, StationImpl station, CookieJar cookieJar) throws Exception {
        velocitizer.evaluate(filename,
                             template,
                             event,
                             station,
                             cookieJar);
        return new Pass(this);
    }
}
