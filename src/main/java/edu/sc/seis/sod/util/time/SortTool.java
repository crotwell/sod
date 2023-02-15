package edu.sc.seis.sod.util.time;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.PlottableChunk;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

/**
 * @author groves Created on Oct 28, 2004
 */
public class SortTool {

    public static LocalSeismogramImpl[] byLengthAscending(LocalSeismogramImpl[] seis) {
        Arrays.sort(seis, new SeisSizeSorter());
        return seis;
    }

    /**
     * @return the seismograms in order of begin time
     */
    public static LocalSeismogramImpl[] byBeginTimeAscending(LocalSeismogramImpl[] seis) {
        Arrays.sort(seis, new SeisBeginSorter());
        return seis;
    }

    public static List<PlottableChunk> byBeginTimeAscending(List<PlottableChunk> pc) {
        Collections.sort(pc, new PCBeginSorter());
        return pc;
    }

    public static PlottableChunk[] byBeginTimeAscending(PlottableChunk[] pc) {
        Arrays.sort(pc, new PCBeginSorter());
        return pc;
    }

    public static RequestFilter[] byBeginTimeAscending(RequestFilter[] rf) {
        Arrays.sort(rf, new RFBeginSorter());
        return rf;
    }

    public static TimeRange[] byBeginTimeAscending(TimeRange[] ranges) {
        Arrays.sort(ranges, new MSTRBeginSorter());
        return ranges;
    }

    public static class SeisSizeSorter implements Comparator<LocalSeismogramImpl>  {

        @Override
        public int compare(LocalSeismogramImpl o1, LocalSeismogramImpl o2) {
            return o1.getTimeInterval().compareTo(o2.getTimeInterval());
        }
    }

    public static class AscendingTimeSorter implements Comparator<Instant> {

        @Override
        public int compare(Instant o1, Instant o2) {
            return o1.compareTo(o2);
        }
    }

    private static class SeisBeginSorter  implements Comparator<LocalSeismogramImpl> {

        @Override
        public int compare(LocalSeismogramImpl o1, LocalSeismogramImpl o2) {
            return o1.getBeginTime().compareTo(o2.getBeginTime());
        }
    }

    private static class PCBeginSorter implements Comparator<PlottableChunk> {


        @Override
        public int compare(PlottableChunk o1, PlottableChunk o2) {
            return o1.getBeginTime().compareTo(o2.getBeginTime());
        }
    }

    private static class RFBeginSorter  implements Comparator<RequestFilter> {

        @Override
        public int compare(RequestFilter o1, RequestFilter o2) {
            return o1.startTime.compareTo(o2.startTime);
        }
    }

    private static class MSTRBeginSorter implements Comparator<TimeRange> {

        @Override
        public int compare(TimeRange o1, TimeRange o2) {
            return o1.getBeginTime().compareTo(o2.getBeginTime());
        }
    }
}