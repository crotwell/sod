/**
 * PhaseCut.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.bag;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

public class PhaseCut {

    /** warning, this class assumes that no other thread will be accessing
     the TauP_Time class while it is being used here. If another thread
     accesses it, the results will be unpredictable. */
    public PhaseCut(TauPUtil timeCalc,
                    String beginPhase, Duration beginOffset,
                    String endPhase, Duration endOffset) {
        this.timeCalc = timeCalc;
        this.beginPhase = beginPhase;
        this.beginOffset = beginOffset;
        this.endPhase = endPhase;
        this.endOffset = endOffset;
    }

    /** Cuts the seismogram based on offsets from the given phases.
     *
     * @throws PhaseNonExistent if either of the phases does not exist
     *    at the distance.
     */
    public LocalSeismogramImpl cut(Location stationLoc,
                                   OriginImpl origin,
                                   LocalSeismogramImpl seis)
        throws TauModelException, PhaseNonExistent, FissuresException  {
        List<Arrival> beginArrivals = timeCalc.calcTravelTimes(stationLoc, origin, new String[] {beginPhase});
        List<Arrival> endArrivals = timeCalc.calcTravelTimes(stationLoc, origin, new String[] {endPhase});

        Instant beginTime = null;
        Instant endTime = null;
        Instant originTime = origin.getOriginTime();
        if (beginArrivals.size() != 0) {
            beginTime = originTime.plus(TimeUtils.durationFromSeconds(beginArrivals.get(0).getTime()));
            beginTime = beginTime.plus(beginOffset);
        } else {
            DistAz distAz = new DistAz(stationLoc, origin.getLocation());
            throw new PhaseNonExistent("Phase "+beginPhase+
                                           " does not exist at this distance, "+
                                           distAz.getDelta()+" degrees");
        }

        if (endArrivals.size() != 0) {
            endTime = originTime.plus(TimeUtils.durationFromSeconds(endArrivals.get(0).getTime()));
            endTime = endTime.plus(endOffset);
        } else {
            DistAz distAz = new DistAz(stationLoc, origin.getLocation());
            throw new PhaseNonExistent("Phase "+endPhase+
                                           " does not exist at this distance, "+
                                           distAz.getDelta()+" degrees");
        }
        Cut cut = new Cut(beginTime, endTime);
        return cut.apply(seis);
    }

    TauPUtil timeCalc;

    String beginPhase;

    Duration beginOffset;

    String endPhase;

    Duration endOffset;

    Logger logger = LoggerFactory.getLogger(PhaseCut.class);
}

