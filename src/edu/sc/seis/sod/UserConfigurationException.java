package edu.sc.seis.sod;

/**
 * A UserConfigurationException indicates a recipe file problem caused by a SOD
 * user.
 * 
 * @author groves
 * 
 * Created on May 3, 2006
 */
public class UserConfigurationException extends ConfigurationException {

    public UserConfigurationException(String s) {
        super(s);
    }
}
