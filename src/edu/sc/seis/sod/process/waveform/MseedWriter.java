package edu.sc.seis.sod.process.waveform;

import java.io.File;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;

public class MseedWriter extends AbstractSeismogramWriter {

    private static final String DEFAULT_FILE_TEMPLATE = "seismograms/${event.filizedTime}/${network.code}/${station.code}/${site.code}.${channel.code}.mseed";

    public MseedWriter(Element el) {
        super(extractFileTemplate(el, DEFAULT_FILE_TEMPLATE));
    }

    public void write(String loc,
                      LocalSeismogramImpl seis,
                      Channel chan,
                      EventAccessOperations ev) throws Exception {
        URLDataSetSeismogram.writeMSeed(seis, new File(loc));
    }
}
