package edu.sc.seis.sod.process.waveform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.SeismogramFileTypes;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.util.convert.sac.FissuresToSac;
import edu.sc.seis.sod.util.display.EventUtil;

public class SacWriter extends AbstractSeismogramWriter {

    public static final String DEFAULT_FILE_TEMPLATE = DEFAULT_FILE_TEMPLATE_WO_EXT+".sac";

    public SacWriter(Element el) throws ConfigurationException {
        this(extractWorkingDir(el),
             extractFileTemplate(el, DEFAULT_FILE_TEMPLATE),
             extractPrefix(el),
             extractProcessors(el),
             DOMHelper.hasElement(el, "storeSeismogramsInDB"),
             DOMHelper.hasElement(el, "littleEndian"));
    }

    private static List<SacProcess> extractProcessors(Element el) {
        NodeList nl = DOMHelper.getElements(el, "phaseTimeHeader");
        ArrayList<SacProcess> out = new ArrayList<SacProcess>();
        for(int i = 0; i < nl.getLength(); i++) {
            out.add( new PhaseHeaderProcess((Element)nl.item(i)));
        }
        Element sacScript = SodUtil.getElement(el, "sacHeaderScript");
        if (sacScript != null) {
            out.add( new SacHeaderScript(sacScript));
        }
        return out;
    }

    public SacWriter() throws ConfigurationException {
        this(DEFAULT_FILE_TEMPLATE);
    }

    public SacWriter(String workingDir) throws ConfigurationException {
        this(workingDir, DEFAULT_FILE_TEMPLATE);
    }

    public SacWriter(String workingDir, String fileTemplate) throws ConfigurationException {
        this(workingDir, fileTemplate, DEFAULT_PREFIX, new ArrayList<SacProcess>(), false, false);
    }

    public SacWriter(List<SacProcess> processes) throws ConfigurationException {
        this(DEFAULT_WORKING_DIR, DEFAULT_FILE_TEMPLATE, DEFAULT_PREFIX, processes, false, false);
    }

    public SacWriter(String workingDir, String fileTemplate, String prefix, List<SacProcess> processes, boolean storeSeismogramsInDB, boolean littleEndian) throws ConfigurationException {
        super(workingDir, fileTemplate, prefix, storeSeismogramsInDB);
        this.processors = processes;
        this.littleEndian=littleEndian;
    }

    public void write(String location,
                      LocalSeismogramImpl seis,
                      ChannelImpl chan,
                      CacheEvent ev) throws Exception {
        SacTimeSeries writer = FissuresToSac.getSAC(seis,
                                                    chan,
                                                    EventUtil.extractOrigin(ev));
        applyProcessors(writer, ev, chan);
        if (littleEndian) {
            writer.getHeader().setLittleEndian();
        }
        File f = new File(location);
        writer.write(f);
        AbstractSeismogramWriter.addBytesWritten(f.length());
    }

    public void applyProcessors(SacTimeSeries writer,
                                CacheEvent ev,
                                ChannelImpl chan) throws Exception {
        for (SacProcess processor : processors) {
            processor.process(writer, ev, chan);
        }
    }


    public SeismogramFileTypes getFileType() {
        return SeismogramFileTypes.SAC;
    }
    
    boolean littleEndian;

    private List<SacProcess> processors = new ArrayList<SacProcess>();
}
