package edu.sc.seis.sod.process.waveform;

import java.io.File;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.hibernate.SeismogramFileReference;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import edu.sc.seis.seisFile.mseed.Blockette1000;
import edu.sc.seis.sod.ConfigurationException;

public class MseedWriter extends AbstractSeismogramWriter {

    private static final String DEFAULT_FILE_TEMPLATE = "Event_${event.getTime('yyyy_DDD_HH_mm_ss')}/${channel.codes}${index}.mseed";

    public MseedWriter(Element el) throws ConfigurationException {
        super(extractWorkingDir(el),
              extractFileTemplate(el, DEFAULT_FILE_TEMPLATE),
              extractPrefix(el),
              DOMHelper.hasElement(el, "storeSeismogramsInDB"));
    }

    public void write(String loc,
                      LocalSeismogramImpl seis,
                      ChannelImpl chan,
                      EventAccessOperations ev) throws Exception {
        SaveSeismogramToFile.addBytesWritten(URLDataSetSeismogram.writeMSeed(seis,
                                                                             new File(loc))
                .length());
    }

    public SeismogramFileTypes getFileType() {
        return SeismogramFileTypes.MSEED;
    }
    
    
}
