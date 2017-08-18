package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.SeismogramFileRefDB;
import edu.sc.seis.sod.hibernate.SeismogramFileTypes;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.measure.ListMeasurement;
import edu.sc.seis.sod.measure.Measurement;
import edu.sc.seis.sod.measure.TimeRangeMeasurement;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.status.StringTree;
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
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        if (cookieJar == null) {throw new NullPointerException("CookieJar cannot be null");}
        if (channel == null) {throw new NullPointerException("Channel cannot be null");}
        List<Measurement> reqList = new ArrayList<Measurement>();
        for (int i = 0; i < original.length; i++) {
            reqList.add(new TimeRangeMeasurement("request"+i, 
                                                 new MicroSecondDate(original[i].start_time), 
                                                 new MicroSecondDate(original[i].end_time)));
        }
        ListMeasurement requestList = new ListMeasurement("request", reqList);
        cookieJar.put("request", requestList);
        if(seismograms.length > 0) {
            removeExisting(event, channel, seismograms[0], seismograms.length);
            for(int i = 0; i < seismograms.length; i++) {
                String loc = generate(event, channel, seismograms[i], i, seismograms.length);
                StringTree mkdirResult = checkParentDirs(loc);
                if (! mkdirResult.isSuccess()) {
                    return new WaveformResult(seismograms, mkdirResult);
                }
                write(loc, seismograms[i], channel, event);
                if (storeSeismogramsInDB) {
                    SeismogramFileRefDB.getSingleton().saveSeismogramToDatabase(event, channel, seismograms[i], loc, getFileType());
                }
                cookieJar.put(AbstractSeismogramWriter.getCookieName(prefix, channel, i), loc);
            }
        }
        return new WaveformResult(true, seismograms, this);
    }

    public abstract SeismogramFileTypes getFileType();
    
    public abstract void write(String loc,
                               LocalSeismogramImpl seis,
                               Channel chan,
                               CacheEvent ev) throws Exception;

    public static void addBytesWritten(long bytes) {
        bytesWritten += bytes;
    }

    public static long getBytesWritten() {
        return bytesWritten;
    }

    public static String getCookieName(String prefix, Channel channel, int i) {
        return AbstractSeismogramWriter.COOKIE_PREFIX + prefix + ChannelIdUtil.toString(channel) + "_"
                + i;
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
