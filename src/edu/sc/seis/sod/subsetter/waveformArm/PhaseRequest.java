package edu.sc.seis.sod.subsetter.waveformArm;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;

public class PhaseRequest implements RequestGenerator{
    public PhaseRequest (Element config) throws ConfigurationException{
        String model = "prem";
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            if(childNodes.item(counter) instanceof Element) {
                Element el = (Element)childNodes.item(counter);
                if(el.getTagName().equals("beginPhase")) {
                    beginPhase = SodUtil.getNestedText(el);
                } else if(el.getTagName().equals("endPhase")) {
                    endPhase = SodUtil.getNestedText(el);
                }else if(el.getTagName().equals("model")){
                    model = SodUtil.getNestedText(el);
                } else if(el.getTagName().equals("beginOffset")) {
                    beginOffset = SodUtil.loadTimeInterval(el);
                } else if(el.getTagName().equals("endOffset")) {
                    endOffset = SodUtil.loadTimeInterval(el);
                }
            }
        }
        try {
            util = TauPUtil.getTauPUtil(model);
        } catch (TauModelException e) {
            throw new ConfigurationException(model + " caused a TauModelException", e);
        }
    }

    public RequestFilter[] generateRequest(EventAccessOperations event,
                                           Channel channel, CookieJar cookieJar) throws Exception{
        Origin origin = EventUtil.extractOrigin(event);
        if ( prevRequestFilters != null &&
            origin.my_location.equals(prevOriginLoc) &&
            channel.my_site.my_location.equals(prevSiteLoc) ) {
            // don't need to do any work
            RequestFilter[] out = new RequestFilter[prevRequestFilters.length];
            for (int i = 0; i < out.length; i++) {
                out[i] = new RequestFilter(channel.get_id(),
                                           prevRequestFilters[i].start_time,
                                           prevRequestFilters[i].end_time);
            }
            return out;
        } else {
            prevOriginLoc = origin.my_location;
            prevSiteLoc = channel.my_site.my_location;
            prevRequestFilters = null;
        } // end of else

        double begin = getArrivalTime(beginPhase, channel, origin);
        double end = getArrivalTime(endPhase, channel, origin);
        if (begin == -1 || end == -1) {
            // no arrivals found, return zero length request filters
            prevRequestFilters = new RequestFilter[0];
            return prevRequestFilters;
        }

        MicroSecondDate originDate = new MicroSecondDate(origin.origin_time);
        TimeInterval bInterval = beginOffset.add(new TimeInterval(begin,
                                                                  UnitImpl.SECOND));
        TimeInterval eInterval = endOffset.add(new TimeInterval(end,
                                                                UnitImpl.SECOND));
        MicroSecondDate bDate = originDate.add(bInterval);
        MicroSecondDate eDate = originDate.add(eInterval);
        RequestFilter filter = new RequestFilter(channel.get_id(),
                                                 bDate.getFissuresTime(),
                                                 eDate.getFissuresTime());
        prevRequestFilters = new RequestFilter[]{filter};
        return prevRequestFilters;
    }



    private double getArrivalTime(String phase, Channel chan, Origin origin) throws TauModelException {
        if (phase.equals(ORIGIN)) {
            return 0;
        }else{
            String[] phases = {phase};
            Arrival[] arrivals = util.calcTravelTimes(chan.my_site.my_location,
                                                      origin,
                                                      phases);
            if(arrivals.length == 0){
                return -1;
            }else{
                // round to milliseconds
                return Math.rint(1000*arrivals[0].getTime())/1000;
            }
        }
    }

    private String beginPhase, endPhase;
    private TimeInterval beginOffset, endOffset;

    private TauPUtil util;
    private RequestFilter[] prevRequestFilters;
    private Location prevOriginLoc, prevSiteLoc;
    private static Logger logger = Logger.getLogger(PhaseRequest.class);
    private static final String ORIGIN = "origin";
}// PhaseRequest
