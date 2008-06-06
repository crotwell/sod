package edu.sc.seis.sod.process.waveform;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.VelocityFileElementParser;
import edu.sc.seis.sod.velocity.ContextWrangler;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public abstract class AbstractSeismogramWriter implements WaveformProcess {

    protected static final String DEFAULT_PREFIX = "";

    public static final String DEFAULT_WORKING_DIR = "seismograms/";

    private String template, prefix;

    public AbstractSeismogramWriter(String workingDir, String fileTemplate, String prefix)
            throws ConfigurationException {
        if(!INDEX_VAR.matcher(fileTemplate).matches()) {
            fileTemplate += "${index}";
        }
        VelocityFileElementParser parser = new VelocityFileElementParser(workingDir, fileTemplate);
        this.template = parser.getTemplate();
        this.prefix = prefix;
        new PrintlineVelocitizer(new String[] {fileTemplate});
    }

    public void removeExisting(EventAccessOperations event,
                               ChannelImpl channel,
                               LocalSeismogramImpl representativeSeismogram) {
        for(int i = 0; true; i++) {
            File cur = new File(generate(event, channel, representativeSeismogram, i));
            if(!cur.exists()) {
                break;
            }
            cur.delete();
        }
    }

    public String generate(EventAccessOperations event,
                           ChannelImpl channel,
                           LocalSeismogramImpl representativeSeismogram,
                           int index) {
        VelocityContext ctx = ContextWrangler.createContext(event);
        if(index > 0) {
            ctx.put("index", "." + index);
        } else {
            ctx.put("index", "");
        }
        ContextWrangler.insertIntoContext(representativeSeismogram, channel, ctx);
        return FissuresFormatter.filize(velocitizer.evaluate(template, ctx));
    }

    public WaveformResult process(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        if (cookieJar == null) {throw new NullPointerException("CookieJar cannot be null");}
        if (channel == null) {throw new NullPointerException("Channel cannot be null");}
        if(seismograms.length > 0) {
            removeExisting(event, channel, seismograms[0]);
            for(int i = 0; i < seismograms.length; i++) {
                String loc = generate(event, channel, seismograms[i], i);
                File parent = new File(loc).getParentFile();
                if(!parent.exists() && !parent.mkdirs()) {
                    StringTreeLeaf reason = new StringTreeLeaf(this,
                                                               false,
                                                               "Unable to create directory "
                                                                       + parent);
                    return new WaveformResult(seismograms, reason);
                }
                write(loc, seismograms[i], channel, event);
                cookieJar.put(SaveSeismogramToFile.getCookieName(prefix, channel.get_id(), i), loc);
            }
        }
        return new WaveformResult(true, seismograms, this);
    }
    
    public String getTemplate(){
        return template;
    }

    public abstract void write(String loc,
                               LocalSeismogramImpl seis,
                               ChannelImpl chan,
                               EventAccessOperations ev) throws Exception;

    private SimpleVelocitizer velocitizer = new SimpleVelocitizer();

    protected static String extractFileTemplate(Element el, String def) {
        return DOMHelper.extractText(el, "location", def);
    }

    protected static String extractPrefix(Element el) {
        return DOMHelper.extractText(el, "prefix", DEFAULT_PREFIX);
    }

    protected static String extractWorkingDir(Element el) {
        return DOMHelper.extractText(el, "workingDir", DEFAULT_WORKING_DIR, true);
    }

    private static final Pattern INDEX_VAR = Pattern.compile(".*\\$\\{?index\\}?.*");
}
