/**
 * TauPUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.bag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.sc.seis.TauP.*;
import edu.sc.seis.TauP.cmdline.TauP_Time;
import edu.sc.seis.TauP.cmdline.args.PhaseArgs;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.OriginImpl;

public class TauPUtil {

    private TauPUtil(String modelName) throws TauModelException {
        tMod = TauModelLoader.load(modelName);
    }

    public List<Arrival> calcTravelTimes(Station station, OriginImpl origin, String[] phaseNames) throws TauPException {
        return calcTravelTimes(Location.of(station),
                               origin,
                               phaseNames);
    }

    public List<Arrival> calcTravelTimes(Channel channel, OriginImpl origin, String[] phaseNames) throws TauPException {
        return calcTravelTimes(Location.of(channel), origin, phaseNames);
    }


    public synchronized List<Arrival> calcTravelTimes(Location stationLoc, OriginImpl origin, String[] phaseNames) throws TauPException {
        QuantityImpl depth = (QuantityImpl)origin.getLocation().depth;
        depth = depth.convertTo(UnitImpl.KILOMETER);
        double depthVal = depth.getValue();
        if (depthVal < 0) {
            // TauP can't handle negative depths
            logger.info("depth negative not allowed, setting to zero");
            depthVal = 0;
        }
        DistAz distAz = new DistAz(stationLoc, origin.getLocation());
        return calcTravelTimes(distAz.getDelta(), depthVal, phaseNames);
    }

    public synchronized List<Arrival> calcTravelTimes(double distDeg, double depthKm, String[] phaseNames) throws TauPException {
        double receiverDepth = 0;
        List<Arrival> arrivals = new ArrayList<>();
        TauModel tModDepth = tMod.depthCorrect(depthKm);
        List<String> parsedNames = new ArrayList<>();
        for (String pn : phaseNames) {
            parsedNames.addAll(PhaseArgs.extractPhaseNames(pn));
        }
        for (String phaseName : parsedNames) {
            SimpleSeismicPhase phase = SeismicPhaseFactory.createPhase(phaseName, tModDepth, tModDepth.getSourceDepth(), receiverDepth);
            List<Arrival> arrivalList = DistanceRay.ofDegrees(distDeg).calculate(phase);
            arrivals.addAll(arrivalList);
        }
        return arrivals;
    }

    public TauModel getTauModel() {
        return tMod;
    }

    public synchronized static TauPUtil getTauPUtil() {
        try {
            return getTauPUtil("prem");
        } catch(TauModelException e) {
            throw new RuntimeException("Should never happen as prem is bundled with TauP", e);
        }
    }

    public synchronized static TauPUtil getTauPUtil(String modelName) throws TauModelException {
        if ( ! taupUtilMap.containsKey(modelName)) {
            taupUtilMap.put(modelName, new TauPUtil(modelName));
        }
        return (TauPUtil)taupUtilMap.get(modelName);
    }

    static Map<String, TauPUtil> taupUtilMap = new HashMap<String, TauPUtil>();

    TauModel tMod;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TauPUtil.class);
}

