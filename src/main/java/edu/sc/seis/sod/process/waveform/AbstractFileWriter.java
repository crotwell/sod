package edu.sc.seis.sod.process.waveform;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;

import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.VelocityFileElementParser;
import edu.sc.seis.sod.velocity.ContextWrangler;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;
import edu.sc.seis.sod.velocity.network.VelocityChannel;

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
                           int index,
                           int numSeismograms) {
        VelocityContext ctx = ContextWrangler.createContext(event);
        if(numSeismograms > 1) {
            ctx.put("index", "." + (index+1)); // names index base 1, array is base 0
        } else {
            ctx.put("index", "");
        }
        ctx.put("prefix", prefix);
        ContextWrangler.insertIntoContext(representativeSeismogram, channel, ctx);
        return FissuresFormatter.filize(velocitizer.evaluate(template, ctx));
    }

    public String generate(CacheEvent event,
                           ChannelImpl channel,
                           ChannelImpl otherChannel,
                           int index,
                           Map<String, Object> extras) {
        VelocityContext ctx = ContextWrangler.createContext(event);
        if(index > 0) {
            ctx.put("index", "." + index);
        } else {
            ctx.put("index", "");
        }
        ctx.put("prefix", prefix);
        ContextWrangler.insertIntoContext(channel, ctx);
        ctx.put("otherChannel", new VelocityChannel(otherChannel));
        for (String key : extras.keySet()) {
            ctx.put(key, extras.get(key));
        }
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
    
    public StringTree checkParentDirs(String filename) {
        File out = new File(filename);
        File parent = out.getParentFile();
        if(parent == null || (!parent.exists() && !parent.mkdirs())) {
            String msg = "Unable to create directory " + parent;
            if (parent == null) {
                msg = "Unable to create directory, File.getParentFile() returns null: " + out.getPath();
            }
            StringTreeLeaf reason = new StringTreeLeaf(this,
                                                       false,
                                                       msg);
            if (firstDirectoryCreationError) {
                // this is probably something the user wants to see, at least once
                firstDirectoryCreationError = false;
                System.err.println("WARNING: "+reason.toString());
            }
            return reason;
        }
        return new Pass(this);
        
    }

    public void removeExisting(CacheEvent event,
                               ChannelImpl channel,
                               LocalSeismogramImpl representativeSeismogram, int numSeismograms) {
        for(int i = 0; true; i++) {
            File cur = new File(generate(event, channel, representativeSeismogram, i, numSeismograms));
            if(!cur.exists()) {
                break;
            }
            cur.delete();
        }
    }
    
    public void removeExisting(CacheEvent event,
                               ChannelImpl channel,
                               ChannelImpl otherChannel,
                               Map<String, Object> extras) {
        for(int i = 0; true; i++) {
            File cur = new File(generate(event, channel, otherChannel, i, extras));
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
    public static final String DEFAULT_PREFIX = "";
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

    boolean firstDirectoryCreationError = true;
}