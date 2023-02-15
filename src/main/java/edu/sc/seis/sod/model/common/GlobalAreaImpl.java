package edu.sc.seis.sod.model.common;

/**
 * GlobalAreaImpl.java
 *
 *
 * Created: Thu Dec  6 20:15:07 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class GlobalAreaImpl implements Area {
    public GlobalAreaImpl (){
	
    }

    public static java.io.Serializable createEmpty() {
        return new GlobalAreaImpl();
    }
}// GlobalAreaImpl
