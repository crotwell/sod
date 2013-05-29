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
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Cut;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.process.waveform.CollapseOverlaps;
import edu.sc.seis.sod.process.waveform.Merge;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class VectorTrim implements WaveformVectorProcess, Threadable {

    private static final Cut EMPTY_CUT = new Cut(TimeUtils.futurePlusOne,
                                                 new MicroSecondDate(-100000000000000l));

    public WaveformVectorResult accept(CacheEvent event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        WaveformVectorResult collapseResult = collapser.accept(event, channelGroup, original, available, seismograms, cookieJar);
        if ( ! collapseResult.isSuccess()) {
            return new WaveformVectorResult(false, collapseResult.getSeismograms(), new StringTreeBranch(this, false, collapseResult.getReason()));
        }
        seismograms = collapseResult.getSeismograms();
        WaveformVectorResult mergeResult = merger.accept(event, channelGroup, original, available, seismograms, cookieJar);
        if ( ! mergeResult.isSuccess()) {
            return new WaveformVectorResult(false, mergeResult.getSeismograms(), new StringTreeBranch(this, false, mergeResult.getReason()));
        }
        seismograms = mergeResult.getSeismograms();
        System.out.println("merge   "+seismograms[0][0].getChannelID().channel_code+seismograms[0][0].getNumPoints()+" "+seismograms[1][0].getNumPoints()+" "+seismograms[2][0].getNumPoints()+" ");
        
        if (seismograms[0].length != seismograms[1].length || seismograms[0].length != seismograms[2].length) {
            return new WaveformVectorResult(seismograms,
                                            new StringTreeLeaf(this,
                                                               false,
                                                               "Unequal number of seismograms in the three components: "+seismograms[0].length+" "+seismograms[1].length+" "+seismograms[2].length));

        }
        for(int i = 0; i < seismograms.length; i++) {
            if(seismograms[i].length == 0) {
                return new WaveformVectorResult(seismograms,
                                                new StringTreeLeaf(this,
                                                                   false,
                                                                   "At least one vector has no seismograms: "+ChannelIdUtil.toString(original[i][0].channel_id)));
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
            System.out.println("norm   "+vector[0][0].getChannelID().channel_code+vector[0][0].getNumPoints()+" "+vector[1][0].getNumPoints()+" "+vector[2][0].getNumPoints()+" ");
            LocalSeismogramImpl[][] cutSeis = cutVector(vector, findSmallestCoveringCuts(vector));
            System.out.println("cut   "+cutSeis[0][0].getChannelID().channel_code+cutSeis[0][0].getNumPoints()+" "+cutSeis[1][0].getNumPoints()+" "+cutSeis[2][0].getNumPoints()+" ");
            // check time alignment
            LocalSeismogramImpl[][] out = new LocalSeismogramImpl[3][cutSeis[0].length];
            for (int i = 0; i < cutSeis[0].length; i++) {
                out[0][i] = cutSeis[0][i];
                out[1][i] = alignTimes(cutSeis[0][i], cutSeis[1][i]);
                out[2][i] = alignTimes(cutSeis[0][i], cutSeis[2][i]);
            }
            System.out.println("alignTimes   "+out[0][0].getChannelID().channel_code+out[0][0].getNumPoints()+" "+out[1][0].getNumPoints()+" "+out[2][0].getNumPoints()+" ");
            return out;
        } else {
            throw new IllegalArgumentException("Unable to normalize samplings on seismograms in vector.  These can not be trimmed to the same length");
        }
    }
    
    public static LocalSeismogramImpl alignTimes(LocalSeismogramImpl main, LocalSeismogramImpl shifty) throws FissuresException {
        shifty = SampleSyncronize.alignTimes(main, shifty);
        System.out.println("main   "+main.getChannelID().channel_code+main.getNumPoints()+" "+main.getBeginTime()+" "+main.getBeginTime().getMicroSecondTime()+" "+main.getSampling());
        System.out.println("shifty "+shifty.getChannelID().channel_code+shifty.getNumPoints()+" "+shifty.getBeginTime()+" "+shifty.getBeginTime().getMicroSecondTime()+" "+shifty.getSampling());
        if (shifty.getNumPoints() == main.getNumPoints() +1) {
            // looks like we are long by one
            if (shifty.getBeginTime().difference(main.getBeginTime()).lessThan(shifty.getBeginTime().add(shifty.getSampling().getPeriod()).difference(main.getBeginTime()))) {
                // first data point closer than second, so chop end 
                shifty = Cut.cut(shifty, 0, shifty.getNumPoints()-2); //cut by index inclusive, so -2 to trim 1 from end
            } else {
                // second is close, so chop begin
                shifty = Cut.cut(shifty, 1, shifty.getNumPoints()-1); //cut by index inclusive, so -1 to not trim end
            }
        }
        System.out.println("cut main   "+main.getNumPoints()+" "+main.getBeginTime());
        System.out.println("cut shifty "+shifty.getNumPoints()+" "+shifty.getBeginTime());
        if (shifty.getNumPoints() == main.getNumPoints()) {
            shifty.begin_time = main.begin_time; // just make sure we are lined up
            return shifty;
        } 
        if (shifty.getNumPoints() == main.getNumPoints()-1) {
            // Oops!
            throw new RuntimeException("Oops, cut ends up with too few points");
        }
        throw new RuntimeException("Oops, can't handle different num points: main="+main.getNumPoints()+" shifty="+shifty.getNumPoints());
    }

    public LocalSeismogramImpl[][] cutVector(LocalSeismogramImpl[][] vector,
                                             Cut[] c) throws FissuresException {
        LocalSeismogramImpl[][] results = new LocalSeismogramImpl[vector.length][];
        for(int i = 0; i < vector.length; i++) {
            List<LocalSeismogramImpl> iResults = new ArrayList<LocalSeismogramImpl>();
            for(int j = 0; j < vector[i].length; j++) {
                for(int k = 0; k < c.length; k++) {
                    LocalSeismogramImpl cutSeis = c[k].apply(vector[i][j]);
                    System.out.println("incut "+k+"  "+cutSeis.getChannelID().channel_code+" "+vector[i][j].getBeginTime().getMicroSecondTime() +" "+vector[i][j].getNumPoints()+" "+cutSeis.getNumPoints()+" "+c[k].getBegin()+" "+c[k].getEnd());
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
        List<Cut> results = new ArrayList<Cut>();
        for(int i = 0; i < vector[0].length; i++) {
            Cut cut = findSmallestCoveringCut(vector[0][i].getBeginTime(),
                                              vector[0][i].getEndTime(),
                                              vector);
            // add extra 1/2 sample to begin and end in case seismograms are not quite time aligned
            TimeInterval halfSampPeriod = ((TimeInterval)vector[0][i].getSampling().getPeriod().divideBy(2));
            cut = new Cut(cut.getBegin().subtract(halfSampPeriod), cut.getEnd().add(halfSampPeriod));
            results.add(cut);
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
    
    public boolean isThreadSafe() {
        return true;
    }
    
    private ANDWaveformProcessWrapper merger = new ANDWaveformProcessWrapper(new Merge());
    private ANDWaveformProcessWrapper collapser = new ANDWaveformProcessWrapper(new CollapseOverlaps());
}
