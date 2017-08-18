package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.MicroSecondTimeRange;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.seismogram.TimeSeriesDataSel;
import edu.sc.seis.sod.util.time.ReduceTool;

public class GapFill extends Merge {

    public GapFill(Element config) {
        if (DOMHelper.hasElement(config, "zeroFill")) {
            filler = new ZeroFill();
        } else if (DOMHelper.hasElement(config, "linearFill")) {
            filler = new LinearFill();
        }
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        WaveformResult merged = super.accept(event, 
                                              channel, 
                                              original, 
                                              available, 
                                              ReduceTool.cutOverlap(seismograms), 
                                              cookieJar);
        List<LocalSeismogramImpl> sortedSeismograms = new ArrayList<LocalSeismogramImpl>();
        for (int i = 0; i < merged.getSeismograms().length; i++) {
            sortedSeismograms.add(merged.getSeismograms()[i]);
        }
        Collections.sort(sortedSeismograms, new Comparator<LocalSeismogramImpl>() {
            public int compare(LocalSeismogramImpl o1, LocalSeismogramImpl o2) {
                if (o1.getBeginTime().before(o2.getBeginTime())) {
                    return -1;
                } else if (o1.getBeginTime().after(o2.getBeginTime())) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        // assume seismograms are merged (no overlaps) and sorted, so pairwise gap filling is ok
        for (int i = 0; i < original.length; i++) {
            MicroSecondTimeRange mstr = new MicroSecondTimeRange(original[i]);
            sortedSeismograms = reduce(sortedSeismograms, mstr);
        }
        return new WaveformResult(true, sortedSeismograms.toArray(new LocalSeismogramImpl[0]), this);
    }
        
    public List<LocalSeismogramImpl> reduce(List<LocalSeismogramImpl> inList, MicroSecondTimeRange mstr) throws FissuresException {
        if (inList.size() == 0 || inList.size() == 1) {
            return inList;
        }
        LocalSeismogramImpl first = inList.remove(0);
        // reduce remainder 
        List<LocalSeismogramImpl> remaining = reduce(inList, mstr);
        LocalSeismogramImpl second = remaining.remove(0);
        List<LocalSeismogramImpl> outList = new LinkedList<LocalSeismogramImpl>();
        if (mstr.contains(first.getEndTime()) && mstr.contains(second.getBeginTime())) {
            // have overlap in a request window, so fill the gap
            LocalSeismogramImpl[] merged = gapFill(first, second);
            for (int i = 0; i < merged.length; i++) {
                outList.add(merged[i]);
            }
        } else {
            // gap not in window, do not fill
            outList.add(first);
            outList.add(second);
        }
        outList.addAll(remaining);
        return outList;
    }
    
    public LocalSeismogramImpl[] gapFill(LocalSeismogramImpl first, LocalSeismogramImpl second) throws FissuresException {
        TimeSeriesDataSel fillData = filler.fill(first, second);
        LocalSeismogramImpl fillSeis = new LocalSeismogramImpl(first.get_id()+"_gapFill",
                                                               first.getProperties(),
                                                               first.getEndTime().add(first.getSampling().getPeriod()),
                                                               calcNumGapPoints(first, second),
                                                               first.getSampling(),
                                                               first.getUnit(), first.getChannelID(),
                                                               first.getParameterRefs(), 
                                                               first.time_corrections, 
                                                               first.sample_rate_history, 
                                                               fillData);
        LocalSeismogramImpl[] merged = ReduceTool.merge(new LocalSeismogramImpl[] {first, fillSeis, second});
        return merged;
    }

    public static int calcNumGapPoints(LocalSeismogramImpl first, LocalSeismogramImpl second) {
        MicroSecondDate firstEnd = first.getEndTime();
        MicroSecondDate secondBegin = second.getBeginTime();
        
        QuantityImpl numSamplePeriods = secondBegin.subtract((TimeInterval)first.getSampling().getPeriod().multiplyBy(0.5)).subtract(firstEnd).divideBy(first.getSampling().getPeriod());
        return (int)Math.ceil(numSamplePeriods.getValue(UnitImpl.DIMENSIONLESS)) -1; // one less than ceiling
    }
    
    FillStyle filler;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GapFill.class);
}

abstract class FillStyle {

    public abstract TimeSeriesDataSel fill(LocalSeismogramImpl first, LocalSeismogramImpl second) throws FissuresException;

}

class ZeroFill extends FillStyle {

    public TimeSeriesDataSel fill(LocalSeismogramImpl first, LocalSeismogramImpl second) {
        TimeSeriesDataSel outHolder = new TimeSeriesDataSel();
        int numGapPoints = GapFill.calcNumGapPoints(first, second);
        if (first.can_convert_to_long()) {
            int[] out = new int[numGapPoints];
            for (int i = 0; i < out.length; i++) {
                out[i] = 0;
            }
            outHolder.int_values(out);
        } else if (first.can_convert_to_float()) {
            float[] out = new float[numGapPoints];
            for (int i = 0; i < out.length; i++) {
                out[i] = 0;
            }
            outHolder.flt_values(out);
        } else  {
            double[] out = new double[numGapPoints];
            for (int i = 0; i < out.length; i++) {
                out[i] = 0;
            }
            outHolder.dbl_values(out);
        }
        return outHolder;
    }
}

class LinearFill extends FillStyle {

    public TimeSeriesDataSel fill(LocalSeismogramImpl first, LocalSeismogramImpl second) throws FissuresException {
        TimeSeriesDataSel outHolder = new TimeSeriesDataSel();
        int numGapPoints = GapFill.calcNumGapPoints(first, second);
        if (first.can_convert_to_long()) {
            int firstEndSample = first.get_as_longs()[first.getNumPoints()-1];
            int secondBeginSample = second.get_as_longs()[0];
            int[] out = new int[numGapPoints];
            for (int i = 0; i < out.length; i++) {
                out[i] = (int)Math.round(firstEndSample + (secondBeginSample-firstEndSample)*((float)i+1)/(numGapPoints+1));
            }
            outHolder.int_values(out);
        } else if (first.can_convert_to_float()) {
            float firstEndSample = first.get_as_floats()[first.getNumPoints()-1];
            float secondBeginSample = second.get_as_floats()[0];
            float[] out = new float[numGapPoints];
            for (int i = 0; i < out.length; i++) {
                out[i] = firstEndSample + (secondBeginSample-firstEndSample)*((float)i+1)/(numGapPoints+1);
            }
            outHolder.flt_values(out);
        } else  {
            double firstEndSample = first.get_as_doubles()[first.getNumPoints()-1];
            double secondBeginSample = second.get_as_doubles()[0];
            double[] out = new double[numGapPoints];
            for (int i = 0; i < out.length; i++) {
                out[i] = firstEndSample + (secondBeginSample-firstEndSample)*((float)i+1)/(numGapPoints+1);
            }
            outHolder.dbl_values(out);
        }
        return outHolder;
    }
}
