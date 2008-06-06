package edu.sc.seis.sod.subsetter.eventStation;

import java.util.ArrayList;

import org.w3c.dom.Element;

import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Path;
import edu.sc.seis.TauP.TauP_Pierce;
import edu.sc.seis.TauP.TimeDist;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class PhaseInteraction implements EventStationSubsetter {

    public PhaseInteraction(Element config) throws ConfigurationException {
        Element element = SodUtil.getElement(config, "modelName");
        if(element != null) modelName = SodUtil.getNestedText(element);
        element = SodUtil.getElement(config, "phaseName");
        if(element != null) phaseName = SodUtil.getNestedText(element);
        element = SodUtil.getElement(config, "interactionStyle");
        if(element != null) interactionStyle = SodUtil.getNestedText(element);
        element = SodUtil.getElement(config, "interactionNumber");
        if(element != null) interactionNumber = Integer.parseInt(SodUtil.getNestedText(element));
        element = SodUtil.getElement(config, "relative");
        if(element != null) phaseInteractionType = (PhaseInteractionType)SodUtil.load(element,
                                                                                      "eventStation");
        element = SodUtil.getElement(config, "absolute");
        if(element != null) phaseInteractionType = (PhaseInteractionType)SodUtil.load(element,
                                                                                      "eventStation");
        try {
            tauPPierce = new TauP_Pierce(modelName);
            tauPPierce.clearPhaseNames();
            tauPPierce.parsePhaseList(phaseName);
            tauPPath = new TauP_Path(tauPPierce.getTauModel());
            tauPPath.clearPhaseNames();
            tauPPath.parsePhaseList(phaseName);
        } catch(TauModelException e) {
            throw new ConfigurationException("Can't load TauP_Pierce", e);
        }
    }

    public StringTree accept(CacheEvent event,
                             StationImpl station,
                          CookieJar cookieJar) throws Exception {
        if(interactionStyle.equals("PATH")) {
            return new StringTreeLeaf(this, acceptPathInteraction(event,
                                                                         station));
        } else {
            return new StringTreeLeaf(this, acceptPierceInteraction(event, station));
        }
    }

    public boolean acceptPathInteraction(CacheEvent event,
                                         StationImpl station) throws Exception {
        OriginImpl origin = event.getOrigin();
        double originDepth;
        double eventStationDistance;
        originDepth = ((QuantityImpl)origin.getLocation().depth).convertTo(UnitImpl.KILOMETER).value;
        tauPPath.setSourceDepth(originDepth);
        eventStationDistance = SphericalCoords.distance(origin.getLocation().latitude,
                                                        origin.getLocation().longitude,
                                                        station.getLocation().latitude,
                                                        station.getLocation().longitude);
        double azimuth = SphericalCoords.azimuth(origin.getLocation().latitude,
                                                 origin.getLocation().longitude,
                                                 station.getLocation().latitude,
                                                 station.getLocation().longitude);
        tauPPath.calculate(eventStationDistance);
        Arrival[] arrivals = tauPPath.getArrivals();
        Arrival[] requiredArrivals = getRequiredArrival(arrivals);
        if(requiredArrivals.length == 0) return false;
        if(phaseInteractionType instanceof Relative) {
            return handleRelativePathInteraction(requiredArrivals,
                                                 eventStationDistance);
        } else {
            return handleAbsolutePhaseInteraction(requiredArrivals,
                                                  azimuth,
                                                  origin,
                                                  "PATH");
        }
    }

    public boolean acceptPierceInteraction(CacheEvent event,
                                           StationImpl station) throws Exception {
        double originDepth;
        double eventStationDistance;
        OriginImpl origin = event.getOrigin();
        originDepth = ((QuantityImpl)origin.getLocation().depth).convertTo(UnitImpl.KILOMETER).value;
        tauPPierce.setSourceDepth(originDepth);
        eventStationDistance = SphericalCoords.distance(origin.getLocation().latitude,
                                                        origin.getLocation().longitude,
                                                        station.getLocation().latitude,
                                                        station.getLocation().longitude);
        double azimuth = SphericalCoords.azimuth(origin.getLocation().latitude,
                                                 origin.getLocation().longitude,
                                                 station.getLocation().latitude,
                                                 station.getLocation().longitude);
        tauPPierce.calculate(eventStationDistance);
        Arrival[] arrivals = tauPPierce.getArrivals();
        Arrival[] requiredArrivals = getRequiredArrival(arrivals);
        if(requiredArrivals.length != 0) {
            if(phaseInteractionType instanceof Relative) {
                return handlePierceRelativePhaseInteraction(requiredArrivals,
                                                            eventStationDistance);
            } else {
                //throw new ConfigurationException("Absolute Area for
                // PhaseInteraction is Not Implemented");
                return handleAbsolutePhaseInteraction(requiredArrivals,
                                                      azimuth,
                                                      origin,
                                                      "PIERCE");
            }
        } else return false;
    }

    public boolean handlePierceRelativePhaseInteraction(Arrival[] requiredArrivals,
                                                        double eventStationDistance)
            throws Exception {
        for(int counter = 0; counter < requiredArrivals.length; counter++) {
            TimeDist[] timeDistArray = requiredArrivals[counter].getPierce();
            TimeDist timeDist = getRequiredTimeDist(timeDistArray);
            QuantityImpl timeDistDepth = new QuantityImpl(timeDist.depth,
                                                          UnitImpl.KILOMETER);
            ;
            QuantityImpl minDepth;
            QuantityImpl maxDepth;
            QuantityImpl timeDistDistance = new QuantityImpl(timeDist.dist
                    * 180 / Math.PI, UnitImpl.DEGREE);
            QuantityImpl minDistance;
            QuantityImpl maxDistance;
            if(((Relative)phaseInteractionType).getDepthRange() != null) {
                minDepth = ((Relative)phaseInteractionType).getDepthRange()
                        .getMinDepth();
                maxDepth = ((Relative)phaseInteractionType).getDepthRange()
                        .getMaxDepth();
            } else {
                minDepth = timeDistDepth;
                maxDepth = timeDistDepth;
            }
            if(((Relative)phaseInteractionType).getDistanceRange() != null) {
                if(((Relative)phaseInteractionType).getReference()
                        .equals("STATION")) {
                    timeDistDistance = new QuantityImpl((eventStationDistance - timeDist.dist)
                                                                * 180 / Math.PI,
                                                        UnitImpl.KILOMETER);
                }
                minDistance = ((Relative)phaseInteractionType).getDistanceRange()
                        .getMin();
                maxDistance = ((Relative)phaseInteractionType).getDistanceRange()
                        .getMax();
            } else {
                minDistance = timeDistDistance;
                maxDistance = timeDistDistance;
            }
            if(minDepth.lessThanEqual(timeDistDepth)
                    && maxDepth.greaterThanEqual(timeDistDepth)) {
                if(minDistance.lessThanEqual(timeDistDistance)
                        && maxDistance.greaterThanEqual(timeDistDistance)) { return true; }
            }//end of if checking for depth.
        }//end of for(int counter = 0; ........)
        return false;
    }

    public boolean handleRelativePathInteraction(Arrival[] requiredArrivals,
                                                 double eventStationDistance)
            throws Exception {
        for(int counter = 0; counter < requiredArrivals.length; counter++) {
            TimeDist[] timeDistArray = requiredArrivals[counter].getPath();
            if(checkForRelativePathInteraction(timeDistArray,
                                               0,
                                               timeDistArray.length,
                                               eventStationDistance,
                                               requiredArrivals[counter].getDistDeg())) { return true; }
        }
        return false;
    }

    public boolean checkForRelativePathInteraction(TimeDist[] timeDistArray,
                                                   int start,
                                                   int end,
                                                   double eventStationDistance,
                                                   double totalDistance)
            throws Exception {
        //for(int i = 0; i < timeDistArray.length; i++) {
        int counter = 0;
        if(end < start) return false;
        int mid = (start + end) / 2;
        TimeDist timeDist = timeDistArray[mid];
        QuantityImpl minDistance = null;
        QuantityImpl maxDistance = null;
        QuantityImpl timeDistDistance = new QuantityImpl(timeDist.dist * 180
                / Math.PI, UnitImpl.DEGREE);
        while(counter < totalDistance) {
            QuantityImpl timeDistDepth = new QuantityImpl(timeDist.depth,
                                                          UnitImpl.KILOMETER);
            QuantityImpl minDepth;
            QuantityImpl maxDepth;
            if(((Relative)phaseInteractionType).getDepthRange() != null) {
                minDepth = ((Relative)phaseInteractionType).getDepthRange()
                        .getMinDepth();
                maxDepth = ((Relative)phaseInteractionType).getDepthRange()
                        .getMaxDepth();
            } else {
                minDepth = timeDistDepth;
                maxDepth = timeDistDepth;
            }
            if(((Relative)phaseInteractionType).getDistanceRange() != null) {
                if(((Relative)phaseInteractionType).getReference()
                        .equals("STATION")) {
                    timeDistDistance = new QuantityImpl((eventStationDistance - timeDist.dist)
                                                                * 180 / Math.PI,
                                                        UnitImpl.DEGREE);
                }
                minDistance = ((Relative)phaseInteractionType).getDistanceRange()
                        .getMin();
                maxDistance = ((Relative)phaseInteractionType).getDistanceRange()
                        .getMax();
            } else {
                minDistance = timeDistDistance;
                maxDistance = timeDistDistance;
            }
            if(minDepth.lessThanEqual(timeDistDepth)
                    && maxDepth.greaterThanEqual(timeDistDepth)) {
                minDistance = new QuantityImpl(minDistance.value + counter,
                                               UnitImpl.DEGREE);
                maxDistance = new QuantityImpl((360 + counter)
                        - minDistance.value, UnitImpl.DEGREE);
                QuantityImpl tempDistance = new QuantityImpl(counter
                        - timeDistDistance.value, UnitImpl.DEGREE);
                if((minDistance.lessThanEqual(timeDistDistance) && maxDistance.greaterThanEqual(timeDistDistance))
                        || (minDistance.lessThanEqual(tempDistance) && maxDistance.greaterThanEqual(tempDistance))) { return true; }
            }
            counter += 360;
        }
        if(end < start) {
            return false;
        } else if(minDistance.greaterThan(timeDistDistance)) {
            return checkForRelativePathInteraction(timeDistArray,
                                                   mid,
                                                   end,
                                                   eventStationDistance,
                                                   totalDistance);
        } else if(maxDistance.lessThan(timeDistDistance)) {
            return checkForRelativePathInteraction(timeDistArray,
                                                   start,
                                                   mid,
                                                   eventStationDistance,
                                                   totalDistance);
        } else return false;
        //}
    }

    public boolean handleAbsolutePhaseInteraction(Arrival[] requiredArrivals,
                                                  double azimuth,
                                                  OriginImpl origin,
                                                  String type) throws Exception {
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
                QuantityImpl timeDistDepth = new QuantityImpl(timeDist[0].depth,
                                                              UnitImpl.KILOMETER);
                ;
                QuantityImpl minDepth;
                QuantityImpl maxDepth;
                if(((Absolute)phaseInteractionType).getDepthRange() != null) {
                    minDepth = ((Absolute)phaseInteractionType).getDepthRange()
                            .getMinDepth();
                    maxDepth = ((Absolute)phaseInteractionType).getDepthRange()
                            .getMaxDepth();
                } else {
                    minDepth = timeDistDepth;
                    maxDepth = timeDistDepth;
                }
                if(minDepth.lessThanEqual(timeDistDepth)
                        && maxDepth.greaterThanEqual(timeDistDepth)) {
                    if(area == null || area instanceof GlobalArea) return true;
                    double tLat = SphericalCoords.latFor(origin.getLocation().latitude,
                                                         origin.getLocation().longitude,
                                                         timeDist[counter].depth,
                                                         azimuth);
                    double tLon = SphericalCoords.lonFor(origin.getLocation().latitude,
                                                         origin.getLocation().longitude,
                                                         timeDist[counter].depth,
                                                         azimuth);
                    if(area instanceof BoxArea) {
                        BoxArea boxArea = (BoxArea)area;
                        if(tLat >= boxArea.min_latitude
                                && tLat <= boxArea.max_latitude
                                && tLon >= boxArea.min_longitude
                                && tLon <= boxArea.max_longitude) { return true; }
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
        for(int counter = 1; counter < timeDist.length - 1; counter++) {
            current = timeDist[counter].depth;
            next = timeDist[counter + 1].depth;
            if(interactionStyle.equals("TOPSIDE REFLECTION")) {
                //("dist = "+distance+" depth = "+depth+" past = "+past+"
                // current = "+current+" next = "+next);
            }
            if(interactionStyle.equals("TOPSIDE REFLECTION") && past < current
                    && current > next) {
                return timeDist[counter];
            } else if(interactionStyle.equals("BOTTOMSIDE REFLECTION")
                    && past > current && current < next) { return timeDist[counter]; }
            past = current;
        }
        return null;
    }

    public Arrival[] getRequiredArrival(Arrival[] arrivals) {
        ArrayList arrayList = new ArrayList();
        for(int counter = 0; counter < arrivals.length; counter++) {
            String arrivalName = arrivals[counter].getName();
            if(phaseName.startsWith("tt")) {
                if(phaseName.equals("tts")
                        && arrivalName.toUpperCase().startsWith("S")) {
                    arrayList.add(arrivals[counter]);
                } else if(phaseName.equals("ttp")
                        && arrivalName.toUpperCase().startsWith("P")) {
                    arrayList.add(arrivals[counter]);
                }
            } else if(phaseName.equals(arrivalName)) {
                arrayList.add(arrivals[counter]);
            }
        }
        Arrival[] requiredArrivals = new Arrival[arrayList.size()];
        requiredArrivals = (Arrival[])arrayList.toArray(requiredArrivals);
        return requiredArrivals;
    }

    public double checkForLongway(double distance, double azimuth) {
        while(distance > 360)
            distance = distance - 360;
        if(distance > 180) {
            azimuth = azimuth + 180;
        }
        return azimuth;
    }

    private String modelName = "prem";

    private String phaseName = null;

    private String interactionStyle = null;

    private int interactionNumber = 1;

    private PhaseInteractionType phaseInteractionType = null;

    private TauP_Pierce tauPPierce;

    private TauP_Path tauPPath;
}// PhaseInteraction
