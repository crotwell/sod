/**
 * Taper.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

public class Taper implements WaveformProcess {
    public Taper (Element config) {
        int type = edu.sc.seis.fissuresUtil.bag.Taper.HANNING;
        Element typeElement = SodUtil.getElement(config, "type");
        if (typeElement != null) {
            String typeStr = SodUtil.getText(typeElement);
            if (typeStr.equals("hanning")) {
                type = edu.sc.seis.fissuresUtil.bag.Taper.HANNING;
            } else if (typeStr.equals("hamming")) {
                type = edu.sc.seis.fissuresUtil.bag.Taper.HANNING;
            } else if (typeStr.equals("cosine")) {
                type = edu.sc.seis.fissuresUtil.bag.Taper.COSINE;
            }
        }
        Element widthElement = SodUtil.getElement(config, "width");
        if (widthElement != null) {
            float width = Float.parseFloat(SodUtil.getText(widthElement));
            taper = new edu.sc.seis.fissuresUtil.bag.Taper(type, width);
        } else {
            taper = new edu.sc.seis.fissuresUtil.bag.Taper(type);
        }
    }

    public WaveformResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar
                                        ) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for (int i=0; i<seismograms.length; i++) {
            out[i] = taper.apply(seismograms[i]);
        } // end of for (int i=0; i<seismograms.length; i++)
        return new WaveformResult(out, new StringTreeLeaf(this, true));
    }

    edu.sc.seis.fissuresUtil.bag.Taper taper;
}

