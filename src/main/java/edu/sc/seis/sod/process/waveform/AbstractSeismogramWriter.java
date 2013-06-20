package edu.sc.seis.sod.process.waveform;

import java.io.File;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.SeismogramFileRefDB;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

public abstract class AbstractSeismogramWriter extends 
AbstractFileWriter implements WaveformProcess {

    protected boolean storeSeismogramsInDB = false;

    public AbstractSeismogramWriter(String workingDir, String fileTemplate, String prefix, boolean storeSeismogramsInDB)
            throws ConfigurationException {
        super(workingDir, fileTemplate, prefix);
        this.storeSeismogramsInDB = storeSeismogramsInDB;
        new PrintlineVelocitizer(new String[] {fileTemplate});
    }

    protected AbstractSeismogramWriter() throws ConfigurationException {
        this("seismograms", DEFAULT_FILE_TEMPLATE_WO_EXT+".unknown", "", false);
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

    public static final String SVN_PARAM = PhaseSignalToNoise.PHASE_STON_PREFIX
    + "ttp";

    static long bytesWritten = 0;

    public static final String COOKIE_PREFIX = "SeisFile_";
}
