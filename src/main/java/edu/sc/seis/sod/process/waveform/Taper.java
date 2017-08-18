/**
 * Taper.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class Taper implements WaveformProcess, Threadable {
    public Taper (Element config) {
        int type = edu.sc.seis.sod.bag.Taper.HANNING;
        Element typeElement = SodUtil.getElement(config, "type");
        if (typeElement != null) {
            String typeStr = SodUtil.getText(typeElement);
            if (typeStr.equals("hanning")) {
                type = edu.sc.seis.sod.bag.Taper.HANNING;
            } else if (typeStr.equals("hamming")) {
                type = edu.sc.seis.sod.bag.Taper.HANNING;
            } else if (typeStr.equals("cosine")) {
                type = edu.sc.seis.sod.bag.Taper.COSINE;
            }
        }
        Element widthElement = SodUtil.getElement(config, "width");
        if (widthElement != null) {
            float width = Float.parseFloat(SodUtil.getText(widthElement));
            taper = new edu.sc.seis.sod.bag.Taper(type, width);
        } else {
            taper = new edu.sc.seis.sod.bag.Taper(type);
        }
    }

    public boolean isThreadSafe() {
        return true;
    }

    public WaveformResult accept(CacheEvent event,
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

    edu.sc.seis.sod.bag.Taper taper;
}

