package edu.sc.seis.sod.process.waveform;

import java.io.File;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.ConfigurationException;

public class SacWriter extends AbstractSeismogramWriter {

    private static final String DEFAULT_FILE_TEMPLATE = "Event_${event.getTime('yyyy_MM_dd_HH_mm_SS')}/${channel.codes}.sac";

    public SacWriter(Element el) throws ConfigurationException {
        this(extractWorkingDir(el),
             extractFileTemplate(el, DEFAULT_FILE_TEMPLATE),
             extractPrefix(el),
             extractProcessors(el));
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
        this(workingDir, fileTemplate, DEFAULT_PREFIX, new SacProcess[0]);
    }

    public SacWriter(SacProcess[] processes) throws ConfigurationException {
        this(DEFAULT_WORKING_DIR, DEFAULT_FILE_TEMPLATE, DEFAULT_PREFIX, processes);
    }

    public SacWriter(String workingDir, String fileTemplate, String prefix, SacProcess[] processes) throws ConfigurationException {
        super(workingDir, fileTemplate, prefix);
        this.processors = processes;
    }

    public void write(String location,
                      LocalSeismogramImpl seis,
                      Channel chan,
                      EventAccessOperations ev) throws Exception {
        SacTimeSeries writer = FissuresToSac.getSAC(seis,
                                                    chan,
                                                    EventUtil.extractOrigin(ev));
        applyProcessors(writer, ev, chan);
        File f = new File(location);
        writer.write(f);
        SaveSeismogramToFile.addBytesWritten(f.length());
    }

    public void applyProcessors(SacTimeSeries writer,
                                EventAccessOperations ev,
                                Channel chan) {
        for(int i = 0; i < processors.length; i++) {
            processors[i].process(writer, ev, chan);
        }
    }

    private SacProcess[] processors;
}
