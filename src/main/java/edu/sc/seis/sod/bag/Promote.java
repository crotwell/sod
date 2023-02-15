package edu.sc.seis.sod.bag;

import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

/**
 * Promots one type of primitive number array to another.
 *
 *
 * Created: Wed Nov  6 18:31:14 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version
 */

public class Promote {

    
    /**
     * Creates a new Promote instace. This promotes an array of one primitive
     * type to another type. Going from some types to others may result in a
     * loss of precision, such as float to int or int to short. The type
     * to convert to is given in the constructor as a Number. The class is used
     * as a key.
     *
     * @param num an instance of one of Short, Integer, Float or Double 
     */
    public Promote(Number num) {
	this.num = num;
    }

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) 
	throws Exception {
	if (seis.can_convert_to_short()) {
	    short[] series = seis.get_as_shorts();
	    if ( num instanceof Short) {
		return seis;
	    } else if ( num instanceof Integer) {
		int[] out = new int[series.length];
		for ( int i=0; i<out.length; i++) {
		    out[i] = series[i];
		} // end of for ()
		return new LocalSeismogramImpl(seis, out);
	    } else if ( num instanceof Float) {
		float[] out = new float[series.length];
		for ( int i=0; i<out.length; i++) {
		    out[i] = series[i];
		} // end of for ()
		return new LocalSeismogramImpl(seis, out);
	    } else {
		// assume double
		double[] out = new double[series.length];
		for ( int i=0; i<out.length; i++) {
		    out[i] = series[i];
		} // end of for ()
		return new LocalSeismogramImpl(seis, out);
	    } // end of if ()
	} else if (seis.can_convert_to_long()) {
	    int[] series = seis.get_as_longs();
	    if ( num instanceof Short) {
		short[] out = new short[series.length];
		for ( int i=0; i<out.length; i++) {
		    out[i] = (short)series[i];
		} // end of for ()
		return new LocalSeismogramImpl(seis, out);
	    } else if ( num instanceof Integer) {
		return seis;
	    } else if ( num instanceof Float) {
		float[] out = new float[series.length];
		for ( int i=0; i<out.length; i++) {
		    out[i] = series[i];
		} // end of for ()
		return new LocalSeismogramImpl(seis, out);
	    } else {
		// assume double
		double[] out = new double[series.length];
		for ( int i=0; i<out.length; i++) {
		    out[i] = series[i];
		} // end of for ()
		return new LocalSeismogramImpl(seis, out);
	    } // end of if ()
	} else if (seis.can_convert_to_float()) {
	    float[] series = seis.get_as_floats();
	    if ( num instanceof Short) {
		short[] out = new short[series.length];
		for ( int i=0; i<out.length; i++) {
		    out[i] = (short)series[i];
		} // end of for ()
		return new LocalSeismogramImpl(seis, out);
	    } else if ( num instanceof Integer) {
		int[] out = new int[series.length];
		for ( int i=0; i<out.length; i++) {
		    out[i] = (int)series[i];
		} // end of for ()
		return new LocalSeismogramImpl(seis, out);
	    } else if ( num instanceof Float) {
		return seis;
	    } else {
		// assume double
		double[] out = new double[series.length];
		for ( int i=0; i<out.length; i++) {
		    out[i] = series[i];
		} // end of for ()
		return new LocalSeismogramImpl(seis, out);
	    } // end of if ()
	} else {
	    double[] series = seis.get_as_doubles();
	    if ( num instanceof Short) {
		short[] out = new short[series.length];
		for ( int i=0; i<out.length; i++) {
		    out[i] = (short)series[i];
		} // end of for ()
		return new LocalSeismogramImpl(seis, out);
	    } else if ( num instanceof Integer) {
		int[] out = new int[series.length];
		for ( int i=0; i<out.length; i++) {
		    out[i] = (int)series[i];
		} // end of for ()
		return new LocalSeismogramImpl(seis, out);
	    } else if ( num instanceof Float) {
		float[] out = new float[series.length];
		for ( int i=0; i<out.length; i++) {
		    out[i] = (float)series[i];
		} // end of for ()
		return new LocalSeismogramImpl(seis, out);
	    } else {
		// assume double
		return seis;
	    } // end of if ()
	} // end of else
    }

    protected Number num;

}// LocalSeismogramFunction
