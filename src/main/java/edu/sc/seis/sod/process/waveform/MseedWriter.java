package edu.sc.seis.sod.process.waveform;

import java.io.File;

import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.hibernate.SeismogramFileTypes;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.station.ChannelImpl;

public class MseedWriter extends AbstractSeismogramWriter {

    public static final String DEFAULT_FILE_TEMPLATE = DEFAULT_FILE_TEMPLATE_WO_EXT+".mseed";

    public MseedWriter(Element el) throws ConfigurationException {
        super(extractWorkingDir(el),
              extractFileTemplate(el, DEFAULT_FILE_TEMPLATE),
              extractPrefix(el),
              DOMHelper.hasElement(el, "storeSeismogramsInDB"));
    }

    public void write(String loc,
                      LocalSeismogramImpl seis,
                      ChannelImpl chan,
                      CacheEvent ev) throws Exception {
        AbstractSeismogramWriter.addBytesWritten(URLDataSetSeismogram.writeMSeed(seis,
                                                                             new File(loc))
                .length());
    }

    public SeismogramFileTypes getFileType() {
        return SeismogramFileTypes.MSEED;
    }
    
    
}
