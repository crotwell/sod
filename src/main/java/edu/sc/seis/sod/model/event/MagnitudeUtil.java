/**
 * MagnitudeUtil.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.model.event;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


public class MagnitudeUtil {

    public static String toString(Magnitude m) {
        return new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US)).format(m.value) + " " + m.type;
    }

    public static boolean areEqual(Magnitude a, Magnitude b) {
        if(a == b) { return true; }
        return a.contributor.equals(b.contributor) && a.type.equals(b.type)
                && a.value == b.value;
    }

    public static boolean areEqual(Magnitude[] a, Magnitude[] b) {
        if(a.length == b.length) {
            for(int i = 0; i < a.length; i++) {
                if(!areEqual(a[i], b[i])) {
                    boolean found = false;
                    for(int j = 0; j < a.length && !found; j++) {
                        if(areEqual(a[i], b[j])) {
                            found = true;
                        }
                    }
                    if(!found) { return false; }
                }
            }
            return true;
        }
        return false;
    }

    public static int hash(Magnitude m) {
        int result = 63;
        result += result * 37 + m.contributor.hashCode();
        result += result * 37 + m.type.hashCode();
        result += result * 37 + Float.floatToIntBits(m.value);
        return result;
    }

    public static int hash(Magnitude[] mags) {
        int result = 0;
        for(int i = 0; i < mags.length; i++) {
            result += hash(mags[i]);
        }
        return result;
    }

}