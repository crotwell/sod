/**
 * Taper.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

public class Taper implements LocalSeismogramProcess {
    public Taper (Element config) {
        Element widthElement = SodUtil.getElement(config, "width");
        if (widthElement != null) {
            float width = Float.parseFloat(SodUtil.getText(widthElement));
            taper = new edu.sc.seis.fissuresUtil.bag.Taper(width);
        } else {
            taper = new edu.sc.seis.fissuresUtil.bag.Taper();
        }
    }

    public LocalSeismogramImpl[] process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar
                                        ) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for (int i=0; i<seismograms.length; i++) {
            out[i] = taper.apply(seismograms[i]);
        } // end of for (int i=0; i<seismograms.length; i++)
        return out;
    }

    edu.sc.seis.fissuresUtil.bag.Taper taper;
}

