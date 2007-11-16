/**
 * ThreeComponentData.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.process.waveform.vector.WaveformVectorProcess;
import edu.sc.seis.sod.process.waveform.vector.WaveformVectorResult;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class ThreeComponentData implements WaveformVectorProcess {

    public WaveformVectorResult process(CacheEvent event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) {
        for (int i = 0; i < seismograms.length; i++) {
            if (seismograms[i].length == 0) {
                return new WaveformVectorResult(seismograms,
                                                new StringTreeLeaf(this,
                                                                   false,
                                                                   "seismograms["+i+"] is empty"));
            }
            
            if (seismograms[i][0].num_points == 0) {
                return new WaveformVectorResult(seismograms,
                                                new StringTreeLeaf(this,
                                                                   false,
                                                                   "seismograms["+i+"][0] has zero data points"));
            }
        }
        return new WaveformVectorResult(seismograms,
                                        new StringTreeLeaf(this,
                                                           true));
    }

}

