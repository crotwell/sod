package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

import edu.sc.seis.TauP.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;
import java.util.*;
import org.w3c.dom.*;


/** sample xml
 *<pre>
 *  &lt;phaseInteraction&gt;
 *          &lt;modelName&gt;prem&lt;/modelName&gt;
 *          &lt;phaseName&gt;PcP&lt;/phaseName&gt;
 *          &lt;interactionStyle&gt;PATH&lt;/interactionStyle&gt;
 *          &lt;interactionNumber&gt;1&lt;/interactionNumber&gt;
 *          &lt;relative&gt;
 *              &lt;reference&gt;EVENT&lt;/reference&gt;
 *              &lt;depthRange&gt;
 *                  &lt;unitRange&gt;
 *                      &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *                      &lt;min&gt;-1000&lt;/min&gt;
 *                      &lt;max&gt;1000&lt;/max&gt;
 *                  &lt;/unitRange&gt;
 *              &lt;/depthRange&gt;
 *              &lt;distanceRange&gt;
 *                  &lt;unit&gt;DEGREE&lt;/unit&gt;
 *                  &lt;min&gt;60&lt;/min&gt;
 *                  &lt;max&gt;70&lt;/max&gt;
 *              &lt;/distanceRange&gt;
 *          &lt;/relative&gt;
 *  &lt;/phaseInteraction&gt;
 *</pre>
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
    if(element != null) phaseInteractionType = (PhaseInteractionType) SodUtil.load(element, "waveFormArm");
    element = SodUtil.getElement(config, "absolute");
    if(element != null) phaseInteractionType = (PhaseInteractionType) SodUtil.load(element, "waveFormArm");
    

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
    originDepth = ((QuantityImpl)origin.my_location.depth).convertTo(UnitImpl.KILOMETER).value;
    tauPPath.setSourceDepth(originDepth);

    eventStationDistance = SphericalCoords.distance(origin.my_location.latitude,
                            origin.my_location.longitude,
                            station.my_location.latitude,
                            station.my_location.longitude);
    double azimuth =  SphericalCoords.azimuth(origin.my_location.latitude,
                            origin.my_location.longitude,
                            station.my_location.latitude,
                            station.my_location.longitude);
    tauPPath.calculate(eventStationDistance);
    Arrival[] arrivals = tauPPath.getArrivals();
    Arrival[] requiredArrivals = getRequiredArrival(arrivals);
    if(requiredArrivals.length == 0) return false;
    
    if(phaseInteractionType instanceof Relative) {
        return handleRelativePathInteraction(requiredArrivals, eventStationDistance);
    } else {
        return handleAbsolutePhaseInteraction(requiredArrivals, azimuth, origin, "PATH");
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
    originDepth = ((QuantityImpl)origin.my_location.depth).convertTo(UnitImpl.KILOMETER).value;
    tauPPierce.setSourceDepth(originDepth);
    eventStationDistance = SphericalCoords.distance(origin.my_location.latitude,
                            origin.my_location.longitude,
                            station.my_location.latitude,
                            station.my_location.longitude);
    double azimuth =  SphericalCoords.azimuth(origin.my_location.latitude,
                          origin.my_location.longitude,
                          station.my_location.latitude,
                          station.my_location.longitude);
    tauPPierce.calculate(eventStationDistance);
    Arrival[] arrivals = tauPPierce.getArrivals();
    Arrival[] requiredArrivals = getRequiredArrival(arrivals);
    if(requiredArrivals.length != 0) {
        if(phaseInteractionType instanceof Relative) {
        return handlePierceRelativePhaseInteraction(requiredArrivals, eventStationDistance);
        } else {
        //throw new ConfigurationException("Absolute Area for PhaseInteraction is Not Implemented");
        return handleAbsolutePhaseInteraction(requiredArrivals, azimuth, origin, "PIERCE");
        }
    } else return false;


    }
    
    public boolean handlePierceRelativePhaseInteraction(Arrival[] requiredArrivals, double eventStationDistance) throws Exception{
    
    
    for(int counter = 0; counter < requiredArrivals.length; counter++) {
        TimeDist[] timeDistArray = requiredArrivals[counter].getPierce();
        TimeDist timeDist = getRequiredTimeDist(timeDistArray);
        QuantityImpl timeDistDepth = new QuantityImpl(timeDist.depth, UnitImpl.KILOMETER);;
        QuantityImpl minDepth;
        QuantityImpl maxDepth;
        QuantityImpl timeDistDistance = new QuantityImpl(timeDist.dist*180/Math.PI, UnitImpl.DEGREE);
        QuantityImpl minDistance;
        QuantityImpl maxDistance;
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
        }//end of if checking for depth.
    }//end of for(int counter = 0; ........)
    return false;
    }

    public boolean handleRelativePathInteraction(Arrival[] requiredArrivals, double eventStationDistance) throws Exception {
    
    for(int counter = 0; counter < requiredArrivals.length; counter++) {

        TimeDist[] timeDistArray = requiredArrivals[counter].getPath();
        if( checkForRelativePathInteraction(timeDistArray, 0,timeDistArray.length, eventStationDistance, requiredArrivals[counter].getDistDeg()) ) {
        return true;
        }
    }
    return false;
    }
    public boolean checkForRelativePathInteraction(TimeDist[] timeDistArray, int start, int end, double eventStationDistance, double totalDistance) throws Exception {
    
    //for(int i = 0; i < timeDistArray.length; i++) {
        int counter = 0;
        if(end < start) return false;
        int mid = (start+end)/2;
        TimeDist timeDist = timeDistArray[mid];
        QuantityImpl minDistance = null;
        QuantityImpl maxDistance = null;
        QuantityImpl timeDistDistance = new QuantityImpl(timeDist.dist*180/Math.PI, UnitImpl.DEGREE);
        while(counter < totalDistance) {
        
        QuantityImpl timeDistDepth = new QuantityImpl(timeDist.depth, UnitImpl.KILOMETER);
        QuantityImpl minDepth;
        QuantityImpl maxDepth;
        
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
        return checkForRelativePathInteraction(timeDistArray,mid, end, eventStationDistance, totalDistance);
        } else if(maxDistance.lessThan(timeDistDistance)) {
        return checkForRelativePathInteraction(timeDistArray, start, mid, eventStationDistance, totalDistance);
        } else return false;
  
        //}
    }
    
    public boolean handleAbsolutePhaseInteraction(Arrival[] requiredArrivals, double azimuth, Origin origin, String type) throws Exception {
    
    edu.iris.Fissures.Area area = ((Absolute)phaseInteractionType).getArea();
    
    for(int i = 0; i < requiredArrivals.length; i++) {
        TimeDist[] timeDist;
        if(type.equals("PIERCE")) {
        timeDist = requiredArrivals[i].getPierce();
        } else {
        timeDist = requiredArrivals[i].getPath();
        }

        azimuth = checkForLongway(requiredArrivals[i].getDistDeg(), azimuth);
        for(int counter = 0; counter < timeDist.length; counter++) {
        QuantityImpl timeDistDepth = new QuantityImpl(timeDist[0].depth, UnitImpl.KILOMETER);;
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
            if(area == null || area instanceof GlobalArea) return true;
            double tLat = SphericalCoords.latFor(origin.my_location.latitude,
                             origin.my_location.longitude,
                             timeDist[counter].depth,
                             azimuth);
            double tLon =  SphericalCoords.lonFor(origin.my_location.latitude,
                              origin.my_location.longitude,
                              timeDist[counter].depth,
                              azimuth);
            if(area instanceof BoxArea) {
            
            BoxArea boxArea = (BoxArea) area;
            if( tLat >= boxArea.min_latitude &&
                tLat <= boxArea.max_latitude &&
                tLon >= boxArea.min_longitude &&
                tLon <= boxArea.max_longitude) {
                return true;
            }
            }//end of if area instanceof BoxArea.
        } //end of if checking for depth.
        }//end of for(int counter = 0.........
    }//end of for(int i= 0; i < requiredArrivals.length; i++)
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
        
        //("dist = "+distance+" depth = "+depth+" past = "+past+" current = "+current+" next = "+next);
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
    
    public Arrival[] getRequiredArrival(Arrival[] arrivals) {
    
    ArrayList arrayList = new ArrayList();
    for(int counter = 0; counter < arrivals.length; counter++) {
        
        String arrivalName = arrivals[counter].getName();
        if(phaseName.startsWith("tt")) {
        if(phaseName.equals("tts") && arrivalName.toUpperCase().startsWith("S")) {
            arrayList.add(arrivals[counter]);
        } else if(phaseName.equals("ttp") && arrivalName.toUpperCase().startsWith("P")) {
            arrayList.add(arrivals[counter]);
        }
        } else if(phaseName.equals(arrivalName)) {
        arrayList.add(arrivals[counter]);
        }
      
    }
    Arrival[] requiredArrivals = new Arrival[arrayList.size()];
    requiredArrivals = (Arrival[]) arrayList.toArray(requiredArrivals);
    return requiredArrivals;
    }

    public double checkForLongway(double distance, double azimuth) {

    while(distance > 360) distance = distance - 360;
    if(distance > 180) {
        azimuth = azimuth + 180;
    }
    return azimuth;
    }

    private String modelName = null;
    
    private String phaseName = null;

    private String interactionStyle = null;

    private int interactionNumber = -1;

    private PhaseInteractionType phaseInteractionType = null;

}// PhaseInteraction
