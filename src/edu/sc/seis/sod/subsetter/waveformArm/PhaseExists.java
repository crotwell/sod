package edu.sc.seis.sod.subsetter.waveformArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.TauP.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;


/**
 * sample xml
 *<pre>
 *      &lt;phaseExists&gt;
 *      &lt;modelName&gt;prem&lt;/modelName&gt;
 *      &lt;phaseName&gt;ttp&lt;/phaseName&gt;
 *  &lt;/phaseExists&gt;
 *</pre>
 */
public class PhaseExists
    implements EventStationSubsetter {
    
    /**
     * Creates a new <code>PhaseExists</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
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
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param o an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations event, NetworkAccess network, Station station,  CookieJar cookies)
        throws Exception{
        Origin origin = null;
        double originDepth;
        origin = event.get_preferred_origin();
        TauP_Time tauPTime = new TauP_Time(modelName);
        tauPTime.clearPhaseNames();
        tauPTime.parsePhaseList(phaseName);
        UnitImpl originUnit = (UnitImpl)origin.my_location.depth.the_units;
        originDepth = origin.my_location.depth.value;
        if(!originUnit.equals(UnitImpl.KILOMETER)) {
            originDepth =((QuantityImpl)origin.my_location.depth).convertTo(UnitImpl.KILOMETER).value;
        }
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
