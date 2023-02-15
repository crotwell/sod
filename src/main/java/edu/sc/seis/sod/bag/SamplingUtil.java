package edu.sc.seis.sod.bag;

import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.station.SamplingRangeImpl;


/**
 * @author groves
 * Created on Oct 7, 2004
 */
public class SamplingUtil {


    public static List<Channel> inSampling(SamplingRangeImpl sampling, List<Channel> chans) {
        double minSPS = getSamplesPerSecond(sampling.min);
        double maxSPS = getSamplesPerSecond(sampling.max);
        List results = new ArrayList();
        for(Channel chan : chans) {
            double chanSPS = chan.getSampleRate().getValue();
            if(minSPS <= chanSPS && chanSPS <= maxSPS) {
                results.add(chan);
            }
        }
        return results;
        
    }

    private static double getSamplesPerSecond(SamplingImpl sampling) {
        return sampling.getFrequency().getValue(UnitImpl.HERTZ);
    }
}
