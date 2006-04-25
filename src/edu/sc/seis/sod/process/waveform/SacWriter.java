package edu.sc.seis.sod.process.waveform;

import java.io.File;
import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.ContextWrangler;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public class SacWriter implements WaveformProcess {

    public SacWriter(Element el) {
        this(extractFileTemplate(el), extractProcessors(el));
    }

    private static String extractFileTemplate(Element el) {
        return DOMHelper.extractText(el, "template", DEFAULT_FILE_TEMPLATE);
    }

    private static SacProcess[] extractProcessors(Element el) {
        NodeList nl = DOMHelper.getElements(el, "phaseTimeHeader");
        SacProcess[] processes = new SacProcess[nl.getLength()];
        for(int i = 0; i < processes.length; i++) {
            processes[i] = new PhaseHeaderProcess((Element)nl.item(i));
        }
        return processes;
    }

    private static final String DEFAULT_FILE_TEMPLATE = "seismograms/${event.filizedTime}/${network.code}/${station.code}/${site.code}.${channel.code}.sac";

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
        this.fileTemplate = fileTemplate;
        this.processors = processes;
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        if(seismograms.length > 0) {
            String base = generateBase(event, channel, seismograms[0]);
            removeExisting(base);
            String[] locs = generateLocations(base, seismograms.length);
            for(int i = 0; i < locs.length; i++) {
                SacTimeSeries writer = FissuresToSac.getSAC(seismograms[i],
                                                            channel,
                                                            EventUtil.extractOrigin(event));
                applyProcessors(writer, event, channel);
                File parent = new File(locs[i]).getParentFile();
                if(!parent.exists() && !parent.mkdirs()) {
                    StringTreeLeaf reason = new StringTreeLeaf(this,
                                                               false,
                                                               "Unable to create directory "
                                                                       + parent);
                    return new WaveformResult(seismograms, reason);
                }
                writer.write(locs[i]);
            }
        }
        return new WaveformResult(true, seismograms, this);
    }

    public void applyProcessors(SacTimeSeries writer,
                                EventAccessOperations ev,
                                Channel chan) {
        for(int i = 0; i < processors.length; i++) {
            processors[i].process(writer, ev, chan);
        }
    }

    public void removeExisting(String base) {
        File baseFile = new File(base);
        int count = 1;
        while(baseFile.exists()) {
            baseFile.delete();
            baseFile = new File(generateFile(base, count++));
        }
    }

    public String[] generateLocations(String base, int length) {
        String[] locs = new String[length];
        for(int i = 0; i < locs.length; i++) {
            locs[i] = generateFile(base, i);
        }
        return locs;
    }

    private String generateFile(String base, int position) {
        if(position == 0) {
            return base;
        }
        return base + "." + position;
    }

    public String generateBase(EventAccessOperations event,
                               Channel channel,
                               LocalSeismogramImpl representativeSeismogram) {
        VelocityContext ctx = ContextWrangler.createContext(event);
        ContextWrangler.insertIntoContext(representativeSeismogram,
                                          channel,
                                          ctx);
        return FissuresFormatter.filizeWithDirectories(velocitizer.evaluate(fileTemplate,
                                                                            ctx));
    }

    private SimpleVelocitizer velocitizer = new SimpleVelocitizer();

    private String fileTemplate;

    private SacProcess[] processors;
}
