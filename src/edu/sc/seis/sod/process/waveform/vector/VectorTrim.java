package edu.sc.seis.sod.process.waveform.vector;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Cut;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class VectorTrim implements WaveformVectorProcess {

    private static final Cut EMPTY_CUT = new Cut(TimeUtils.futurePlusOne,
                                                 new MicroSecondDate(-100000000000000l));

    public WaveformVectorResult process(CacheEvent event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        for(int i = 0; i < seismograms.length; i++) {
            if(seismograms[i].length == 0) {
                return new WaveformVectorResult(seismograms,
                                                new StringTreeLeaf(this,
                                                                   false,
                                                                   "At least one vector missing seismograms"));
            }
        }
        try {
            return new WaveformVectorResult(trim(seismograms),
                                            new StringTreeLeaf(this,
                                                               true,
                                                               "Each vector of equal size"));
        } catch(IllegalArgumentException e) {
            return new WaveformVectorResult(seismograms,
                                            new StringTreeLeaf(this,
                                                               false,
                                                               e.getMessage()));
        }
    }

    public LocalSeismogramImpl[][] trim(LocalSeismogramImpl[][] vector)
            throws FissuresException {
        if(normalizeSampling(vector)) {
            return cutVector(vector, findSmallestCoveringCuts(vector));
        } else {
            throw new IllegalArgumentException("Unable to normalize samplings on seismograms in vector.  These can not be trimmed to the same length");
        }
    }

    public LocalSeismogramImpl[][] cutVector(LocalSeismogramImpl[][] vector,
                                             Cut[] c) throws FissuresException {
        LocalSeismogramImpl[][] results = new LocalSeismogramImpl[vector.length][];
        for(int i = 0; i < vector.length; i++) {
            List iResults = new ArrayList();
            for(int j = 0; j < vector[i].length; j++) {
                for(int k = 0; k < c.length; k++) {
                    LocalSeismogramImpl cutSeis = c[k].apply(vector[i][j]);
                    if(cutSeis != null) {
                        cutSeis.begin_time = c[k].getBegin().getFissuresTime();//Align all seis on microseconds
                        if(cutSeis.getEndTime().after(c[k].getEnd())) {//Since they weren't aligned during initial cut, there could be an extra point
                            cutSeis = Cut.cut(cutSeis,
                                             0,
                                              cutSeis.getNumPoints() - 1);
                       }
                        iResults.add(cutSeis);
                    }
                }
            }
            results[i] = (LocalSeismogramImpl[])iResults.toArray(new LocalSeismogramImpl[0]);
        }
        return results;
    }

    public Cut[] findSmallestCoveringCuts(LocalSeismogramImpl[][] vector) {
        List results = new ArrayList();
        for(int i = 0; i < vector[0].length; i++) {
            results.add(findSmallestCoveringCut(vector[0][i].getBeginTime(),
                                                vector[0][i].getEndTime(),
                                                vector));
        }
        return (Cut[])results.toArray(new Cut[0]);
    }

    private Cut findSmallestCoveringCut(MicroSecondDate start,
                                        MicroSecondDate end,
                                        LocalSeismogramImpl[][] vector) {
        Cut c = new Cut(start, end);
        for(int i = 1; i < vector.length; i++) {
            if(vector[i].length == 0) {// Cut everything
                return EMPTY_CUT;
            }
            for(int j = 0; j < vector[i].length; j++) {
                if(c.overlaps(vector[i][j])) {
                    if(vector[i][j].getBeginTime().after(c.getBegin())) {
                        c = new Cut(vector[i][j].getBeginTime(), c.getEnd());
                    }
                    if(vector[i][j].getEndTime().before(c.getEnd())) {
                        c = new Cut(c.getBegin(), vector[i][j].getEndTime());
                    }
                    break;
                }
                if(j == vector[i].length - 1) {
                    return EMPTY_CUT;
                }
            }
        }
        return c;
    }

    /**
     * If the passed in seismograms sampling periods are all within 1% in size,
     * each has its sampling set to the sampling of the first sampling
     */
    public boolean normalizeSampling(LocalSeismogramImpl[][] impls) {
        QuantityImpl lastPeriod = null;
        for(int i = 0; i < impls.length; i++) {
            for(int j = 0; j < impls[i].length; j++) {
                TimeInterval curPeriod = (TimeInterval)impls[i][j].getSampling()
                        .getPeriod()
                        .convertTo(UnitImpl.SECOND);
                if(lastPeriod != null) {
                    if(Math.abs(1 - lastPeriod.divideBy(curPeriod).getValue()) > .01) {
                        return false;
                    }
                }
                lastPeriod = curPeriod;
            }
        }
        for(int i = 0; i < impls.length; i++) {
            for(int j = 0; j < impls[i].length; j++) {
                impls[i][j].sampling_info = impls[0][0].sampling_info;
            }
        }
        return true;
    }
}
