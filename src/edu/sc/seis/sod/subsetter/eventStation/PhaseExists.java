package edu.sc.seis.sod.subsetter.eventStation;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

/**
 * sample xml
 *
 * <pre>
 *
 *       &lt;phaseExists&gt;
 *       &lt;modelName&gt;prem&lt;/modelName&gt;
 *       &lt;phaseName&gt;ttp&lt;/phaseName&gt;
 *   &lt;/phaseExists&gt;
 *
 * </pre>
 */
public class PhaseExists implements EventStationSubsetter {

    public PhaseExists(Element config) throws ConfigurationException {
        Element element = SodUtil.getElement(config, "modelName");
        if(element != null) modelName = SodUtil.getNestedText(element);
        element = SodUtil.getElement(config, "phaseName");
        if(element != null) {
            phaseName = SodUtil.getNestedText(element);
        } else {
            throw new ConfigurationException("Phase name not in configuration");
        }
        try {
            tauPTime = TauPUtil.getTauPUtil(modelName);
        } catch(TauModelException e) {
            throw new ConfigurationException("Cannot initize TauP travel time calculator",
                                             e);
        }
    }

    public StringTree accept(EventAccessOperations event,
                          Station station,
                          CookieJar cookieJar) throws Exception {
        Origin origin = event.get_preferred_origin();
        Arrival[] arrivals = tauPTime.calcTravelTimes(station,
                                                      origin,
                                                      new String[] {phaseName});
        if(getRequiredArrival(arrivals) == null) return new StringTreeLeaf(this, false);
        else return new StringTreeLeaf(this, true);
    }

    public Arrival getRequiredArrival(Arrival[] arrivals) {
        Arrival requiredArrival = null;
        for(int counter = 0; counter < arrivals.length; counter++) {
            String arrivalName = arrivals[counter].getName();
            if(phaseName.startsWith("tt")) {
                if(phaseName.equals("tts")
                        && arrivalName.toUpperCase().startsWith("S")) {
                    requiredArrival = arrivals[counter];
                } else if(phaseName.equals("ttp")
                        && arrivalName.toUpperCase().startsWith("P")) {
                    requiredArrival = arrivals[counter];
                }
            } else if(phaseName.equals(arrivalName)) {
                requiredArrival = arrivals[counter];
            }
        }
        return requiredArrival;
    }

    TauPUtil tauPTime;

    private String modelName = "iasp91";

    private String phaseName;
}// PhaseExists
