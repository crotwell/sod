/**
 * SimplePhaseStoN.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.bag;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

/** Calculates a signal to noise ration around a phase. The short time window
 * (numerator of the ratio) is given by the standard deviation of the section of the seismogram
 * from phase + shortOffsetBegin to phase + shortOffsetEnd. The long time
 * window (demominator of the ratio) is similar. The first arriving phase of
 * the calculated arrivals is used. */
public class SimplePhaseStoN {

    public SimplePhaseStoN(String phase,
                           Duration shortOffsetBegin,
                           Duration shortOffsetEnd,
                           String longPhase,
                           Duration longOffsetBegin,
                           Duration longOffsetEnd,
                           TauPUtil taup) throws TauModelException {
        this.phase = phase;
        this.longPhase = longPhase;
        this.shortOffsetBegin = shortOffsetBegin;
        this.shortOffsetEnd = shortOffsetEnd;
        this.longOffsetBegin = longOffsetBegin;
        this.longOffsetEnd = longOffsetEnd;

        if (shortOffsetBegin == null) {
            throw new NullPointerException("shortOffsetBegin cannot be null");
        }
        if (shortOffsetEnd == null) {
            throw new NullPointerException("shortOffsetEnd cannot be null");
        }
        if (longOffsetBegin == null) {
            throw new NullPointerException("longOffsetBegin cannot be null");
        }
        if (longOffsetEnd == null) {
            throw new NullPointerException("longOffsetEnd cannot be null");
        }
        this.taup = taup;
        shortCut = new PhaseCut(taup, phase, shortOffsetBegin, phase, shortOffsetEnd);
        longCut = new PhaseCut(taup, longPhase, longOffsetBegin, longPhase, longOffsetEnd);
    }


    public SimplePhaseStoN(String phase,
                           Duration shortOffsetBegin,
                           Duration shortOffsetEnd,
                           Duration longOffsetBegin,
                           Duration longOffsetEnd,
                           TauPUtil taup) throws TauModelException {
        this(phase,
             shortOffsetBegin,
             shortOffsetEnd,
             phase,
             longOffsetBegin,
             longOffsetEnd,
             taup);
    }

    public SimplePhaseStoN(String phase,
                           Duration shortOffsetBegin,
                           Duration shortOffsetEnd,
                           Duration longOffsetBegin,
                           Duration longOffsetEnd) throws TauModelException {
        this(phase,
             shortOffsetBegin,
             shortOffsetEnd,
             phase,
             longOffsetBegin,
             longOffsetEnd);
    }
    
    public SimplePhaseStoN(String phase,
                           Duration shortOffsetBegin,
                           Duration shortOffsetEnd,
                           String longPhase,
                           Duration longOffsetBegin,
                           Duration longOffsetEnd) throws TauModelException {
        this(phase,
             shortOffsetBegin,
             shortOffsetEnd,
             longPhase,
             longOffsetBegin,
             longOffsetEnd,
             TauPUtil.getTauPUtil("prem"));
    }

    /** Defaults to plus and minus 5 seconds around the phase for the short
     * time interval, and the preceding 100 seconds before that for the long
     * time interval. */
    public SimplePhaseStoN(String phase) throws TauModelException {
        this(phase,
             Duration.ofSeconds(-1),
             Duration.ofSeconds(+5),
             Duration.ofSeconds(-100),
             Duration.ofSeconds(-5));
    }

    /** Calculates the trigger value for the given windows. Returns null if
     * either of the windows have no data in them. */
    public LongShortTrigger process(Location stationLoc,
                                    OriginImpl origin,
                                    LocalSeismogramImpl seis) throws FissuresException, TauModelException, PhaseNonExistent {
        LocalSeismogramImpl shortSeis = shortCut.cut(stationLoc, origin, seis);
        LocalSeismogramImpl longSeis = longCut.cut(stationLoc, origin, seis);
        if (shortSeis == null || longSeis == null || shortSeis.getNumPoints() <= 1 || longSeis.getNumPoints() <= 1) { return null; }

        
        Statistics longStat = new Statistics(longSeis);
        double denominator = longStat.stddev();
        if (denominator == 0) {
            // check for all zero seis
            return null;
        }
        Statistics shortStat = new Statistics(shortSeis);
        // use the stddev of the short, but based on the mean of the
        // long term
        double numerator = Math.sqrt(shortStat.var(longStat.mean()));
        
        List<Arrival> arrivals = taup.calcTravelTimes(stationLoc, origin, new String[] {phase});
        Instant phaseTime = null;
        Instant originTime = origin.getOriginTime();
        if (arrivals.size() != 0) {
            phaseTime = originTime.plus(TimeUtils.durationFromSeconds(arrivals.get(0).getTime()));
        }

        Duration sampPeriod = seis.getSampling().getPeriod();
        int phaseIndex = (int)(TimeUtils.durationToDoubleSeconds(Duration.between(seis.getBeginTime(), phaseTime)) / TimeUtils.durationToDoubleSeconds(sampPeriod));
        float ratio = (float)(numerator/denominator);
        return new LongShortTrigger(seis, phaseIndex, ratio, (float)numerator, (float)denominator);
    }

    protected String phase, longPhase;
    protected Duration shortOffsetBegin;
    protected Duration shortOffsetEnd;
    protected Duration longOffsetBegin;
    protected Duration longOffsetEnd;
    protected PhaseCut shortCut;
    protected PhaseCut longCut;
    protected TauPUtil taup;
}

