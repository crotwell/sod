package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Time;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * sample xml file
 *<pre>
 *  &lt;phaseRequest&gt;
 *      &lt;beginPhase&gt;ttp&lt;/beginPhase&gt;
 *          &lt;beginOffset&gt;
 *          &lt;unit&gt;SECOND&lt;/unit&gt;
 *          &lt;value&gt;-120&lt;/value&gt;
 *      &lt;/beginOffset&gt;
 *      &lt;endPhase&gt;tts&lt;/endPhase&gt;
 *      &lt;endOffset&gt;
 *          &lt;unit&gt;SECOND&lt;/unit&gt;
 *          &lt;value&gt;600&lt;/value&gt;
 *      &lt;/endOffset&gt;
 *  &lt;/phaseRequest&gt;
 *</pre>
 */



public class PhaseRequest implements RequestGenerator{
    /**
     * Creates a new <code>PhaseRequest</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public PhaseRequest (Element config) throws ConfigurationException{

        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                Element element = (Element)node;
                if(element.getTagName().equals("beginPhase")) {
                    beginPhase = SodUtil.getNestedText(element);
                } else if(element.getTagName().equals("beginOffset")) {
                    SodElement sodElement =
                        (SodElement) SodUtil.load(element,
                                                  waveformArmPackage);
                    beginOffset = (BeginOffset)sodElement;
                } else if(element.getTagName().equals("endPhase")) {
                    endPhase = SodUtil.getNestedText(element);
                } else if(element.getTagName().equals("endOffset")) {
                    SodElement sodElement =
                        (SodElement) SodUtil.load(element,
                                                  waveformArmPackage);
                    endOffset = (EndOffset)sodElement;
                }
            }
        }
    }

    /**
     * Describe <code>generateRequest</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>RequestFilter[]</code> value
     */
    public RequestFilter[] generateRequest(EventAccessOperations event,
                                           Channel channel, CookieJar cookieJar) throws Exception{
        Origin origin = null;
        double arrivalStartTime = -100.0;
        double arrivalEndTime = -100.0;
        origin = event.get_preferred_origin();

        if ( prevRequestFilter != null &&
            origin.my_location.equals(prevOriginLoc) &&
            channel.my_site.my_location.equals(prevSiteLoc) ) {
            // don't need to do any work
            RequestFilter[] out = new RequestFilter[prevRequestFilter.length];
            for (int i = 0; i < out.length; i++) {
                out[i] = new RequestFilter(channel.get_id(),
                                           prevRequestFilter[i].start_time,
                                           prevRequestFilter[i].end_time);
            }
            return out;
        } else {
            prevOriginLoc = origin.my_location;
            prevSiteLoc = channel.my_site.my_location;
            prevRequestFilter = null;
        } // end of else


        Properties props = Start.getProperties();
        String tauPModel = new String();
        tauPModel = props.getProperty("edu.sc.seis.sod.TaupModel");
        if(tauPModel == null) tauPModel = "prem";


        String phaseNames= "";
        if ( ! beginPhase.equals(ORIGIN)) {
            phaseNames += " "+beginPhase;
        } // end of if (beginPhase.equals("origin"))
        if ( ! endPhase.equals(ORIGIN)) {
            phaseNames += " "+endPhase;
        } // end of if (beginPhase.equals("origin"))


        Arrival[] arrivals = calculateArrivals(tauPModel,
                                               phaseNames,
                                               origin.my_location,
                                               channel.my_site.my_location);

        for(int counter = 0; counter < arrivals.length; counter++) {
            String arrivalName = arrivals[counter].getName();
            if(beginPhase.startsWith("tt")) {
                if(beginPhase.equals("tts")
                   && arrivalName.toUpperCase().startsWith("S")) {
                    arrivalStartTime = arrivals[counter].getTime();
                    break;
                } else if(beginPhase.equals("ttp")
                          && arrivalName.toUpperCase().startsWith("P")) {
                    arrivalStartTime = arrivals[counter].getTime();
                    break;
                }
            } else if(beginPhase.equals(arrivalName)) {
                arrivalStartTime = arrivals[counter].getTime();
                break;
            }
        }

        for(int counter = 0; counter < arrivals.length; counter++) {
            String arrivalName = arrivals[counter].getName();
            if(endPhase.startsWith("tt")) {
                if(endPhase.equals("tts")
                   && arrivalName.toUpperCase().startsWith("S")) {
                    arrivalEndTime = arrivals[counter].getTime();
                    break;
                } else if(endPhase.equals("ttp")
                          && arrivalName.toUpperCase().startsWith("P")) {
                    arrivalEndTime = arrivals[counter].getTime();
                    break;
                }
            } else if(endPhase.equals(arrivalName)) {
                arrivalEndTime = arrivals[counter].getTime();
                break;
            }
        }

        if (beginPhase.equals(ORIGIN)) {
            arrivalStartTime = 0;
        }
        if (endPhase.equals(ORIGIN)) {
            arrivalEndTime = 0;
        }

        if(arrivalStartTime == -100.0 || arrivalEndTime == -100.0) {
            // no arrivals found, return zero length request filters
            prevRequestFilter = new RequestFilter[0];
            return prevRequestFilter;
        }

        // round to milliseconds
        arrivalStartTime = Math.rint(1000*arrivalStartTime)/1000;
        arrivalEndTime = Math.rint(1000*arrivalEndTime)/1000;

        edu.iris.Fissures.Time originTime = origin.origin_time;
        MicroSecondDate originDate = new MicroSecondDate(originTime);
        TimeInterval bInterval = beginOffset.getTimeInterval();
        bInterval = bInterval.add(new TimeInterval(arrivalStartTime,
                                                   UnitImpl.SECOND));
        TimeInterval eInterval = endOffset.getTimeInterval();
        eInterval = eInterval.add(new TimeInterval(arrivalEndTime,
                                                   UnitImpl.SECOND));
        MicroSecondDate bDate = originDate.add(bInterval);
        MicroSecondDate eDate = originDate.add(eInterval);
        RequestFilter[] filters;
        filters = new RequestFilter[1];
        filters[0] =
            new RequestFilter(channel.get_id(),
                              bDate.getFissuresTime(),
                              eDate.getFissuresTime()
                             );

        prevRequestFilter = filters;
        return filters;

    }

    protected static TauP_Time tauPTime = new TauP_Time();

    protected synchronized static Arrival[] calculateArrivals(String tauPModelName,
                                                              String phases,
                                                              Location originLoc,
                                                              Location channelLoc)
        throws java.io.IOException, TauModelException {
        if (!tauPTime.getTauModelName().equals(tauPModelName)) {
            tauPTime.loadTauModel(tauPModelName);
        }
        tauPTime.clearPhaseNames();
        tauPTime.parsePhaseList(phases);

        double originDepth =
            ((QuantityImpl)originLoc.depth).convertTo(UnitImpl.KILOMETER).value;
        tauPTime.setSourceDepth(originDepth);
        tauPTime.calculate(SphericalCoords.distance(originLoc.latitude,
                                                    originLoc.longitude,
                                                    channelLoc.latitude,
                                                    channelLoc.longitude));

        return tauPTime.getArrivals();
    }

    private BeginOffset beginOffset;

    private String beginPhase;

    private EndOffset endOffset;

    private String endPhase;

    private RequestFilter[] prevRequestFilter = null;
    private Location prevOriginLoc = null;
    protected Location prevSiteLoc = null;
    private static Logger logger = Logger.getLogger(PhaseRequest.class);
    private static final String ORIGIN = "origin";

}// PhaseRequest
