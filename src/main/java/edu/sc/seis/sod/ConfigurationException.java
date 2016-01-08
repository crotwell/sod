package edu.sc.seis.sod;

/**
 * ConfigurationException.java
 * 
 * 
 * Created: Fri Mar 15 15:34:04 2002
 * 
 * @author Philip Crotwell
 */
public class ConfigurationException extends Exception {

    public ConfigurationException(String s) {
        super(s);
    }

    public ConfigurationException(String s, Throwable e) {
        super(s, e);
    }
}// ConfigurationException
