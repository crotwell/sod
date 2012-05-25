package edu.sc.seis.sod.process.waveform.vector;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.IncompatibleSeismograms;
import edu.sc.seis.fissuresUtil.bag.Rotate;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.measure.ListMeasurement;
import edu.sc.seis.sod.measure.Measurement;
import edu.sc.seis.sod.measure.ScalarMeasurement;
import edu.sc.seis.sod.measure.SeismogramMeasurement;
import edu.sc.seis.sod.process.waveform.AbstractSeismogramWriter;
import edu.sc.seis.sod.process.waveform.MseedWriter;
import edu.sc.seis.sod.process.waveform.SacWriter;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class IterDeconReceiverFunction extends AbstractWaveformVectorMeasure {

    public IterDeconReceiverFunction(Element config) throws ConfigurationException, TauModelException {
        super(config);
        parseIterDeconConfig(config);
        taup = TauPUtil.getTauPUtil(modelName);
        decon = new IterDecon(maxBumps, true, tol, gwidth);
    }

    public void parseIterDeconConfig(Element config) throws ConfigurationException {
        Element modelElement = SodUtil.getElement(config, "modelName");
        if (modelElement != null) {
            modelName = SodUtil.getNestedText(modelElement);
        }
        Element phaseNameElement = SodUtil.getElement(config, "phaseName");
        if (phaseNameElement != null) {
            String phaseName = SodUtil.getNestedText(phaseNameElement);
            if (phaseName.equals("P")) {
                pWave = true;
            } else {
                pWave = false;
            }
        }
        Element gElement = SodUtil.getElement(config, "gaussianWidth");
        if (gElement != null) {
            String gwidthStr = SodUtil.getNestedText(gElement);
            gwidth = Float.parseFloat(gwidthStr);
        }
        Element bumpsElement = SodUtil.getElement(config, "maxBumps");
        if (bumpsElement != null) {
            String bumpsStr = SodUtil.getNestedText(bumpsElement);
            maxBumps = Integer.parseInt(bumpsStr);
        }
        Element toleranceElement = SodUtil.getElement(config, "tolerance");
        if (toleranceElement != null) {
            String toleranceStr = SodUtil.getNestedText(toleranceElement);
            tol = Float.parseFloat(toleranceStr);
        }
        Element asciiWriterElement = SodUtil.getElement(config, "asciiWriter");
        if (asciiWriterElement != null) {
            SacWriter sac = new SacWriter(asciiWriterElement);
            writer = sac;
        }
        Element mseedWriterElement = SodUtil.getElement(config, "mseedWriter");
        if (mseedWriterElement != null) {
            MseedWriter mseed = new MseedWriter(mseedWriterElement);
            writer = mseed;
        }
        Element sacWriterElement = SodUtil.getElement(config, "sacWriter");
        if (sacWriterElement != null) {
            SacWriter sac = new SacWriter(sacWriterElement);
            writer = sac;
        }
    }

    @Override
    Measurement calculate(CacheEvent event,
                          ChannelGroup channelGroup,
                          RequestFilter[][] original,
                          RequestFilter[][] available,
                          LocalSeismogramImpl[][] seismograms,
                          CookieJar cookieJar) throws Exception {
            Channel chan = channelGroup.getChannels()[0];
            Origin origin = event.get_preferred_origin();
            ChannelId[] chanIds = new ChannelId[channelGroup.getChannels().length];
            for (int i = 0; i < chanIds.length; i++) {
                chanIds[i] = channelGroup.getChannels()[i].get_id();
            }
            LocalSeismogramImpl[] singleSeismograms = new LocalSeismogramImpl[3];
            for (int i = 0; i < singleSeismograms.length; i++) {
                singleSeismograms[i] = seismograms[i][0];
            }
            IterDeconResult[] ans = process(event, channelGroup, singleSeismograms);
            String[] phaseName = pWave ? new String[] {"ttp"} : new String[] {"tts"};
            List<Arrival> pPhases = taup.calcTravelTimes(chan.getSite().getStation(), origin, phaseName);
            MicroSecondDate firstP = new MicroSecondDate(origin.getOriginTime());
            firstP = firstP.add(new TimeInterval(pPhases.get(0).getTime(), UnitImpl.SECOND));
            TimeInterval shift = getShift();
            List<Measurement> measurementList = new ArrayList<Measurement>();
            for (int i = 0; i < ans.length; i++) {
                float[] predicted = ans[i].getPredicted();
                // ITR for radial
                // ITT for tangential
                String chanCode = (i == 0) ? "ITR" : "ITT";
                double az = (i == 0) ? Rotate.getRadialAzimuth(channelGroup.getStation().getLocation(),
                                                              event.getPreferred().getLocation())
                        : Rotate.getTransverseAzimuth(channelGroup.getStation().getLocation(), event.getPreferred()
                                .getLocation());
                LocalSeismogramImpl rfSeis = saveTimeSeries(predicted,
                               "receiver function " + singleSeismograms[0].channel_id.station_code,
                               chanCode,
                               firstP.subtract(shift),
                               singleSeismograms[0],
                               UnitImpl.DIMENSONLESS,
                               new Orientation((float)az, 0),
                               event,
                               channelGroup,
                               original,
                               available,
                               cookieJar);
                String mName = (i == 0) ? "radial" : "transverse";
                measurementList.add(new SeismogramMeasurement(mName, rfSeis));
                measurementList.add(new ScalarMeasurement(mName+"_percentMatch", ans[i].getPercentMatch()));
            }
            return new ListMeasurement(getName(), measurementList);
        
    }

    public IterDeconResult[] process(EventAccessOperations event,
                                     ChannelGroup channelGroup,
                                     LocalSeismogramImpl[] localSeis) throws NoPreferredOrigin, FissuresException,
            IncompatibleSeismograms, TauModelException, ZeroPowerException {
        return process(event, channelGroup.getChannels(), localSeis);
    }

    public IterDeconResult[] process(EventAccessOperations event, Channel[] channel, LocalSeismogramImpl[] localSeis)
            throws NoPreferredOrigin, IncompatibleSeismograms, FissuresException, TauModelException, ZeroPowerException {
        LocalSeismogramImpl n = null, e = null, z = null;
        String foundChanCodes = "";
        for (int i = 0; i < localSeis.length; i++) {
            if (localSeis[i].channel_id.channel_code.endsWith("N")) {
                n = localSeis[i];
            } else if (localSeis[i].channel_id.channel_code.endsWith("E")) {
                e = localSeis[i];
            }
            if (localSeis[i].channel_id.channel_code.endsWith("Z")) {
                z = localSeis[i];
            }
            foundChanCodes += localSeis[i].channel_id.channel_code + " ";
        }
        if (n == null || e == null || z == null) {
            logger.error("problem one seismogram component is null ");
            throw new NullPointerException("problem one seismogram component is null, " + foundChanCodes + " " + " "
                    + (n != null) + " " + (e != null) + " " + (z != null));
        }
        Channel nChan = null, eChan = null, zChan = null;
        for (int i = 0; i < channel.length; i++) {
            if (channel[i].get_id().channel_code.endsWith("N")) {
                nChan = channel[i];
            } else if (channel[i].get_id().channel_code.endsWith("E")) {
                eChan = channel[i];
            }
            if (channel[i].get_id().channel_code.endsWith("Z")) {
                zChan = channel[i];
            }
        }
        if (nChan == null || eChan == null || zChan == null) {
            logger.error("problem one channel component is null ");
            throw new NullPointerException("problem one channel component is null, " + " " + (nChan != null) + " "
                    + (eChan != null) + " " + (zChan != null));
        }
        Location staLoc = zChan.getSite().getStation().getLocation();
        Origin origin = event.get_preferred_origin();
        Location evtLoc = origin.getLocation();
        LocalSeismogramImpl[] rotSeis = Rotate.rotateGCP(e,
                                                         eChan.getOrientation(),
                                                         n,
                                                         nChan.getOrientation(),
                                                         staLoc,
                                                         evtLoc,
                                                         "T",
                                                         "R");
        float[][] rotated = {rotSeis[0].get_as_floats(), rotSeis[1].get_as_floats()};
        // check lengths, trim if needed???
        float[] zdata = z.get_as_floats();
        if (rotated[0].length != zdata.length) {
            logger.error("data is not of same length " + rotated[0].length + " " + zdata.length);
            throw new IncompatibleSeismograms("data is not of same length " + rotated[0].length + " " + zdata.length);
        }
        if (zdata.length == 0) {
            throw new IncompatibleSeismograms("data is of zero length ");
        }
        SamplingImpl samp = SamplingImpl.createSamplingImpl(z.sampling_info);
        double period = samp.getPeriod().convertTo(UnitImpl.SECOND).getValue();
        zdata = IterDecon.makePowerTwo(zdata);
        rotated[0] = IterDecon.makePowerTwo(rotated[0]);
        rotated[1] = IterDecon.makePowerTwo(rotated[1]);
        IterDeconResult ansRadial;
        IterDeconResult ansTangential;
        if (pWave) {
            ansRadial = processComponent(rotated[1], zdata, (float)period, staLoc, origin);
            ansTangential = processComponent(rotated[0], zdata, (float)period, staLoc, origin);
        } else {
            // s wave deconvolve horizontal from z, opposite of P wave
            ansRadial = processComponent(zdata, rotated[1], (float)period, staLoc, origin);
            ansTangential = processComponent(zdata, rotated[0], (float)period, staLoc, origin);
        }
        IterDeconResult[] ans = new IterDeconResult[2];
        ans[0] = ansRadial;
        ans[1] = ansTangential;
        return ans;
    }

    public IterDeconResult processComponent(float[] component,
                                            float[] zdata,
                                            float period,
                                            Location staLoc,
                                            Origin origin) throws TauModelException, ZeroPowerException {
        if (component.length == 0) {
            throw new ArrayIndexOutOfBoundsException("Component length is " + component.length);
        }
        if (zdata.length == 0) {
            throw new ArrayIndexOutOfBoundsException("Z component length is " + zdata.length);
        }
        IterDeconResult ans = decon.process(component, zdata, period);
        float[] predicted = ans.getPredicted();
        logger.info("predicted.length = " + predicted.length);
        String[] phaseName = pWave ? new String[] {"ttp"} : new String[] {"tts"};
        List<Arrival> pPhases = taup.calcTravelTimes(staLoc, origin, phaseName);
        MicroSecondDate firstP = new MicroSecondDate(origin.getOriginTime());
        logger.debug("origin " + firstP);
        firstP = firstP.add(new TimeInterval(pPhases.get(0).getTime(), UnitImpl.SECOND));
        logger.debug("firstP " + firstP);
        // TimeInterval shift = firstP.subtract(z.getBeginTime());
        shift = (TimeInterval)shift.convertTo(UnitImpl.SECOND);
        if (shift.getValue() != 0) {
            logger.debug("shifting by " + shift + "  before 0=" + predicted[0]);
            predicted = IterDecon.phaseShift(predicted, (float)shift.getValue(), period);
            logger.debug("shifting by " + shift);
        }
        logger.info("Finished with receiver function processing");
        logger.debug("rec func begin " + firstP.subtract(shift));
        ans.predicted = predicted;
        ans.setAlignShift(shift);
        return ans;
    }

    public LocalSeismogramImpl saveTimeSeries(float[] data,
                               String name,
                               String chanCode,
                               MicroSecondDate begin,
                               LocalSeismogramImpl refSeismogram,
                               UnitImpl unit,
                               Orientation orientation,
                               CacheEvent event,
                               ChannelGroup channelGroup,
                               RequestFilter[][] original,
                               RequestFilter[][] available,
                               CookieJar cookieJar) throws Exception {
        ChannelId recFuncChanId = new ChannelId(refSeismogram.channel_id.network_id,
                                                refSeismogram.channel_id.station_code,
                                                refSeismogram.channel_id.site_code,
                                                chanCode,
                                                refSeismogram.channel_id.begin_time);
        ChannelImpl recFuncChan = new ChannelImpl(recFuncChanId, name, orientation, channelGroup.getChannel1()
                .getSamplingInfo(), channelGroup.getChannel1().getEffectiveTime(), channelGroup.getChannel1().getSite());
        LocalSeismogramImpl predSeis = new LocalSeismogramImpl("recFunc/" + chanCode + "/" + refSeismogram.get_id(),
                                                               begin.getFissuresTime(),
                                                               data.length,
                                                               refSeismogram.sampling_info,
                                                               unit,
                                                               recFuncChanId,
                                                               data);
        predSeis.setName(name);
        if (writer != null) {
            writer.accept(event, recFuncChan, original[0], available[0], new LocalSeismogramImpl[] {predSeis}, cookieJar);
        }
        return predSeis;
    }

    public float getGwidth() {
        return gwidth;
    }

    public float getTol() {
        return tol;
    }

    public int getMaxBumps() {
        return maxBumps;
    }

    public boolean ispWave() {
        return pWave;
    }

    public TimeInterval getShift() {
        return shift;
    }

    public TimeInterval getPad() {
        return pad;
    }


    public boolean isOverwrite() {
        return overwrite;
    }

    public static float DEFAULT_GWIDTH = 2.5f;

    public static int DEFAULT_MAXBUMPS = 400;

    public static float DEFAULT_TOL = 0.001f;

    protected float gwidth = DEFAULT_GWIDTH;

    protected float tol = DEFAULT_TOL;

    protected int maxBumps = DEFAULT_MAXBUMPS;
    
    protected String modelName = "prem";
    
    protected boolean pWave = true;

    protected TimeInterval shift = getDefaultShift();

    protected TimeInterval pad = getDefaultShift();

    protected AbstractSeismogramWriter writer;

    static public final TimeInterval DEFAULT_SHIFT = new TimeInterval(10, UnitImpl.SECOND);

    public static TimeInterval getDefaultShift() {
        return DEFAULT_SHIFT;
    }

    boolean overwrite = false;

    TauPUtil taup;

    IterDecon decon;

    public boolean isThreadSafe() {
        return true;
    }

    final static Logger logger = LoggerFactory.getLogger(IterDeconReceiverFunction.class);
}
