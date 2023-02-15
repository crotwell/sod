package edu.sc.seis.sod.bag;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.EncodedData;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.seismogram.TimeSeriesDataSel;
import edu.sc.seis.sod.util.time.RangeTool;
import edu.sc.seis.sod.util.time.ReduceTool;

/**
 * Cuts seismograms based on a begin and end time.
 * 
 * 
 * Created: Tue Oct 1 21:23:44 2002
 * 
 * @author Philip Crotwell
 * @version $Id: Cut.java 21318 2010-05-26 16:41:04Z crotwell $
 */
public class Cut implements LocalSeismogramFunction {

    public Cut(Instant begin, Instant end) {
        this.begin = begin;
        this.end = end;
    }

    public Cut(RequestFilter request) {
        this(request.startTime,
             request.endTime);
    }

    /**
     * @return - a seismogram cut to the configured time window. The original
     *         seismogram is not modified. Returns null if no data is within the
     *         cut window.
     */
    public LocalSeismogramImpl apply(LocalSeismogramImpl seis)
            throws FissuresException {
        if(!overlaps(seis)) {
            return null;
        } else if(seis.getBeginTime().equals(getBegin())
                && seis.getEndTime().equals(getEnd())) {
            return seis;
        }
        // first trim unneeded encoded data. This helps a lot for large seismograms as no decompression needed
        LocalSeismogramImpl tmpSeis = applyEncoded(seis);
        int beginIndex = getBeginIndex(tmpSeis);
        int endIndex = getEndIndex(tmpSeis);
        return cut(tmpSeis, beginIndex, endIndex);
    }

    public static LocalSeismogramImpl cut(LocalSeismogramImpl seis, int beginIndex, int endIndex) throws FissuresException {
        LocalSeismogramImpl outSeis;
        if (beginIndex < 0) {beginIndex = 0;}
        if (endIndex > seis.getNumPoints()) {endIndex = seis.getNumPoints();}
        seis = cutEncoded(seis, beginIndex, endIndex); // fast coarse cut if encoded
        if(seis.can_convert_to_short()) {
            short[] outS = new short[endIndex - beginIndex+1];
            short[] inS = seis.get_as_shorts();
            System.arraycopy(inS, beginIndex, outS, 0, endIndex - beginIndex+1);
            outSeis = new LocalSeismogramImpl(seis, outS);
        } else if(seis.can_convert_to_long()) {
            int[] outI = new int[endIndex - beginIndex+1];
            int[] inI = seis.get_as_longs();
            System.arraycopy(inI, beginIndex, outI, 0, endIndex - beginIndex+1);
            outSeis = new LocalSeismogramImpl(seis, outI);
        } else if(seis.can_convert_to_float()) {
            float[] outF = new float[endIndex - beginIndex+1];
            float[] inF = seis.get_as_floats();
            System.arraycopy(inF, beginIndex, outF, 0, endIndex - beginIndex+1);
            outSeis = new LocalSeismogramImpl(seis, outF);
        } else {
            double[] outD = new double[endIndex - beginIndex+1];
            double[] inD = seis.get_as_doubles();
            System.arraycopy(inD, beginIndex, outD, 0, endIndex - beginIndex+1);
            outSeis = new LocalSeismogramImpl(seis, outD);
        } // end of else
        outSeis.begin_time = seis.getBeginTime()
                .plus(seis.getSampling()
                        .getPeriod()
                        .multipliedBy(beginIndex));
        return outSeis;
    }

    public boolean overlaps(LocalSeismogramImpl seis) {
        return begin.isBefore(seis.getEndTime())
                && end.isAfter(seis.getBeginTime());
    }

    protected int getEndIndex(LocalSeismogramImpl seis) {
        Duration sampPeriod = seis.getSampling().getPeriod();
        Duration endShift = Duration.between(seis.getBeginTime(), end);
        double endShiftSecs = TimeUtils.durationToDoubleSeconds(endShift) / TimeUtils.durationToDoubleSeconds(sampPeriod);
        int endIndex = (int)Math.floor(endShiftSecs);
        if(endIndex < 0) {
            endIndex = 0;
        }
        if(endIndex >= seis.getNumPoints()) {
            endIndex = seis.getNumPoints()-1;
        }
        return endIndex;
    }

    protected int getBeginIndex(LocalSeismogramImpl seis) {
        Duration sampPeriod = seis.getSampling().getPeriod();
        Duration beginShift = Duration.between(seis.getBeginTime(), begin);
        double beginShiftSecs = TimeUtils.durationToDoubleSeconds(beginShift) / TimeUtils.durationToDoubleSeconds(sampPeriod);
        int beginIndex = (int)Math.ceil(beginShiftSecs);
        if(beginIndex < 0) {
            beginIndex = 0;
        } // end of if (beginIndex < 0)
        if(beginIndex >= seis.getNumPoints()) {
            beginIndex = seis.getNumPoints() - 1;
        }
        return beginIndex;
    }

    public String toString() {
        return "Cut from " + begin + " to " + end;
    }

    public Instant getBegin() {
        return begin;
    }

    public Instant getEnd() {
        return end;
    }

    private Instant begin, end;

    public static final UnitImpl SEC_PER_SEC = UnitImpl.divide(UnitImpl.SECOND,
                                                               UnitImpl.SECOND);

    public RequestFilter apply(RequestFilter original) {
        RequestFilter result = new RequestFilter(original.channelId, original.startTime, original.endTime);
        if(begin.isAfter(original.getEndTime()) || end.isBefore(original.getStartTime())) {
            return null;
        } // end of if ()
        if(begin.isAfter(original.getStartTime())) {
            result.startTime = begin;
        } else {
            result.startTime = original.startTime;
        }
        if(end.isBefore(original.getEndTime())) {
            result.endTime = end;
        } else {
            result.endTime = original.endTime;
        }
        return result;
    }
    


    /**
     * Makes a seismogram covering as little extra beyond begin and end times of
     * this cut without extracting the data from the encoded data array. This
     * means there may be a few extra points around the begin and end time as
     * the encoded data segments probably won't line up with the cut times. If
     * the cut and the seismogram have no time in common, null is returned. If
     * the data isn't encoded, no cut is performed
     * 
     * @return an encoded seismogram covering as little of cut time as possible
     *         or null if there's no overlap
     * @throws FissuresException
     * 
     */
    public LocalSeismogramImpl applyEncoded(LocalSeismogramImpl seis)
            throws FissuresException {
        if(!seis.is_encoded()) {
            return seis;
        }
        if(!overlaps(seis)) {
            return null;
        }
        int beginIndex = getBeginIndex(seis);
        int endIndex = getEndIndex(seis);
        return cutEncoded(seis, beginIndex, endIndex);
    }
    
    public static LocalSeismogramImpl cutEncoded(LocalSeismogramImpl seis, int beginIndex, int endIndex)
            throws FissuresException {
        if(!seis.is_encoded()) {
            return seis;
        }
        List<EncodedData> outData = new ArrayList<EncodedData>();
        EncodedData[] ed = seis.get_as_encoded();
        int currentPoint = 0;
        int firstUsedPoint = -1;
        int pointsInNewSeis = 0;
        for(int i = 0; i < ed.length && currentPoint <= endIndex; i++) {
            if(currentPoint + ed[i].num_points > beginIndex) {
                outData.add(ed[i]);
                pointsInNewSeis += ed[i].num_points;
                if(firstUsedPoint == -1) {
                    firstUsedPoint = currentPoint;
                }
            }
            currentPoint += ed[i].num_points;
        }
        TimeSeriesDataSel ds = new TimeSeriesDataSel();
        ds.encoded_values((EncodedData[])outData.toArray(new EncodedData[outData.size()]));
        LocalSeismogramImpl outSeis = new LocalSeismogramImpl(seis, ds);
        outSeis.begin_time = seis.getBeginTime()
                .plus(seis.getSampling()
                        .getPeriod()
                        .multipliedBy(firstUsedPoint));
        outSeis.num_points = pointsInNewSeis;
        return outSeis;
    }
    
    /** Applys a coarse cut to the seismograms based on the request filter. This uses Cut.applyEncoded to cut
     * sections of encoded data without decompressing first. Thus large data volumes can be reduced without memory problems.
     */
    public static LocalSeismogramImpl[] coarseCut(RequestFilter[] aFilterseq, LocalSeismogramImpl[] seis) throws FissuresException {
        List<LocalSeismogramImpl> out = new ArrayList<LocalSeismogramImpl>();
        RequestFilter[] mergedRequest = ReduceTool.merge(aFilterseq);
        for (int i = 0; i < mergedRequest.length; i++) {
            Cut c = new Cut(mergedRequest[i]);
            for (int j = 0; j < seis.length; j++) {
                LocalSeismogramImpl tmpSeis = c.applyEncoded((LocalSeismogramImpl)seis[j]);
                if (tmpSeis != null) {
                    out.add(tmpSeis);
                }
            }
        }
        
        return out.toArray(new LocalSeismogramImpl[0]);
    }
    
    
    /**
     * Return an array with any overlapping seismograms turned into a single
     * seismogram and any contained seismograms thrown away. The input array is
     * unmodified.
     */
    public static LocalSeismogramImpl[] cutOverlap(LocalSeismogramImpl[] seis)
            throws FissuresException {
        // Don't modify the passed in array
        LocalSeismogramImpl[] tmp = new LocalSeismogramImpl[seis.length];
        for(int i = 0; i < tmp.length; i++) {
            tmp[i] = seis[i];
        }
        seis = tmp;
        ReduceTool.LSMerger merger = new ReduceTool.LSMerger();
        for(int i = 0; i < seis.length; i++) {
            if(seis[i] == null) {
                continue;
            }
            boolean changeMade;
            do {
                changeMade = false;
                for(int j = i + 1; j < seis.length; j++) {
                    if(seis[j] == null) {
                        continue;
                    }
                    if(ReduceTool.contains(seis[i], seis[j])) {
                        seis[j] = null;
                        changeMade = true;
                    } else if(ReduceTool.contains(seis[j], seis[i])) {
                        seis[i] = seis[j];
                        seis[j] = null;
                        changeMade = true;
                    } else if(RangeTool.areOverlapping(seis[i], seis[j])) {
                        Instant iEnd = seis[i].getEndTime();
                        Instant iBegin = seis[i].getBeginTime();
                        Duration halfSample = seis[i].getSampling()
                                .getPeriod()
                                .dividedBy(2);
                        if(iEnd.isBefore(seis[j].getEndTime())) {
                            // overlap on i's end
                            Cut cut = new Cut(iEnd.plus(halfSample),
                                              seis[j].getEndTime());
                            seis[j] = cut.apply(seis[j]);
                        } else {
                            Cut cut = new Cut(seis[j].getBeginTime(),
                                              iBegin.minus(halfSample));
                            seis[j] = cut.apply(seis[j]);
                        }
                        if (seis[j] != null) {
                            seis[i] = merger.merge(seis[i], seis[j]);
                            seis[j] = null;
                        }
                        changeMade = true;
                    }
                }
            } while(changeMade);
        }
        List results = new ArrayList(seis.length);
        for(int i = 0; i < seis.length; i++) {
            if(seis[i] != null) {
                results.add(seis[i]);
            }
        }
        return (LocalSeismogramImpl[])results.toArray(new LocalSeismogramImpl[0]);
    }


}// Cut
