package edu.sc.seis.sod.process.waveform;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.sound.FissuresToWAV;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.SeismogramFileTypes;
import edu.sc.seis.sod.model.common.MicroSecondTimeRange;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.station.ChannelImpl;


public class WavWriter extends AbstractSeismogramWriter {

    public WavWriter(String workingDir, String fileTemplate, String prefix, int speedup) throws ConfigurationException {
        super(workingDir, fileTemplate, prefix, false);
        this.speedup = speedup;
    }

    public WavWriter(Element el) throws ConfigurationException {
        this(extractWorkingDir(el),
             extractFileTemplate(el, DEFAULT_FILE_TEMPLATE),
             extractPrefix(el),
             SodUtil.loadInt(el, SPEEDUP, 2000));
    }
    

    public static final String DEFAULT_FILE_TEMPLATE = DEFAULT_FILE_TEMPLATE_WO_EXT+".wav";


    @Override
    public SeismogramFileTypes getFileType() {
        // not used as not saved in db
        return null;
    }

    @Override
    public void write(String loc, LocalSeismogramImpl seis, ChannelImpl chan, CacheEvent ev) throws Exception {
        File f = new File(loc);
        FissuresToWAV fisToWAV = new FissuresToWAV(seis, speedup);
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
        fisToWAV.writeWAV(out, new MicroSecondTimeRange(seis));
        out.close();
    }
    
    int speedup = 2000;
    
    public static final String SPEEDUP = "speedup";
}
