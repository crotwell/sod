package edu.sc.seis.sod.util.time;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.EncodedData;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.Plottable;
import edu.sc.seis.sod.model.seismogram.PlottableChunk;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.seismogram.TimeSeriesDataSel;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.util.display.SimplePlotUtil;

/**
 * @author groves Created on Oct 29, 2004
 */
public class ReduceTool {

    public static boolean contains(LocalSeismogramImpl container,
                                    LocalSeismogramImpl containee) {
        return equalsOrBefore(container.getBeginTime(),
                              containee.getBeginTime())
                && equalsOrAfter(container.getEndTime(), containee.getEndTime());
    }
    
    /** moved to sod-bag Cut */
    @Deprecated
    public static LocalSeismogramImpl[] cutOverlap(LocalSeismogramImpl[] seis) throws FissuresException
    {
    throw new RuntimeException("Use Cut.cutOverlap");
    }
    
    public static LocalSeismogramImpl[] removeContained(LocalSeismogramImpl[] seis) {
        SortTool.byLengthAscending(seis);
        List results = new ArrayList();
        for(int i = 0; i < seis.length; i++) {
            Instant iEnd = seis[i].getEndTime();
            Instant iBegin = seis[i].getBeginTime();
            boolean contained = false;
            for(int j = i + 1; j < seis.length && !contained; j++) {
                if(equalsOrAfter(iBegin, seis[j].getBeginTime())
                        && equalsOrBefore(iEnd, seis[j].getEndTime())) {
                    contained = true;
                }
            }
            if(!contained) {
                results.add(seis[i]);
            }
        }
        return (LocalSeismogramImpl[])results.toArray(new LocalSeismogramImpl[0]);
    }

    /**
     * Unites contiguous and equal seismograms into a single
     * LocalSeismogramImpl. Partially overlapping seismograms are left separate.
     */
    public static LocalSeismogramImpl[] merge(LocalSeismogramImpl[] seis) {
        return new LSMerger().merge(seis);
    }

    /**
     * Unites all RequestFilters for the same channel in the given array into a
     * single requestfilter if they're contiguous or overlapping in time.
     */
    public static RequestFilter[] merge(RequestFilter[] ranges) {
        return new RFMerger().merge(ranges);
    }

    public static List<RequestFilter> trimTo(List<RequestFilter> rfList, List<RequestFilter> windowList) {
        List<RequestFilter> out = new ArrayList<RequestFilter>();
        for (RequestFilter window : windowList) {
            Instant windowStart = window.startTime;
            Instant windowEnd = window.endTime;
            for (RequestFilter rf : rfList) {
                Instant rfStart = rf.startTime;
                Instant rfEnd = rf.endTime;
                if ((rfStart.isAfter(windowStart) || rfStart.equals(windowStart))
                        && (rfEnd.isBefore(windowEnd) || rfEnd.equals(windowEnd))) {
                    // good, totally contained
                    out.add(rf);
                } else if (rfEnd.isBefore(windowStart) || rfEnd.equals(windowStart)) {
                    // bad, completely before window
                } else if (rfStart.isAfter(windowEnd) || rfStart.equals(windowEnd)) {
                    // bad, completely after window
                } else {
                    // some overlap
                    if (rfStart.isBefore(windowStart)) {
                        rfStart = windowStart;
                    }
                    if (rfEnd.isAfter(windowEnd)) {
                        rfEnd = windowEnd;
                    }
                    out.add(new RequestFilter(rf.channelId, rfStart, rfEnd));
                }
            }
        }
        return out;
    }

    /**
     * Unites all ranges in the given array into a single range if they're
     * contiguous or overlapping
     */
    public static TimeRange[] merge(TimeRange[] ranges) {
        return new MSTRMerger().merge(ranges);
    }
    
    public static List<TimeRange> mergeMicroSecondTimeRange(List<TimeRange> ranges) {
        return new MSTRMerger().merge(ranges);
    }

    /**
     * Unites all chunks in the given array into a single chunk if they're
     * contiguous or overlapping in time. Ignores the channels and samples per
     * second inside of the chunks, so they must be grouped according to that
     * before being merged
     */
    public static List<PlottableChunk> merge(List<PlottableChunk> chunks) {
        return new PlottableChunkMerger().merge(chunks);
    }
    
    public static RequestFilter cover(RequestFilter[] rf) {
        if (rf == null || rf.length == 0) { return null;}
        RFMerger rfm = new RFMerger();
        RequestFilter out = rf[0];
        for (int i = 1; i < rf.length; i++) {
            out = (RequestFilter)rfm.merge(out, rf[i]);
        }
        return out;
    }

    private static abstract class Merger {

        public abstract Object merge(Object one, Object two);

        public abstract boolean shouldMerge(Object one, Object two);

        public Object[] internalMerge(Object[] chunks,
                                      Object[] resultantTypeArray) {
            chunks = (Object[])chunks.clone();
            for(int i = 0; i < chunks.length; i++) {
                Object chunk = chunks[i];
                for(int j = i + 1; j < chunks.length; j++) {
                    Object chunk2 = chunks[j];
                    if(shouldMerge(chunk, chunk2)) {
                        chunks[j] = merge(chunk, chunk2);
                        chunks[i] = null;
                        break;
                    }
                }
            }
            List results = new ArrayList();
            for(int i = 0; i < chunks.length; i++) {
                if(chunks[i] != null) {
                    results.add(chunks[i]);
                }
            }
            return results.toArray(resultantTypeArray);
        }
    }

    private static class MSTRMerger extends Merger {

        public Object merge(Object one, Object two) {
            return new TimeRange(cast(one), cast(two));
        }

        public boolean shouldMerge(Object one, Object two) {
            TimeRange o = (TimeRange)one;
            TimeRange t = (TimeRange)two;
            if(o.getBeginTime().isBefore(t.getBeginTime())) {
                return !o.getEndTime().isBefore(t.getBeginTime());
            }
            return !t.getEndTime().isBefore(o.getBeginTime());
        }

        public TimeRange cast(Object o) {
            return (TimeRange)o;
        }

        public TimeRange[] merge(TimeRange[] ranges) {
            return (TimeRange[])internalMerge(ranges,
                                                         new TimeRange[0]);
        }
        
        public List<TimeRange> merge(List<TimeRange> chunks) {
            return Arrays.asList((TimeRange[])internalMerge(chunks.toArray(),
                                                   new TimeRange[0]));
        }
    }

    private static class RFMerger extends Merger {

        public Object merge(Object one, Object two) {
            RequestFilter orig = (RequestFilter)one;
            TimeRange tr = new TimeRange(toMSTR(one),
                                                               toMSTR(two));
            return new RequestFilter(orig.channelId, tr.getBeginTime(), tr.getEndTime());
        }

        protected String getChannelString(Object rf) {
            return ChannelIdUtil.toStringNoDates(((RequestFilter)rf).channelId);
        }

        public boolean shouldMerge(Object one, Object two) {
            return getChannelString(one).equals(getChannelString(two))
                    && (RangeTool.areOverlapping(toMSTR(one), toMSTR(two)) || RangeTool.areContiguous(toMSTR(one),
                                                                                                      toMSTR(two)));
        }

        protected TimeRange toMSTR(Object o) {
            return new TimeRange((RequestFilter)o);
        }

        public RequestFilter[] merge(RequestFilter[] ranges) {
            return (RequestFilter[])internalMerge(ranges, new RequestFilter[0]);
        }
    }

    public static class LSMerger extends Merger {

        public Object merge(Object one, Object two) {
            return merge((LocalSeismogramImpl)one, (LocalSeismogramImpl)two);
        }

        public LocalSeismogramImpl merge(LocalSeismogramImpl seis,
                                         LocalSeismogramImpl seis2) {
            TimeRange fullRange = new TimeRange(toMSTR(seis),
                                                                      toMSTR(seis2));
            if(fullRange.equals(toMSTR(seis))) {
                return seis;
            }
            LocalSeismogramImpl earlier = seis;
            LocalSeismogramImpl later = seis2;
            if(seis2.getBeginTime().isBefore(seis.getBeginTime())) {
                earlier = seis2;
                later = seis;
            }
            try {
                if(seis.is_encoded() && seis2.is_encoded()) {
                    EncodedData[] earlierED = earlier.get_as_encoded();
                    EncodedData[] laterED = later.get_as_encoded();
                    EncodedData[] outED = new EncodedData[earlierED.length
                            + laterED.length];
                    System.arraycopy(earlierED, 0, outED, 0, earlierED.length);
                    System.arraycopy(laterED,
                                     0,
                                     outED,
                                     earlierED.length,
                                     laterED.length);
                    TimeSeriesDataSel td = new TimeSeriesDataSel();
                    td.encoded_values(outED);
                    LocalSeismogramImpl newSeis = new LocalSeismogramImpl(earlier,
                                                                          td);
                    newSeis.num_points = seis.num_points + seis2.num_points;
                    return newSeis;
                }
                int numPoints = seis.getNumPoints() + seis2.getNumPoints();
                if(seis.can_convert_to_short() && seis2.can_convert_to_short()) {
                    short[] outS = new short[numPoints];
                    System.arraycopy(earlier.get_as_shorts(),
                                     0,
                                     outS,
                                     0,
                                     earlier.getNumPoints());
                    System.arraycopy(later.get_as_shorts(),
                                     0,
                                     outS,
                                     earlier.getNumPoints(),
                                     later.getNumPoints());
                    return new LocalSeismogramImpl(earlier, outS);
                } else if(seis.can_convert_to_long()
                        && seis2.can_convert_to_long()) {
                    int[] outI = new int[numPoints];
                    System.arraycopy(earlier.get_as_longs(),
                                     0,
                                     outI,
                                     0,
                                     earlier.getNumPoints());
                    System.arraycopy(later.get_as_longs(),
                                     0,
                                     outI,
                                     earlier.getNumPoints(),
                                     later.getNumPoints());
                    return new LocalSeismogramImpl(earlier, outI);
                } else if(seis.can_convert_to_float()
                        && seis2.can_convert_to_float()) {
                    float[] outF = new float[numPoints];
                    System.arraycopy(earlier.get_as_floats(),
                                     0,
                                     outF,
                                     0,
                                     earlier.getNumPoints());
                    System.arraycopy(later.get_as_floats(),
                                     0,
                                     outF,
                                     earlier.getNumPoints(),
                                     later.getNumPoints());
                    return new LocalSeismogramImpl(earlier, outF);
                } else {
                    double[] outD = new double[numPoints];
                    System.arraycopy(earlier.get_as_doubles(),
                                     0,
                                     outD,
                                     0,
                                     earlier.getNumPoints());
                    System.arraycopy(later.get_as_doubles(),
                                     0,
                                     outD,
                                     earlier.getNumPoints(),
                                     later.getNumPoints());
                    return new LocalSeismogramImpl(earlier, outD);
                }
            } catch(FissuresException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean shouldMerge(Object one, Object two) {
            return getChannelString(one).equals(getChannelString(two))
                    && (RangeTool.areContiguous((LocalSeismogramImpl)one,
                                                (LocalSeismogramImpl)two) || toMSTR(one).equals(toMSTR(two)));
        }

        protected String getChannelString(Object rf) {
            return ChannelIdUtil.toStringNoDates(((LocalSeismogramImpl)rf).channel_id);
        }

        protected TimeRange toMSTR(Object o) {
            return new TimeRange((LocalSeismogramImpl)o);
        }

        public LocalSeismogramImpl[] merge(LocalSeismogramImpl[] ranges) {
            return (LocalSeismogramImpl[])internalMerge(ranges,
                                                        new LocalSeismogramImpl[0]);
        }
    }

    private static class PlottableChunkMerger extends Merger {

        public Object merge(Object one, Object two) {
            PlottableChunk chunk = cast(one);
            PlottableChunk chunk2 = cast(two);
            TimeRange fullRange = new TimeRange(chunk.getTimeRange(),
                                                                      chunk2.getTimeRange());
            int samples = (int)Math.floor(chunk.getPixelsPerDay() * 2
                    * TimeUtils.durationToFloatDays(fullRange.getInterval()));
            int[] y = new int[samples];
            fill(fullRange, y, chunk);
            fill(fullRange, y, chunk2);
            Plottable mergedData = new Plottable(null, y);
            PlottableChunk earlier = chunk;
            if(chunk2.getBeginTime().isBefore(chunk.getBeginTime())) {
                earlier = chunk2;
            }
            return new PlottableChunk(mergedData,
                                      earlier.getBeginPixel(),
                                      earlier.getJDay(),
                                      earlier.getYear(),
                                      chunk.getPixelsPerDay(),
                                      chunk.getNetworkCode(),
                                      chunk.getStationCode(),
                                      chunk.getSiteCode(),
                                      chunk.getChannelCode());
        }

        public boolean shouldMerge(Object one, Object two) {
            return RangeTool.areContiguous(cast(one), cast(two))
                    || RangeTool.areOverlapping(cast(one), cast(two));
        }

        private PlottableChunk cast(Object o) {
            return (PlottableChunk)o;
        }

        public List<PlottableChunk> merge(List<PlottableChunk> chunks) {
            return Arrays.asList((PlottableChunk[])internalMerge(chunks.toArray(),
                                                   new PlottableChunk[0]));
        }

        public static int[] fill(TimeRange fullRange,
                                 int[] y,
                                 PlottableChunk chunk) {
            Instant rowBeginTime = chunk.getBeginTime();
            int offsetIntoRequestSamples = SimplePlotUtil.getPixel(y.length / 2,
                                                                   fullRange,
                                                                   rowBeginTime) * 2;
            int[] dataY = chunk.getData().y_coor;
            int numSamples = dataY.length;
            int firstSampleForRequest = 0;
            if(offsetIntoRequestSamples < 0) {
                firstSampleForRequest = -1 * offsetIntoRequestSamples;
            }
            int lastSampleForRequest = numSamples;
            if(offsetIntoRequestSamples + numSamples > y.length) {
                lastSampleForRequest = y.length - offsetIntoRequestSamples;
            }
            for(int i = firstSampleForRequest; i < lastSampleForRequest; i++) {
                y[i + offsetIntoRequestSamples] = dataY[i];
            }
            return y;
        }
    }

    public static boolean equalsOrAfter(Instant first,
                                        Instant second) {
        return first.equals(second) || first.isAfter(second);
    }

    public static boolean equalsOrBefore(Instant first,
                                         Instant second) {
        return first.equals(second) || first.isBefore(second);
    }

}