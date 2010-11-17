package edu.sc.seis.sod.process.waveform;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.hibernate.SeismogramFileRefDB;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.VelocityFileElementParser;
import edu.sc.seis.sod.velocity.ContextWrangler;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public abstract class AbstractSeismogramWriter implements WaveformProcess {

    public static final String DEFAULT_FILE_TEMPLATE_WO_EXT = "Event_${event.getTime('yyyy_MM_dd_HH_mm_ss')}/${channel.codes}${index}";
 
    protected static final String DEFAULT_PREFIX = "";

    public static final String DEFAULT_WORKING_DIR = "seismograms/";

    private String template, prefix;
    
    protected boolean storeSeismogramsInDB = false;

    public AbstractSeismogramWriter(String workingDir, String fileTemplate, String prefix, boolean storeSeismogramsInDB)
            throws ConfigurationException {
        if(!INDEX_VAR.matcher(fileTemplate).matches()) {
            fileTemplate += "${index}";
        }
        VelocityFileElementParser parser = new VelocityFileElementParser(workingDir, fileTemplate);
        this.template = parser.getTemplate();
        this.prefix = prefix;
        this.storeSeismogramsInDB = storeSeismogramsInDB;
        new PrintlineVelocitizer(new String[] {fileTemplate});
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
        ContextWrangler.insertIntoContext(representativeSeismogram, channel, ctx);
        return FissuresFormatter.filize(velocitizer.evaluate(template, ctx));
    }

    public WaveformResult accept(CacheEvent event,
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
                if (storeSeismogramsInDB) {
                    SeismogramFileRefDB.getSingleton().saveSeismogramToDatabase(event, channel, seismograms[i], loc, getFileType());
                }
                cookieJar.put(AbstractSeismogramWriter.getCookieName(prefix, channel.get_id(), i), loc);
            }
        }
        return new WaveformResult(true, seismograms, this);
    }
    
    public abstract SeismogramFileTypes getFileType();
    
    public String getTemplate(){
        return template;
    }

    public abstract void write(String loc,
                               LocalSeismogramImpl seis,
                               ChannelImpl chan,
                               CacheEvent ev) throws Exception;

    public static void addBytesWritten(long bytes) {
        bytesWritten += bytes;
    }

    public static long getBytesWritten() {
        return bytesWritten;
    }

    public static String getCookieName(String prefix, ChannelId channel, int i) {
        return AbstractSeismogramWriter.COOKIE_PREFIX + prefix + ChannelIdUtil.toString(channel) + "_"
                + i;
    }

    private SimpleVelocitizer velocitizer = new SimpleVelocitizer();

    public static final String SVN_PARAM = PhaseSignalToNoise.PHASE_STON_PREFIX
    + "ttp";

    static long bytesWritten = 0;

    public static final String COOKIE_PREFIX = "SeisFile_";

    protected static String extractFileTemplate(Element el, String def) {
        return DOMHelper.extractText(el, "location", def);
    }

    protected static String extractPrefix(Element el) {
        return DOMHelper.extractText(el, "prefix", DEFAULT_PREFIX);
    }

    public static String extractWorkingDir(Element el) {
        return DOMHelper.extractText(el, "workingDir", DEFAULT_WORKING_DIR, true);
    }

    private static final Pattern INDEX_VAR = Pattern.compile(".*\\$\\{?index\\}?.*");
}
