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


/** sample xml
 * 	&lt;phaseInteraction&gt;
 * 			&lt;modelName&gt;prem&lt;/modelName&gt;
 *			&lt;phaseName&gt;PcP&lt;/phaseName&gt;
 *			&lt;interactionStyle&gt;PATH&lt;/interactionStyle&gt;
 *			&lt;interactionNumber&gt;1&lt;/interactionNumber&gt;
 *			&lt;relative&gt;
 *				&lt;reference&gt;EVENT&lt;/reference&gt;
 * 				&lt;depthRange&gt;
 *					&lt;unitRange&gt;
 *						&lt;unit&gt;KILOMETER&lt;/unit&gt;
 *						&lt;min&gt;-1000&lt;/min&gt;
 *						&lt;max&gt;1000&lt;/max&gt;
 *					&lt;/unitRange&gt;
 *				&lt;/depthRange&gt;
 *				&lt;distanceRange&gt;
 *					&lt;unit&gt;DEGREE&lt;/unit&gt;
 *					&lt;min&gt;60&lt;/min&gt;
 *					&lt;max&gt;70&lt;/max&gt;
 *				&lt;/distanceRange&gt;
 *			&lt;/relative&gt;
 *	&lt;/phaseInteraction&gt;
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
	/*System.out.println("The phaseName is "+phaseName);
	  System.out.println("The modelName is "+modelName);
	  System.out.println("The interactionStyle is "+interactionStyle);
	*/
	if(interactionStyle.equals("PATH")) return acceptPathInteraction(event, network, station, cookies);
	else return acceptPierceInteraction(event, network, station, cookies);

    }
    
    public boolean acceptPathInteraction(EventAccessOperations event,  NetworkAccess network,Station station, CookieJar cookies) throws Exception{
	Origin origin = null;
	double originDepth;
	double eventStationDistance;
	origin = event.get_preferred_origin();
	TauP_Path tauPPath = new TauP_Path(modelName);
	tauPPath.clearPhaseNames();
	tauPPath.parsePhaseList(phaseName);
	UnitImpl originUnit = (UnitImpl)origin.my_location.depth.the_units;
	originDepth = origin.my_location.depth.value;
	if(!originUnit.equals(UnitImpl.KILOMETER)) {
	    originDepth = ((QuantityImpl)origin.my_location.depth).convertTo(UnitImpl.KILOMETER).value;
	}
	tauPPath.setSourceDepth(origin.my_location.depth.value);
	eventStationDistance = SphericalCoords.distance(origin.my_location.latitude, 
							origin.my_location.longitude,
							station.my_location.latitude,
							station.my_location.longitude);
	//	System.out.println("The depth of the origin is "+originDepth);
	//	System.out.println("The eventStation Distance is "+eventStationDistance);
	tauPPath.calculate(eventStationDistance); 
	Arrival[] arrivals = tauPPath.getArrivals();
	Arrival requiredArrival = getRequiredArrival(arrivals);
	if(requiredArrival == null) return false;
	TimeDist[] timeDist = requiredArrival.getPath();
	System.out.println("The Distance in degrees is "+requiredArrival.getDistDeg()+"actual = "+timeDist[timeDist.length-1].dist*180/Math.PI);
	if(phaseInteractionType instanceof Relative) {
	    return handleRelativePathInteraction(timeDist, 0, timeDist.length, eventStationDistance, requiredArrival.getDistDeg());
	} else {
	    throw new ConfigurationException("Absolute Area for PhaseInteraction is Not Implemented");
		//return handlePierceAbsolutePhaseInteraction(requiredTimeDist);
	}
    }

    


    public boolean acceptPierceInteraction(EventAccessOperations event,  NetworkAccess network,Station station, CookieJar cookies) throws Exception{
	Origin origin = null;
	double originDepth;
	double eventStationDistance;
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
	eventStationDistance = SphericalCoords.distance(origin.my_location.latitude, 
							origin.my_location.longitude,
							station.my_location.latitude,
							station.my_location.longitude);
	//	System.out.println("The depth of the origin is "+originDepth);
	//	System.out.println("The eventStation Distance is "+eventStationDistance);
	tauPPierce.calculate(eventStationDistance); 
	Arrival[] arrivals = tauPPierce.getArrivals();
	Arrival requiredArrival = getRequiredArrival(arrivals);
	if(requiredArrival != null) {
	    //	    System.out.println("Got the Required Arrival");
	    TimeDist[] timeDist = requiredArrival.getPierce();
	    TimeDist requiredTimeDist = getRequiredTimeDist(timeDist);
	    if(requiredTimeDist == null) return false;
	    //	    System.out.println("Got the Required Time Dist ");
	    if(phaseInteractionType instanceof Relative) {
		return handlePierceRelativePhaseInteraction(requiredTimeDist, eventStationDistance);
	    } else {
		throw new ConfigurationException("Absolute Area for PhaseInteraction is Not Implemented");
		//return handlePierceAbsolutePhaseInteraction(requiredTimeDist);
	    }
	} else return false;


    }
    
    public boolean handlePierceRelativePhaseInteraction(TimeDist timeDist, double eventStationDistance) throws Exception{
	
	
	QuantityImpl timeDistDepth = new QuantityImpl(timeDist.depth, UnitImpl.KILOMETER);;
	QuantityImpl minDepth;
	QuantityImpl maxDepth;
	QuantityImpl timeDistDistance = new QuantityImpl(timeDist.dist*180/Math.PI, UnitImpl.DEGREE);
	QuantityImpl minDistance;
	QuantityImpl maxDistance;
	//	System.out.println("Must check depthRange and Distance Range Now");
	if(((Relative)phaseInteractionType).getDepthRange() != null) {
	    
	    minDepth = (QuantityImpl)((Relative)phaseInteractionType).getDepthRange().getMinDepth();
	    maxDepth = (QuantityImpl)((Relative)phaseInteractionType).getDepthRange().getMaxDepth();
	} else {
	    minDepth = timeDistDepth;
	    maxDepth = timeDistDepth;
	}
	if(((Relative)phaseInteractionType).getDistanceRange() != null) {
	    if(((Relative)phaseInteractionType).getReference().equals("STATION")) {
		timeDistDistance = new QuantityImpl((eventStationDistance - timeDist.dist)*180/Math.PI, UnitImpl.KILOMETER);
	    }
	    minDistance = (QuantityImpl)((Relative)phaseInteractionType).getDistanceRange().getMinDistance();
	    maxDistance = (QuantityImpl)((Relative)phaseInteractionType).getDistanceRange().getMaxDistance();
		
	} else {
	    minDistance = timeDistDistance;
	    maxDistance = timeDistDistance;
	}
	
	if(minDepth.lessThanEqual(timeDistDepth) && maxDepth.greaterThanEqual(timeDistDepth)) {
	    if(minDistance.lessThanEqual(timeDistDistance) && maxDistance.greaterThanEqual(timeDistDistance)) {
		return  true;
	    }
	}
	return false;
    }

    public boolean handleRelativePathInteraction(TimeDist[] timeDistArray, int start, int end, double eventStationDistance, double totalDistance) throws Exception {
	
	System.out.println("The length of the path array is "+timeDistArray.length);
	//for(int i = 0; i < timeDistArray.length; i++) {
	    int counter = 0;
	    if(end < start) return false;
	    System.out.println("Performing pathInteraction subsetting "+start+"  "+end);
	    int mid = (start+end)/2;
	    TimeDist timeDist = timeDistArray[mid];
	    QuantityImpl minDistance = null;
	    QuantityImpl maxDistance = null;
	    QuantityImpl timeDistDistance = new QuantityImpl(timeDist.dist*180/Math.PI, UnitImpl.DEGREE);
	    while(counter < totalDistance) {
		
		QuantityImpl timeDistDepth = new QuantityImpl(timeDist.depth, UnitImpl.KILOMETER);
		QuantityImpl minDepth;
		QuantityImpl maxDepth;
		
		//	System.out.println("Must check depthRange and Distance Range Now");
		if(((Relative)phaseInteractionType).getDepthRange() != null) {
		    
		    minDepth = (QuantityImpl)((Relative)phaseInteractionType).getDepthRange().getMinDepth();
		    maxDepth = (QuantityImpl)((Relative)phaseInteractionType).getDepthRange().getMaxDepth();
		} else {
		    minDepth = timeDistDepth;
		    maxDepth = timeDistDepth;
		}
		if(((Relative)phaseInteractionType).getDistanceRange() != null) {
		    if(((Relative)phaseInteractionType).getReference().equals("STATION")) {
			timeDistDistance = new QuantityImpl((eventStationDistance - timeDist.dist)*180/Math.PI, UnitImpl.DEGREE);
		    }
		    minDistance = (QuantityImpl)((Relative)phaseInteractionType).getDistanceRange().getMinDistance();
		    maxDistance = (QuantityImpl)((Relative)phaseInteractionType).getDistanceRange().getMaxDistance();
		    
		} else {
		    minDistance = timeDistDistance;
		    maxDistance = timeDistDistance;
		}
		if(minDepth.lessThanEqual(timeDistDepth) && maxDepth.greaterThanEqual(timeDistDepth)) {
		    minDistance = new QuantityImpl(minDistance.value+counter , UnitImpl.DEGREE);
		    maxDistance =  new QuantityImpl((360+counter)-minDistance.value, UnitImpl.DEGREE);
		    QuantityImpl tempDistance = new QuantityImpl(counter-timeDistDistance.value, UnitImpl.DEGREE);
		    if((minDistance.lessThanEqual(timeDistDistance) && maxDistance.greaterThanEqual(timeDistDistance)) ||
		       (minDistance.lessThanEqual(tempDistance) && maxDistance.greaterThanEqual(tempDistance))) {
			return  true;
		    } 
		}
		
		counter += 360;
		
	    }
	    if(end < start) { return false;}
	    else if(minDistance.greaterThan(timeDistDistance)) {
		return handleRelativePathInteraction(timeDistArray,mid, end, eventStationDistance, totalDistance);
	    } else if(maxDistance.lessThan(timeDistDistance)) {
		return handleRelativePathInteraction(timeDistArray, start, mid, eventStationDistance, totalDistance);
	    } else return false;
  
	    //}	   
    }
    
    public boolean handlePierceAbsolutePhaseInteraction(TimeDist timeDist) throws Exception {
	
	QuantityImpl timeDistDepth = new QuantityImpl(timeDist.depth, UnitImpl.KILOMETER);;
	QuantityImpl minDepth;
	QuantityImpl maxDepth;
	if(((Absolute)phaseInteractionType).getDepthRange() != null) {
	    minDepth = (QuantityImpl)((Absolute)phaseInteractionType).getDepthRange().getMinDepth();
	    maxDepth = (QuantityImpl)((Absolute)phaseInteractionType).getDepthRange().getMaxDepth();
	} else {
	    minDepth = timeDistDepth;
	    maxDepth = timeDistDepth;
	}
	if(minDepth.lessThanEqual(timeDistDepth) && maxDepth.greaterThanEqual(timeDistDepth)) {
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
	    if(interactionStyle.equals("TOPSIDE REFLECTION")) {
		
		//System.out.println("dist = "+distance+" depth = "+depth+" past = "+past+" current = "+current+" next = "+next);
	    }
	    if(interactionStyle.equals("TOPSIDE REFLECTION") && past < current && current > next) {
		return timeDist[counter];
	    } else if(interactionStyle.equals("BOTTOMSIDE REFLECTION") && past > current && current < next) {
		return timeDist[counter];
	    } 
	    past = current;
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
