package edu.sc.seis.sod.hibernate;

/**
 * SeismogramFileTypes.java
 * 
 * 
 * Created: Tue Mar 18 15:38:13 2003
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
import java.net.MalformedURLException;
import java.net.URL;

import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;

public class SeismogramFileTypes {

    private SeismogramFileTypes(String name, int intVal) {
        this.name = name;
        this.intVal = intVal;
    }

    public String toString() {
        return getName();
    }

    public boolean equals(Object obj) {
        if(!(obj instanceof SeismogramFileTypes))
            return false;
        return ((SeismogramFileTypes)obj).getName().equals(this.name);
    }

    public String getName() {
        return this.name;
    }

    public int getIntValue() {
        return this.intVal;
    }

    public URL getURLValue() {
        try {
            return new URL(URL_PREFIX + getName());
        } catch(MalformedURLException e) {
            // shouldn't ever happen as these are static strings
            GlobalExceptionHandler.handle("Trouble creating URL for file type "
                    + getName(), e);
        }
        return null;
    }

    public static SeismogramFileTypes fromString(String typeURL)
            throws UnsupportedFileTypeException {
        if(typeURL.equals(MSEED.getURLValue().toString())) {
            return MSEED;
        } else if(typeURL.equals(SAC.getURLValue().toString())) {
            return SAC;
        } else if(typeURL.equals(PSN.getURLValue().toString())) {
            return PSN;
        } else if (typeURL.equals(RT_130.getURLValue().toString())) {
            return RT_130;
        }
        throw new UnsupportedFileTypeException(typeURL);
    }

    public static SeismogramFileTypes fromInt(int type)
            throws UnsupportedFileTypeException {
        if(type == MSEED.getIntValue()) {
            return MSEED;
        } else if(type == SAC.getIntValue()) {
            return SAC;
        } else if(type == PSN.getIntValue()) {
            return PSN;
        } else if (type == RT_130.getIntValue()) {
            return RT_130;
        }
        throw new UnsupportedFileTypeException("" + type);
    }

    public static final SeismogramFileTypes SAC = new SeismogramFileTypes("sac",
                                                                          1);

    public static final SeismogramFileTypes MSEED = new SeismogramFileTypes("mseed",
                                                                            2);

    public static final SeismogramFileTypes PSN = new SeismogramFileTypes("psn", 3);
    
    public static final SeismogramFileTypes RT_130 = new SeismogramFileTypes("rt130", 4);
    
    public static final SeismogramFileTypes SIMPLE_ASCII = new SeismogramFileTypes("simple_ascii", 5);

    public static final String URL_PREFIX = "http://www.seis.sc.edu/xml/SeismogramFileTypes/";

    private String name;

    private int intVal;
}// SeismogramFileTypes
