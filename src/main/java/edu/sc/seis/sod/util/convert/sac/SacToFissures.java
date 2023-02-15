package edu.sc.seis.sod.util.convert.sac;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.Month;
import java.time.ZonedDateTime;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.quakeml.Event;
import edu.sc.seis.seisFile.fdsnws.quakeml.Origin;
import edu.sc.seis.seisFile.fdsnws.quakeml.RealQuantity;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.seisFile.sac.SacConstants;
import edu.sc.seis.seisFile.sac.SacHeader;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.Orientation;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.SeismogramAttrImpl;
import edu.sc.seis.sod.model.seismogram.TimeSeriesDataSel;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.StationId;

/**
 * SacToFissures.java Created: Thu Mar 2 13:48:26 2000
 * 
 * @author Philip Crotwell
 * @version
 */
public class SacToFissures {

    public SacToFissures() {}
    
    public static LocalSeismogramImpl getSeismogram(File sacFile) throws FileNotFoundException, IOException, FissuresException {
        SacTimeSeries sac = SacTimeSeries.read(sacFile);
        return getSeismogram(sac);
    }
    
    public static LocalSeismogramImpl getSeismogram(InputStream in) throws IOException, FissuresException {
        DataInputStream dis;
        if (in instanceof DataInputStream) {
            dis = (DataInputStream)in;
        } else {
            dis = new DataInputStream(in);
        }
        SacTimeSeries sac = SacTimeSeries.read(dis);
        return getSeismogram(sac);
    }

    /**
     * Gets a LocalSeismogram. The data comes from the sac file, while the
     * SeismogramAttr comes from attr. A check is made on the beginTime,
     * numPoints and sampling and the sac file is considered correct for these
     * three.
     */
    public static LocalSeismogramImpl getSeismogram(SacTimeSeries sac,
                                                    SeismogramAttrImpl attr)
            throws FissuresException {
        LocalSeismogramImpl seis = new LocalSeismogramImpl(attr, sac.getY());
        if(seis.getNumPoints() != sac.getHeader().getNpts()) {
            seis.num_points = sac.getHeader().getNpts();
        }
        SamplingImpl samp = seis.getSampling();
        Duration period = ((SamplingImpl)samp).getPeriod();
        if(sac.getHeader().getDelta() != 0) {
            double error = (period.toNanos()/TimeUtils.NANOS_IN_SEC - sac.getHeader().getDelta())
                    / sac.getHeader().getDelta();
            if(error > 0.01) {
                seis.sampling_info = new SamplingImpl(1,
                                                      new QuantityImpl(sac.getHeader().getDelta(),
                                                                       UnitImpl.SECOND).toDuration());
            } 
        }
        if( ! SacConstants.isUndef(sac.getHeader().getB())) {
            Instant beginTime = getSeismogramBeginTime(sac);
            double error = Duration.between(seis.getBeginTime(), 
                                            beginTime).toNanos()
                    / (double)period.toNanos();
            if(Math.abs(error) > 0.01) {
                seis.begin_time = beginTime;
            } // end of if (error > 0.01)
        } // end of if (sac.b != -12345)
        return seis;
    }

    public static LocalSeismogramImpl getSeismogram(SacTimeSeries sac)
            throws FissuresException {
        TimeSeriesDataSel data = new TimeSeriesDataSel();
        data.flt_values(sac.getY());
        return new LocalSeismogramImpl(getSeismogramAttr(sac), data);
    }
    
    public static SeismogramAttrImpl getSeismogramAttr(SacTimeSeries sac)
    throws FissuresException {
        Instant beginTime = getSeismogramBeginTime(sac);
        ChannelId chanId = getChannelId(sac);
        String evtName = "   ";
        SacHeader header = sac.getHeader();
        if( ! SacConstants.isUndef(header.getKevnm())) {
            evtName += header.getKevnm().trim() + " ";
        }
        if( ! SacConstants.isUndef(header.getEvla()) &&  ! SacConstants.isUndef(header.getEvlo())
                &&  ! SacConstants.isUndef(header.getEvdp())) {
            evtName += "lat: " + header.getEvla() + " lon: " + header.getEvlo() + " depth: "
                    + (header.getEvdp() / 1000) + " km";
        }
        if( ! SacConstants.isUndef(sac.getHeader().getGcarc())) {
            DecimalFormat df = new DecimalFormat("##0.#");
            evtName += "  " + df.format(header.getGcarc()) + " deg.";
        }
        if( ! SacConstants.isUndef(sac.getHeader().getAz())) {
            DecimalFormat df = new DecimalFormat("##0.#");
            evtName += "  az " + df.format(header.getAz()) + " deg.";
        }
        // seis id can be anything, so set to net:sta:site:chan:begin
        String seisId = chanId.getNetworkId() + ":"
                + chanId.getStationCode() + ":" + chanId.getLocCode() + ":"
                + chanId.getChannelCode() + ":" + TimeUtils.toISOString(beginTime);
        return new SeismogramAttrImpl(seisId,
                                       beginTime,
                                       sac.getHeader().getNpts(),
                                       new SamplingImpl(1,
                                                        new QuantityImpl(sac.getHeader().getDelta(),
                                                                         UnitImpl.SECOND).toDuration()),
                                       UnitImpl.COUNT,
                                       chanId);
    }

    public static ChannelId getChannelId(SacTimeSeries sac) {
        return getChannelId(sac.getHeader());
    }

    public static ChannelId getChannelId(SacHeader header) {
        if( ! SacConstants.isUndef(header.getKhole())
                && header.getKhole().trim().length() == 2) { return getChannelId(header,
                                                                                 header.getKhole().trim()); }
        return getChannelId(header, "  ");
    }

    public static ChannelId getChannelId(SacTimeSeries sac, String siteCode) {
        return getChannelId(sac.getHeader(), siteCode);
    }

    public static ChannelId getChannelId(SacHeader header, String siteCode) {
        Instant nzTime = getNZTime(header);
        String netCode = "XX";
        if( ! SacConstants.isUndef(header.getKnetwk())) {
            netCode = header.getKnetwk().trim().toUpperCase();
        }
        String staCode = "XXXXX";
        if( ! SacConstants.isUndef(header.getKstnm())) {
            staCode = header.getKstnm().trim().toUpperCase();
        }
        String chanCode = "XXX";
        if( ! SacConstants.isUndef(header.getKcmpnm())) {
            chanCode = header.getKcmpnm().trim().toUpperCase();
            if(chanCode.length() == 5) {
                // site code is first 2 chars of kcmpnm
                siteCode = edu.sc.seis.seisFile.fdsnws.stationxml.Channel.fixLocCode(chanCode.substring(0, 2));
                chanCode = chanCode.substring(2, 5);
            }
        }
        ChannelId id = new ChannelId(netCode,
                                     staCode,
                                     siteCode,
                                     chanCode,
                                     nzTime);
        return id;
    }

    public static Channel getChannel(SacTimeSeries sac) {
        return getChannel(sac.getHeader());
    }
    
    public static Channel getChannel(SacHeader header) {
        ChannelId chanId = getChannelId(header);
        float stel = header.getStel();
        if(stel == -12345.0f) {
            stel = 0;
        } // end of if (stel == -12345.0f)
        float stdp = header.getStdp();
        if(stdp == -12345.0f) {
            stdp = 0;
        } // end of if (stdp == -12345.0f)
        Location loc = new Location(header.getStla(),
                                    header.getStlo(),
                                    new QuantityImpl(header.getStel(), UnitImpl.METER),
                                    new QuantityImpl(header.getStdp(), UnitImpl.METER));
        Orientation orient = new Orientation(header.getCmpaz(), header.getCmpinc() - 90);
        SamplingImpl samp = new SamplingImpl(1,
                                             new QuantityImpl(header.getDelta(),
                                                              UnitImpl.SECOND).toDuration());
        Instant begin_time = getNZTime(header);
        TimeRange effective = new TimeRange(begin_time,
                                            (Instant)null);
        Network netAttr = new Network(chanId.getNetworkId());
        netAttr.setStartDateTime(begin_time);
        StationId staId = new StationId(chanId.getNetworkId(),
                                        chanId.getStationCode(),
                                        begin_time);
        Station station = new Station(netAttr, chanId.getStationCode());
        station.setLatitude(loc.latitude);
        station.setLongitude(loc.longitude);
        station.setElevation((float)loc.elevation.getValue(UnitImpl.METER));
        station.setStartDateTime(begin_time);
        station.setDescription("from sac");
        Channel chan =  new Channel(station,
                                    chanId.getLocCode(),
                               chanId.getChannelCode());
        chan.setAzimuth(orient.azimuth);
        chan.setDip(orient.dip);
        chan.setSampleRate((float)samp.getFrequency().getValue(UnitImpl.HERTZ));
        chan.setStartDateTime(begin_time);
        return chan;
    }

    /**
     * calculates the reference (NZ) time from the sac headers NZYEAR, NZJDAY,
     * NZHOUR, NZMIN, NZSEC, NZMSEC. If any of these are UNDEF (-12345), then ClockUtil.wayPast
     */
    public static Instant getNZTime(SacTimeSeries sac) {
        return getNZTime(sac.getHeader());
    }


    /**
     * calculates the reference (NZ) time from the sac headers NZYEAR, NZJDAY,
     * NZHOUR, NZMIN, NZSEC, NZMSEC. If any of these are UNDEF (-12345), then ClockUtil.wayPast
     */
    public static Instant getNZTime(SacHeader header) {
        if ( SacConstants.isUndef(header.getNzyear()) ||
                SacConstants.isUndef(header.getNzjday()) ||
                SacConstants.isUndef(header.getNzhour()) ||
                SacConstants.isUndef(header.getNzmin()) ||
                SacConstants.isUndef(header.getNzsec()) ||
                SacConstants.isUndef(header.getNzmsec())) {
            return TimeUtils.future;
        }
        ZonedDateTime originTime = ZonedDateTime.of(header.getNzyear(), 
        		Month.JANUARY.getValue(),
        		1,
        		header.getNzhour(),
        		header.getNzmin(),
        		header.getNzsec(),
        		header.getNzmsec() * 1000000,
        		TimeUtils.TZ_UTC);
        originTime = originTime.plusDays(header.getNzjday()-1);
        return originTime.toInstant();
    }

    /**
     * calculates the event origin time from the sac headers O, NZYEAR, NZJDAY,
     * NZHOUR, NZMIN, NZSEC, NZMSEC.
     */
    public static Instant getEventOriginTime(SacTimeSeries sac) {
        return getEventOriginTime(sac.getHeader());
    }

    public static Instant getEventOriginTime(SacHeader header) {
        Instant originTime = getNZTime(header);
        originTime = originTime.plusNanos(Math.round( TimeUtils.NANOS_IN_SEC * header.getO()));
        return originTime;
    }

    /**
     * calculates the seismogram begin time from the sac headers B, NZYEAR,
     * NZJDAY, NZHOUR, NZMIN, NZSEC, NZMSEC.
     */
    public static Instant getSeismogramBeginTime(SacTimeSeries sac) {
        return getSeismogramBeginTime(sac.getHeader());
    }

    /**
     * calculates the seismogram begin time from the sac headers B, NZYEAR,
     * NZJDAY, NZHOUR, NZMIN, NZSEC, NZMSEC.
     */
    public static Instant getSeismogramBeginTime(SacHeader header ) {
        Instant bTime = getNZTime(header);
        Duration sacBMarker = TimeUtils.durationFromSeconds(header.getB());
        bTime = bTime.plus(sacBMarker);
        return bTime;
    }

    public static Event getEvent(SacTimeSeries sac) {
        return getEvent(sac.getHeader());
    }

    public static Event getEvent(SacHeader header) {
        if(! SacConstants.isUndef(header.getO()) && ! SacConstants.isUndef(header.getEvla())
                &&  ! SacConstants.isUndef(header.getEvlo()) &&  ! SacConstants.isUndef(header.getEvdp())) {
            Instant beginTime = getEventOriginTime(header);
            EventAttrImpl attr = new EventAttrImpl("SAC Event");
            OriginImpl[] origins = new OriginImpl[1];
            Location loc;
            if(header.getEvdp() > 1000) {
                loc = new Location(header.getEvla(),
                                   header.getEvlo(),
                                   new QuantityImpl(0, UnitImpl.METER),
                                   new QuantityImpl(header.getEvdp(), UnitImpl.METER));
            } else {
                loc = new Location(header.getEvla(),
                                   header.getEvlo(),
                                   new QuantityImpl(0, UnitImpl.METER),
                                   new QuantityImpl(header.getEvdp(),
                                                    UnitImpl.KILOMETER));
            } // end of else
            
            Origin origin = new Origin(beginTime, header.getEvla(), header.getEvlo());
            if(header.getEvdp() > 1000) {
            		origin.setDepth(new RealQuantity(header.getEvdp()*1000)); // km to meter
            } else {
        		    origin.setDepth(new RealQuantity(header.getEvdp()));
            }
            Event event = new Event(origin);
            return event;
        } else {
            return null;
        }
    }
} // SacToFissures
