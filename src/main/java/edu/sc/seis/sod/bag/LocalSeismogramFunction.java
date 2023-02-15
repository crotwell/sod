package edu.sc.seis.sod.bag;

import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

/**
 * LocalSeismogramFunction.java
 *
 *
 * Created: Wed Nov  6 18:31:14 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version
 */

public interface LocalSeismogramFunction {

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) 
	throws Exception;

}// LocalSeismogramFunction
