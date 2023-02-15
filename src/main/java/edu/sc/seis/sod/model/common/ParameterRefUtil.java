/**
 * ParameterRefUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.model.common;


public class ParameterRefUtil{
    public static boolean areEqual(ParameterRef a, ParameterRef b){
        if(a.a_id.equals(b.a_id) && a.creator.equals(b.creator)){
            return true;
        }
        return false;
    }

    public static int hash(ParameterRef pr){
        int result = 72;
        result += result * 15 + pr.a_id.hashCode();
        result += result * 15 + pr.creator.hashCode();
        return result;
    }

    public static boolean areEqual(ParameterRef[] a, ParameterRef[] b){
        if(a.length == b.length){
            for (int i = 0; i < a.length; i++) {
                if( ! areEqual(a[i], b[i])){
                    boolean found = false;
                    for (int j = 0; j < a.length && !found; j++) {
                        if(areEqual(a[i], b[j])){ found = true; }
                    }
                    if( ! found) { return false; }
                }
            }
            return true;
        }
        return false;
    }

    public static int hash(ParameterRef[] paramRefs){
        int result = 0;
        for (int i = 0; i < paramRefs.length; i++) {
            result += hash(paramRefs[i]);
        }
        return result;
    }
}

