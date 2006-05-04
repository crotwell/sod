package edu.sc.seis.sod.process.waveform;

import java.io.File;
import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.ContextWrangler;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public abstract class AbstractSeismogramWriter implements WaveformProcess {

    protected static final String DEFAULT_PREFIX = "";

    protected static final String DEFAULT_WORKING_DIR = "seismograms/";

    private String workingDir, fileTemplate, prefix;

    public AbstractSeismogramWriter(String workingDir,
                                    String fileTemplate,
                                    String prefix) throws ConfigurationException {
        this.workingDir = workingDir;
        if(!this.workingDir.endsWith(File.separator)){
            this.workingDir += File.separator;
        }
        this.fileTemplate = fileTemplate;
        if(fileTemplate.startsWith(File.separator)){
            this.fileTemplate = fileTemplate.substring(1);
        }
        this.prefix = prefix;
        new PrintlineVelocitizer(new String[] {fileTemplate});
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
        return FissuresFormatter.filize(workingDir
                + velocitizer.evaluate(fileTemplate, ctx));
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
                File parent = new File(locs[i]).getParentFile();
                if(!parent.exists() && !parent.mkdirs()) {
                    StringTreeLeaf reason = new StringTreeLeaf(this,
                                                               false,
                                                               "Unable to create directory "
                                                                       + parent);
                    return new WaveformResult(seismograms, reason);
                }
                write(locs[i], seismograms[i], channel, event);
                cookieJar.put(SaveSeismogramToFile.getCookieName(prefix,
                                                                 channel.get_id(),
                                                                 i),
                              locs[i]);
            }
        }
        return new WaveformResult(true, seismograms, this);
    }

    public abstract void write(String loc,
                               LocalSeismogramImpl seis,
                               Channel chan,
                               EventAccessOperations ev) throws Exception;

    private SimpleVelocitizer velocitizer = new SimpleVelocitizer();

    protected static String extractFileTemplate(Element el, String def) {
        return DOMHelper.extractText(el, "location", def);
    }

    protected static String extractPrefix(Element el) {
        return DOMHelper.extractText(el, "prefix", DEFAULT_PREFIX);
    }

    protected static String extractWorkingDir(Element el) {
        return DOMHelper.extractText(el, "workingDir", DEFAULT_WORKING_DIR);
    }
}
