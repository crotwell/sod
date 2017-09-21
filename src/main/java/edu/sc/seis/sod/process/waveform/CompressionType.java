package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.EncodedData;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

public class CompressionType implements WaveformProcess {

    public CompressionType(Element el) {
        NodeList nl = el.getElementsByTagName("type");
        types = new int[nl.getLength()];
        for(int i = 0; i < nl.getLength(); i++) {
            types[i] = DOMHelper.extractInt((Element)nl.item(i), ".", -1);
        }
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
        for(int s = 0; s < seismograms.length; s++) {
            if(seismograms[s].is_encoded()) {
                EncodedData[] edata = seismograms[s].get_as_encoded();
                for(int i = 0; i < edata.length; i++) {
                    boolean found = false;
                    for(int t = 0; t < types.length; t++) {
                        if(edata[i].compression == types[t]) {
                            found = true;
                            break;
                        }
                    }
                    if (! found) {
                        return new WaveformResult(false, seismograms, this, "type "+edata[i].compression+" not accepted");
                    }
                }
            }
        }
        return new WaveformResult(true, seismograms, this);
    }

    int[] types;
}
