package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import org.w3c.dom.Element;

/**
 * Removes the mean from the seismograms.
 *
 *
 * Created: Wed Nov  6 17:58:10 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: RMean.java 8857 2004-05-21 20:02:00Z crotwell $
 */

public class RMean implements LocalSeismogramProcess {
    public RMean (Element config) {
        rmean = new edu.sc.seis.fissuresUtil.bag.RMean();
    }

    /**
     * Removes the mean from the seismograms.
     */
    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar)
        throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for (int i=0; i<seismograms.length; i++) {
            out[i] = rmean.apply(seismograms[i]);
        } // end of for (int i=0; i<seismograms.length; i++)
        return new LocalSeismogramResult(true, out);
    }

    edu.sc.seis.fissuresUtil.bag.RMean rmean;
}// RMean
