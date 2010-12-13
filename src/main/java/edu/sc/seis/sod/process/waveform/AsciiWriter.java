package edu.sc.seis.sod.process.waveform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.sod.ConfigurationException;


public class AsciiWriter extends AbstractSeismogramWriter {

    public static final String DEFAULT_FILE_TEMPLATE = DEFAULT_FILE_TEMPLATE_WO_EXT+".txt";

    public AsciiWriter(Element el) throws ConfigurationException {
        this(extractWorkingDir(el),
             extractFileTemplate(el, DEFAULT_FILE_TEMPLATE),
             extractPrefix(el),
             DOMHelper.hasElement(el, "storeSeismogramsInDB"));
    }
    
    public AsciiWriter(String workingDir, String fileTemplate, String prefix, boolean storeSeismogramsInDB)
            throws ConfigurationException {
        super(workingDir, fileTemplate, prefix, storeSeismogramsInDB);
        // TODO Auto-generated constructor stub
    }

    @Override
    public SeismogramFileTypes getFileType() {
        return SeismogramFileTypes.SIMPLE_ASCII;
    }

    @Override
    public void write(String location, LocalSeismogramImpl seis, ChannelImpl chan, CacheEvent ev) throws Exception {
        File f = new File(location);
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        writer.println("# "+ChannelIdUtil.toStringNoDates(chan.getId())+" "+seis.getNumPoints()+" "+seis.getSampling().getPeriod()+" "+seis.getBeginTime()+" "+seis.getUnit());
        if(seis.can_convert_to_long()) {
            int[] asInts = seis.get_as_longs();
            for(int i = 0; i < asInts.length; i++) {
                writer.println(asInts[i]);
            }
        } else if(seis.can_convert_to_float()) {
            float[] asFloats = seis.get_as_floats();
            for(int i = 0; i < asFloats.length; i++) {
                writer.println(asFloats[i]);
            }
        }
        writer.close();
        AbstractSeismogramWriter.addBytesWritten(f.length());
    }
}
