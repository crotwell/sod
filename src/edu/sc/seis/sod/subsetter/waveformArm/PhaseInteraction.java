package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;
import edu.sc.seis.TauP.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * PhaseInteraction.java
 *
 *
 * Created: Mon Apr  8 16:32:56 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class PhaseInteraction implements EventStationSubsetter {
    /**
     * Creates a new <code>PhaseInteraction</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public PhaseInteraction (Element config) throws ConfigurationException{

	Element element = SodUtil.getElement(config,"modelName");
	if(element != null) modelName = SodUtil.getNestedText(element);
	element = SodUtil.getElement(config,"phaseName");
	if(element != null) phaseName = SodUtil.getNestedText(element);
	element = SodUtil.getElement(config,"interactionStyle");
	if(element != null) interactionStyle = SodUtil.getNestedText(element);
	element = SodUtil.getElement(config, "interactionNumber");
	if(element != null) interactionNumber = Integer.parseInt(SodUtil.getNestedText(element));
	element = SodUtil.getElement(config, "relative");
	if(element != null) phaseInteractionType = (PhaseInteractionType) SodUtil.load(element, "edu.sc.seis.sod.subsetter.waveFormArm");
	element = SodUtil.getElement(config, "absolute");
	if(element != null) phaseInteractionType = (PhaseInteractionType) SodUtil.load(element, "edu.sc.seis.sod.subsetter.waveFormArm");
	

    }
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param eventAccess an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations event,  NetworkAccess network,Station station, CookieJar cookies) throws Exception{
	System.out.println("The phaseName is "+phaseName);
	System.out.println("The modelName is "+modelName);
	System.out.println("The interactionStyle is "+interactionStyle);
	
	Origin origin = null;
	double originDepth;
	origin = event.get_preferred_origin();
	TauP_Pierce tauPPierce = new TauP_Pierce(modelName);
	tauPPierce.clearPhaseNames();
	tauPPierce.parsePhaseList(phaseName);
	UnitImpl originUnit = (UnitImpl)origin.my_location.depth.the_units;
	originDepth = origin.my_location.depth.value;
	if(!originUnit.equals(UnitImpl.KILOMETER)) {
	    originDepth = ((QuantityImpl)origin.my_location.depth).convertTo(UnitImpl.KILOMETER).value;
	}
	tauPPierce.setSourceDepth(origin.my_location.depth.value);
	tauPPierce.calculate(SphericalCoords.distance(origin.my_location.latitude, 
						    origin.my_location.longitude,
						    station.my_location.latitude,
						    station.my_location.longitude));
	Arrival[] arrivals = tauPPierce.getArrivals();
	Arrival requiredArrival = getRequiredArrival(arrivals);
	
	TimeDist[] timeDist = requiredArrival.getPierce();
	TimeDist requiredTimeDist = getRequiredTimeDist(timeDist);
	
	return true;
    }
    
    public boolean handleRelativePhaseInteraction(TimeDist timeDist) throws Exception{

	QuantityImpl timeDistDepth = new QuantityImpl(timeDist.depth, UnitImpl.KILOMETER);
	QuantityImpl minDepth = (QuantityImpl)((Relative)phaseInteractionType).getDepthRange().getMinDepth();
	QuantityImpl maxDepth = (QuantityImpl)((Relative)phaseInteractionType).getDepthRange().getMaxDepth();
	if(minDepth.lessThanEqual(timeDistDepth) && maxDepth.lessThanEqual(timeDistDepth)) {
	    
	    return  true;
	}
	return false;
    }

    public TimeDist getRequiredTimeDist(TimeDist[] timeDist) {
	
	if(timeDist.length == 0) return null;
	double past = timeDist[0].depth;
	double current;
	double next;
	for(int counter = 1; counter < timeDist.length-1; counter++) {
	    current =  timeDist[counter].depth;
	    next = timeDist[counter+1].depth;
	    if(phaseInteractionType.equals("TOPSIDE REFLECTION") && past < current && current < next) {

		return timeDist[counter];
	    } else if(phaseInteractionType.equals("BOTTOMSIDE REFLECTION") && past < current && current > next) {

		return timeDist[counter];
	    }
	}
	return null;

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

    private String modelName = null;
    
    private String phaseName = null;

    private String interactionStyle = null;

    private int interactionNumber = -1;

    private PhaseInteractionType phaseInteractionType = null;
  

}// PhaseInteraction
