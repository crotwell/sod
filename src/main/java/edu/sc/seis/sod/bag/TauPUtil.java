/**
 * TauPUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.bag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModel;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Time;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.OriginImpl;

public class TauPUtil {

    private TauPUtil(String modelName) throws TauModelException {
        taup_time = new TauP_Time(modelName);
    }

    public List<Arrival> calcTravelTimes(Station station, OriginImpl origin, String[] phaseNames) throws TauModelException {
        return calcTravelTimes(Location.of(station),
                               origin,
                               phaseNames);
    }

    public List<Arrival> calcTravelTimes(Channel channel, OriginImpl origin, String[] phaseNames) throws TauModelException {
        return calcTravelTimes(Location.of(channel), origin, phaseNames);
    }


    public synchronized List<Arrival> calcTravelTimes(Location stationLoc, OriginImpl origin, String[] phaseNames) throws TauModelException {
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

    public synchronized List<Arrival> calcTravelTimes(double distDeg, double depthKm, String[] phaseNames) throws TauModelException {
        taup_time.setSourceDepth(depthKm);
        taup_time.clearPhaseNames();
        for (int i = 0; i < phaseNames.length; i++) {
            taup_time.appendPhaseName(phaseNames[i]);
        }
        taup_time.calculate(distDeg);
        List<Arrival> arrivals = taup_time.getArrivals();
        return arrivals;
    }

    public TauModel getTauModel() {
        return taup_time.getTauModel();
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

    TauP_Time taup_time;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TauPUtil.class);
}

