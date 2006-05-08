package edu.sc.seis.sod.process.waveform;

import java.io.File;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import edu.sc.seis.sod.ConfigurationException;

public class MseedWriter extends AbstractSeismogramWriter {

    private static final String DEFAULT_FILE_TEMPLATE = "Event_${event.getTime('yyyy_MM_dd_HH_mm_SS')}/${channel.codes}.mseed";

    public MseedWriter(Element el) throws ConfigurationException {
        super(extractWorkingDir(el),
              extractFileTemplate(el, DEFAULT_FILE_TEMPLATE),
              extractPrefix(el));
    }

    public void write(String loc,
                      LocalSeismogramImpl seis,
                      Channel chan,
                      EventAccessOperations ev) throws Exception {
        SaveSeismogramToFile.addBytesWritten(URLDataSetSeismogram.writeMSeed(seis,
                                                                             new File(loc))
                .length());
    }
}
