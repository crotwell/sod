package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.Property;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class SNEPProblem implements WaveformProcess {

    public SNEPProblem() {
        this((String)null);
    }

    public SNEPProblem(String type) {
        this.type = type;
    }

    public SNEPProblem(Element el) throws ConfigurationException {
        Element typeEl = SodUtil.getElement(el, "type");
        if(typeEl != null) {
            type = SodUtil.getText(typeEl);
        }
    }

    public WaveformResult process(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        String name = "snep.problem";
        for(int i = 0; i < seismograms.length; i++) {
            Property[] properties = seismograms[i].properties;
            for(int j = 0; j < properties.length; j++) {
                if(properties[j].name.equals(name)) {
                    if(type == null
                            || (type != null && properties[j].value.equals(type))) {
                        return new WaveformResult(seismograms,
                                                  new StringTreeLeaf(this, true));
                    }
                }
            }
        }
        return new WaveformResult(seismograms, new StringTreeLeaf(this, false));
    }

    private String type;
}
