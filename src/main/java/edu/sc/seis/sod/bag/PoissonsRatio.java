package edu.sc.seis.sod.bag;


/**
 * @author crotwell
 * Created on Mar 29, 2005
 */
public class PoissonsRatio {

    public static double calcPoissonsRatio(float vpvs) {
        return (1 - .5*(vpvs*vpvs))/(1 - vpvs*vpvs);
    }
    
    public static double calcVpVs(float pr) {
        return Math.sqrt((pr - 1.)/(pr - .5));
    }
}
