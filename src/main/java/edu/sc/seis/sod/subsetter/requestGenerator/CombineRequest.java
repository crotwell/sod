package edu.sc.seis.sod.subsetter.requestGenerator;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;


public class CombineRequest implements RequestGenerator {

    public CombineRequest(Element config) throws ConfigurationException {
        NodeList kids = config.getChildNodes();
        for (int i = 0; i< kids.getLength(); i++) {
            if (kids.item(i) instanceof Element) {
                Object obj = SodUtil.load((Element)kids.item(i), packages);
                if(obj instanceof RequestGenerator){ generators.add((RequestGenerator)obj); }
            }
        }
    }
    
    public RequestFilter[] generateRequest(CacheEvent event, Channel channel, MeasurementStorage cookieJar) throws Exception {
        List<RequestFilter> out = new ArrayList<RequestFilter>();
        for (RequestGenerator gen : generators) {
            RequestFilter[] subout = gen.generateRequest(event, channel, cookieJar);
            for (int i = 0; i < subout.length; i++) {
                out.add(subout[i]);
            }
        }
        return out.toArray(new RequestFilter[0]);
    }
    
    protected List<RequestGenerator> generators = new ArrayList<RequestGenerator>();
    
    protected String[] packages = new String[] {"requestGenerator"};
}
