package edu.sc.seis.sod.bag;

import edu.sc.seis.sod.model.seismogram.LocalMotionVectorImpl;

/**
 * LocalMotionVectorFunction.java
 *
 *
 * Created: Sun Dec 15 13:38:39 2002
 *
 * @author Philip Crotwell
 * @version $Id: LocalMotionVectorFunction.java 3021 2002-12-16 18:48:10Z crotwell $
 */
public interface LocalMotionVectorFunction {

    public LocalMotionVectorImpl apply(LocalMotionVectorImpl vec);

} // LocalMotionVectorFunction
