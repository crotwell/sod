package edu.sc.seis.sod.process.waveform;

import java.io.File;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.ConfigurationException;

public class SacWriter extends AbstractSeismogramWriter {

    public static final String DEFAULT_FILE_TEMPLATE = "Event_${event.getTime('yyyy_DDD_HH_mm_ss')}/${channel.codes}${index}.sac";

    public SacWriter(Element el) throws ConfigurationException {
        this(extractWorkingDir(el),
             extractFileTemplate(el, DEFAULT_FILE_TEMPLATE),
             extractPrefix(el),
             extractProcessors(el),
             DOMHelper.hasElement(el, "littleEndian"));
    }

    private static SacProcess[] extractProcessors(Element el) {
        NodeList nl = DOMHelper.getElements(el, "phaseTimeHeader");
        SacProcess[] processes = new SacProcess[nl.getLength()];
        for(int i = 0; i < processes.length; i++) {
            processes[i] = new PhaseHeaderProcess((Element)nl.item(i));
        }
        return processes;
    }

    public SacWriter() throws ConfigurationException {
        this(DEFAULT_FILE_TEMPLATE);
    }

    public SacWriter(String workingDir) throws ConfigurationException {
        this(workingDir, DEFAULT_FILE_TEMPLATE);
    }

    public SacWriter(String workingDir, String fileTemplate) throws ConfigurationException {
        this(workingDir, fileTemplate, DEFAULT_PREFIX, new SacProcess[0], false);
    }

    public SacWriter(SacProcess[] processes) throws ConfigurationException {
        this(DEFAULT_WORKING_DIR, DEFAULT_FILE_TEMPLATE, DEFAULT_PREFIX, processes, false);
    }

    public SacWriter(String workingDir, String fileTemplate, String prefix, SacProcess[] processes, boolean littleEndian) throws ConfigurationException {
        super(workingDir, fileTemplate, prefix);
        this.processors = processes;
        this.littleEndian=littleEndian;
    }

    public void write(String location,
                      LocalSeismogramImpl seis,
                      ChannelImpl chan,
                      EventAccessOperations ev) throws Exception {
        SacTimeSeries writer = FissuresToSac.getSAC(seis,
                                                    chan,
                                                    EventUtil.extractOrigin(ev));
        applyProcessors(writer, ev, chan);
        if (littleEndian) {
            writer.setLittleEndian();
        }
        File f = new File(location);
        writer.write(f);
        SaveSeismogramToFile.addBytesWritten(f.length());
    }

    public void applyProcessors(SacTimeSeries writer,
                                EventAccessOperations ev,
                                ChannelImpl chan) {
        for(int i = 0; i < processors.length; i++) {
            processors[i].process(writer, ev, chan);
        }
    }
    
    boolean littleEndian;

    private SacProcess[] processors;
}
