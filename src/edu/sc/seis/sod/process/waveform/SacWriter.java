package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

public class SacWriter extends AbstractSeismogramWriter {

    private static final String DEFAULT_FILE_TEMPLATE = "seismograms/${event.filizedTime}/${network.code}/${station.code}/${site.code}.${channel.code}.sac";

    public SacWriter(Element el) {
        this(extractFileTemplate(el, DEFAULT_FILE_TEMPLATE),
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

    public SacWriter() {
        this(DEFAULT_FILE_TEMPLATE);
    }

    public SacWriter(String fileTemplate) {
        this(fileTemplate, new SacProcess[0]);
    }

    public SacWriter(SacProcess[] processes) {
        this(DEFAULT_FILE_TEMPLATE, processes);
    }

    public SacWriter(String fileTemplate, SacProcess[] processes) {
        super(fileTemplate);
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
        writer.write(location);
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
