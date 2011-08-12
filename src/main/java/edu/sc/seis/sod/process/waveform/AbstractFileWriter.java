package edu.sc.seis.sod.process.waveform;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;

import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.subsetter.VelocityFileElementParser;
import edu.sc.seis.sod.velocity.ContextWrangler;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public abstract class AbstractFileWriter {

    public AbstractFileWriter(String workingDir, String fileTemplate, String prefix) {
        this.workingDir = workingDir;
        this.origTemplate = fileTemplate;
        this.prefix = prefix;

        if(!INDEX_VAR.matcher(fileTemplate).matches()) {
            fileTemplate += "${index}";
        }
        VelocityFileElementParser parser = new VelocityFileElementParser(workingDir, fileTemplate);
        this.template = parser.getTemplate();
        this.prefix = prefix;
    }

    public String generate(CacheEvent event,
                           ChannelImpl channel,
                           LocalSeismogramImpl representativeSeismogram,
                           int index) {
        VelocityContext ctx = ContextWrangler.createContext(event);
        if(index > 0) {
            ctx.put("index", "." + index);
        } else {
            ctx.put("index", "");
        }
        ctx.put("prefix", prefix);
        ContextWrangler.insertIntoContext(representativeSeismogram, channel, ctx);
        return FissuresFormatter.filize(velocitizer.evaluate(template, ctx));
    }

    public String generate(CacheEvent event,
                           ChannelGroup channelGroup,
                           int index,
                           Map<String, Object> extras) {
        VelocityContext ctx = ContextWrangler.createContext(event);
        if(index > 0) {
            ctx.put("index", "." + index);
        } else {
            ctx.put("index", "");
        }
        ctx.put("prefix", prefix);
        ContextWrangler.insertIntoContext(channelGroup, ctx);
        for (String key : extras.keySet()) {
            ctx.put(key, extras.get(key));
        }
        return FissuresFormatter.filize(velocitizer.evaluate(template, ctx));
    }

    public void removeExisting(CacheEvent event,
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
    
    public void removeExisting(CacheEvent event,
                               ChannelGroup channelGroup,
                               Map<String, Object> extras) {
        for(int i = 0; true; i++) {
            File cur = new File(generate(event, channelGroup, i, extras));
            if(!cur.exists()) {
                break;
            }
            cur.delete();
        }
    }
            
    public static final String DEFAULT_FILE_TEMPLATE_WO_EXT = "Event_${event.getTime('yyyy_MM_dd_HH_mm_ss')}/${prefix}${channel.codes}${index}";
    protected static final String DEFAULT_PREFIX = "";
    public static final String DEFAULT_WORKING_DIR = "seismograms/";
    
    protected String workingDir;
    protected String origTemplate;
    protected String template;
    protected String prefix;
    protected SimpleVelocitizer velocitizer = new SimpleVelocitizer();

    public String getTemplate() {
        return template;
    }

    protected static String extractFileTemplate(Element el, String def) {
        return DOMHelper.extractText(el, "location", def);
    }

    protected static String extractPrefix(Element el) {
        return DOMHelper.extractText(el, "prefix", DEFAULT_PREFIX);
    }

    public static String extractWorkingDir(Element el) {
        return DOMHelper.extractText(el, "workingDir", DEFAULT_WORKING_DIR, true);
    }

    protected static final Pattern INDEX_VAR = Pattern.compile(".*\\$\\{?index\\}?.*");

    public AbstractFileWriter() {
        super();
    }
}