package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.TauP.TauP_Time;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;


/**
 * sample xml
 *<pre>
 *      &lt;phaseExists&gt;
 *      &lt;modelName&gt;prem&lt;/modelName&gt;
 *      &lt;phaseName&gt;ttp&lt;/phaseName&gt;
 *  &lt;/phaseExists&gt;
 *</pre>
 */
public class PhaseExists implements EventStationSubsetter {

    public PhaseExists (Element config) throws ConfigurationException {
        Element element = SodUtil.getElement(config,"modelName");
        if(element != null) modelName = SodUtil.getNestedText(element);
        element = SodUtil.getElement(config,"phaseName");
        if(element != null) {
            phaseName = SodUtil.getNestedText(element);
        } else {
            throw new ConfigurationException("Phase name not in configuration");
        }
    }

    public boolean accept(EventAccessOperations event, Station station, CookieJar cookieJar)
        throws Exception{
        Origin origin = event.get_preferred_origin();
        TauP_Time tauPTime = new TauP_Time(modelName);
        tauPTime.clearPhaseNames();
        tauPTime.parsePhaseList(phaseName);
        UnitImpl originUnit = (UnitImpl)origin.my_location.depth.the_units;
        tauPTime.setSourceDepth(origin.my_location.depth.value);
        tauPTime.calculate(SphericalCoords.distance(origin.my_location.latitude,
                                                    origin.my_location.longitude,
                                                    station.my_location.latitude,
                                                    station.my_location.longitude));
        Arrival[] arrivals  = tauPTime.getArrivals();
        if(getRequiredArrival(arrivals) == null) return false;
        else return true;
    }

    public Arrival getRequiredArrival(Arrival[] arrivals) {
        Arrival requiredArrival = null;
        for(int counter = 0; counter < arrivals.length; counter++) {
            String arrivalName = arrivals[counter].getName();
            if(phaseName.startsWith("tt")) {
                if(phaseName.equals("tts") && arrivalName.toUpperCase().startsWith("S")) {
                    requiredArrival = arrivals[counter];
                } else if(phaseName.equals("ttp") && arrivalName.toUpperCase().startsWith("P")) {
                    requiredArrival = arrivals[counter];
                }
            } else if(phaseName.equals(arrivalName)) {
                requiredArrival = arrivals[counter];
            }
        }
        return requiredArrival;
    }

    private String modelName = "iasp91";

    private String phaseName;
}// PhaseExists
