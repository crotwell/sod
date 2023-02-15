package edu.sc.seis.sod.bag;

import java.time.Duration;
import java.time.Instant;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

/**
 * Calculus.java
 * 
 * 
 * Created: Thu Aug 15 14:47:23 2002
 * 
 * @author Philip Crotwell
 * @version
 */

public class Calculus {
	public Calculus() {

	}

	public static int[] difference(int[] data) {
		int[] out = new int[data.length - 1];
		for (int i = 0; i < out.length; i++) {
			out[i] = data[i + 1] - data[i];
		} // end of for (int i=0; i<out.length; i++)
		return out;
	}

	public static LocalSeismogramImpl difference(LocalSeismogramImpl seis)
			throws FissuresException {
		int[] seisData = seis.get_as_longs();
		int[] out = difference(seisData);
		return new LocalSeismogramImpl(seis, out);
	}

	public static LocalSeismogramImpl differentiate(LocalSeismogramImpl seis)
			throws FissuresException {
		SamplingImpl samp = seis.getSampling();
		double sampPeriod = TimeUtils.durationToDoubleSeconds(samp.getPeriod());
		LocalSeismogramImpl outSeis;

		if (seis.can_convert_to_float()) {
			float[] data = seis.get_as_floats();
			float[] out = new float[data.length - 1];
			for (int i = 0; i < out.length; i++) {
				out[i] = (float) ((data[i + 1] - data[i]) / sampPeriod);
			} // end of for (int i=0; i<out.length; i++)
			outSeis = new LocalSeismogramImpl(seis, out);
		} else {
			// must be doubles
			double[] data = seis.get_as_doubles();
			double[] out = new double[data.length - 1];
			for (int i = 0; i < out.length; i++) {
				out[i] = (data[i + 1] - data[i]) / sampPeriod;
			}
			outSeis = new LocalSeismogramImpl(seis, out);
		} // end of else
		outSeis.y_unit = UnitImpl.divide(UnitImpl
				.createUnitImpl(outSeis.y_unit), UnitImpl.SECOND);
		Instant begin = outSeis.getBeginTime();
		begin = begin.plus(samp.getPeriod().dividedBy(2));
		outSeis.begin_time = begin;
		return outSeis;
	}

	public static LocalSeismogramImpl integrate(LocalSeismogramImpl seis)
			throws FissuresException {
		SamplingImpl samp = seis.getSampling();
        double sampPeriod = TimeUtils.durationToDoubleSeconds(samp.getPeriod());
		LocalSeismogramImpl outSeis;
		if (seis.can_convert_to_float()) {
			float[] data = seis.get_as_floats();
			float[] out = new float[data.length];
			out[0] = 0;
			for (int i = 1; i < out.length; i++) {
                out[i] = out[i-1] + (data[i-1]+data[i])/2 * (float)sampPeriod;
			}
			outSeis = new LocalSeismogramImpl(seis, out);
		} else {
			// must be doubles
			double[] data = seis.get_as_doubles();
			double[] out = new double[data.length];
			out[0] = 0;
			for (int i = 1; i < out.length; i++) {
				out[i] = out[i-1] + (data[i-1]+data[i])/2 * sampPeriod;
			}
			outSeis = new LocalSeismogramImpl(seis, out);
		} // end of else
		outSeis.y_unit = UnitImpl.multiply(UnitImpl
				.createUnitImpl(outSeis.y_unit), UnitImpl.SECOND);
		return outSeis;
	}

}// Calculus
