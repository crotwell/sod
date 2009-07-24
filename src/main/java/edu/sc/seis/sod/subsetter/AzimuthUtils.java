/**
 * AzimuthUtils.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.subsetter;

import edu.sc.seis.fissuresUtil.bag.DistAz;

public class AzimuthUtils{
    public static boolean isAzimuthBetween(DistAz dz, double min, double max){
        return isAngleBetween(dz.getAz(), min, max);
    }

    public static boolean isBackAzimuthBetween(DistAz dz, double min, double max){
        return isAngleBetween(dz.getBaz(), min, max);
    }
    public static boolean isAngleBetween(double angle, double min, double max){
        double converted = convert(angle, min);
        return min  <= converted && max >= converted;
    }

    private  static double convert(double az, double min){
        return (az - min)%360 + min;
    }
}

