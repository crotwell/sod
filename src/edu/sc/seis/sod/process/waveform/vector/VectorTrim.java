package edu.sc.seis.sod.process.waveform.vector;

import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Cut;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class VectorTrim implements WaveformVectorProcess {

    public WaveformVectorResult process(EventAccessOperations event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        return new WaveformVectorResult(trim(seismograms),
                                        new StringTreeLeaf(this,
                                                           true,
                                                           "Each vector of equal size"));
    }

    public LocalSeismogramImpl[][] trim(LocalSeismogramImpl[][] vector)
            throws FissuresException {
        return cutVector(vector, findSmallestCoveringCuts(vector));
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
                return new Cut(TimeUtils.futurePlusOne,
                               new MicroSecondDate(-100000000000000l));
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
                    return new Cut(TimeUtils.futurePlusOne,
                                   new MicroSecondDate(-100000000000000l));
                }
            }
        }
        return c;
    }
}
