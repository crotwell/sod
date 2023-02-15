package edu.sc.seis.sod.util.convert.sac;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.sac.Complex;
import edu.sc.seis.seisFile.sac.SacConstants;
import edu.sc.seis.seisFile.sac.SacHeader;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

/**
 * FissuresToSac.java
 * 
 * 
 * Created: Wed Apr 10 10:52:00 2002
 * 
 * @author Philip Crotwell
 * @version
 */

public class FissuresToSac {

	/**
	 * Creates a SacTimeSeries object from a LocalSeismogram. Headers in the SAC
	 * object are filled in as much as possible, with the notable exception of
	 * event information and station location and channel orientation.
	 * 
	 * @param seis
	 *            the <code>LocalSeismogramImpl</code> with the data
	 * @return a <code>SacTimeSeries</code> with data and headers filled
	 */
	public static SacTimeSeries getSAC(LocalSeismogramImpl seis)
			throws CodecException {
		float[] floatSamps;
		try {
			if (seis.can_convert_to_long()) {
				int[] idata = seis.get_as_longs();
				floatSamps = new float[idata.length];
				for (int i = 0; i < idata.length; i++) {
					floatSamps[i] = idata[i];
				}
			} else {
				floatSamps = seis.get_as_floats();
			} // end of else
		} catch (FissuresException e) {
			if (e.getCause() instanceof CodecException) {
				throw (CodecException) e.getCause();
			} else {
				throw new CodecException(e);
			}
		}
		SacHeader header = SacHeader.createEmptyEvenSampledTimeSeriesHeader();
		header.setIztype( SacConstants.IB);
		SamplingImpl samp = (SamplingImpl) seis.sampling_info;
		Duration period = samp.getPeriod();
		float f = (float) TimeUtils.durationToDoubleSeconds(period);
		header.setDelta( f);

		UnitImpl yUnit = (UnitImpl) seis.y_unit;
		QuantityImpl min = (QuantityImpl) seis.getMinValue();
		header.setDepmin( (float) min.convertTo(yUnit).getValue());
		QuantityImpl max = (QuantityImpl) seis.getMaxValue();
		header.setDepmax( (float) max.convertTo(yUnit).getValue());
		QuantityImpl mean = (QuantityImpl) seis.getMeanValue();
		header.setDepmen( (float) mean.convertTo(yUnit).getValue());

		setKZTime(header, seis.begin_time);

		header.setKnetwk(seis.channel_id.getNetworkId());
		header.setKstnm( seis.channel_id.getStationCode());
		header.setKcmpnm( seis.channel_id.getChannelCode());
		header.setKhole( seis.channel_id.getLocCode());

        return new SacTimeSeries(header, floatSamps);
	}

	/**
	 * Creates a SacTimeSeries object from a LocalSeismogram. Headers in the SAC
	 * object are filled in as much as possible, with the notable exception of
	 * event information.
	 * 
	 * @param seis
	 *            a <code>LocalSeismogramImpl</code> value
	 * @param channel
	 *            a <code>Channel</code> value
	 * @return a <code>SacTimeSeries</code> value
	 */
	public static SacTimeSeries getSAC(LocalSeismogramImpl seis, Channel channel)
			throws CodecException {
		SacTimeSeries sac = getSAC(seis);
		addChannel(sac.getHeader(), channel);
		return sac;
	}

	/**
	 * Creates a SacTimeSeries object from a LocalSeismogram. Headers in the SAC
	 * object are filled in as much as possible, with the notable exception of
	 * station location and channel orientation information.
	 * 
	 * @param seis
	 *            a <code>LocalSeismogramImpl</code> value
	 * @param origin
	 *            an <code>Origin</code> value
	 * @return a <code>SacTimeSeries</code> value
	 */
	public static SacTimeSeries getSAC(LocalSeismogramImpl seis, OriginImpl origin)
			throws CodecException {
		SacTimeSeries sac = getSAC(seis);
		addOrigin(sac.getHeader(), origin);
		return sac;
	}

	/**
	 * Creates a SacTimeSeries object from a LocalSeismogram. Headers in the SAC
	 * object are filled in as much as possible.
	 * 
	 * @param seis
	 *            a <code>LocalSeismogramImpl</code> value
	 * @param channel
	 *            a <code>Channel</code> value
	 * @param origin
	 *            an <code>Origin</code> value
	 * @return a <code>SacTimeSeries</code> value
	 */
	public static SacTimeSeries getSAC(LocalSeismogramImpl seis,
			Channel channel, OriginImpl origin) throws CodecException {
		SacTimeSeries sac = getSAC(seis);
		if (channel != null) {
			addChannel(sac.getHeader(), channel);
		}
		if (origin != null) {
			addOrigin(sac.getHeader(), origin);
		}
		if (origin != null && channel != null) {
			DistAz distAz = new DistAz(channel, origin);
			sac.getHeader().setGcarc( (float) distAz.getDelta());
			sac.getHeader().setDist( (float) distAz.getDelta() * 111.19f);
			sac.getHeader().setAz( (float) distAz.getAz());
			sac.getHeader().setBaz( (float) distAz.getBaz());
		}
		return sac;
	}

	/**
	 * Adds the Channel information, including station location and channel
	 * orientation to the sac object.
	 * 
	 * @param header
	 *            a <code>SacTimeSeries</code> header object to be modified
	 * @param channel
	 *            a <code>Channel</code>
	 */
	public static void addChannel(SacHeader header, Channel channel) {
	    header.setStla( (float) channel.getLatitude().getValue());
	    header.setStlo( (float) channel.getLongitude().getValue());
		header.setStel( channel.getElevation().getValue());
		header.setStdp( channel.getDepth().getValue());

		header.setCmpaz( channel.getAzimuth().getValue());
		// sac vert. is 0, fissures and seed vert. is -90
		// sac hor. is 90, fissures and seed hor. is 0
		header.setCmpinc( 90 + channel.getDip().getValue());
	}

	/**
	 * Adds origin informtion to the sac object, including the o marker.
	 * 
	 * @param header
	 *            a <code>SacTimeSeries</code> header object to be modified
	 * @param origin
	 *            an <code>Origin</code> value
	 */
	public static void addOrigin(SacHeader header, OriginImpl origin) {
        header.setEvla( origin.getLocation().latitude);
        header.setEvlo( origin.getLocation().longitude);
		QuantityImpl z = (QuantityImpl) origin.getLocation().elevation;
		header.setEvel( (float) z.convertTo(UnitImpl.METER).getValue());
		z = (QuantityImpl) origin.getLocation().depth;
		header.setEvdp( (float) z.convertTo(UnitImpl.METER).getValue());

		ZonedDateTime isoTime = ZonedDateTime.of(header.getNzyear(), 1, 1, header.getNzhour(),
		                              header.getNzmin(), header.getNzsec(), header.getNzmsec()*1000000, TimeUtils.TZ_UTC);
		isoTime = isoTime.plusDays(header.getNzjday()-1);
		Instant beginTime = isoTime.toInstant();
		Instant originTime = origin.getOriginTime();
		setKZTime(header, originTime);
		Duration sacBMarker =  Duration.between(originTime, beginTime);
		header.setB( (float) TimeUtils.durationToDoubleSeconds(sacBMarker));
		header.setO( 0);
		header.setIztype( SacConstants.IO);
		if (origin.getMagnitudes().length > 0) {
		    header.setMag( origin.getMagnitudes()[0].value);
		}
	}

    public static void setKZTime(SacHeader header, Instant date) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date, TimeUtils.TZ_UTC);
		header.setNzyear( zdt.getYear());
		header.setNzjday( zdt.getDayOfYear());
		header.setNzhour( zdt.getHour());
		header.setNzmin( zdt.getMinute());
		header.setNzsec( zdt.getSecond());
		header.setNzmsec( zdt.getNano() / TimeUtils.NANOS_IN_MILLI );
	}


	private static Complex ONE = new Complex(1,0);

}// FissuresToSac
