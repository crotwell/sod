package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractPrintlineProcess;

public class PrintlineEventStation  extends AbstractPrintlineProcess implements EventStationSubsetter {

    public PrintlineEventStation(Element config)
            throws ConfigurationException {
        super(config);
    }
    
    public String getDefaultTemplate() {
        return DEFAULT_TEMPLATE;
    }

    public static final String DEFAULT_TEMPLATE = "Event: $event Station: $station";


    public StringTree accept(CacheEvent event, Station station, CookieJar cookieJar) throws Exception {
        velocitizer.evaluate(filename,
                             template,
                             event,
                             station,
                             cookieJar);
        return new Pass(this);
    }
}
