package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.exceptionHandler.WrappedException;

/**
 * ConfigurationException.java
 *
 *
 * Created: Fri Mar 15 15:34:04 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class ConfigurationException
    extends Exception  implements WrappedException
{

    /**
     * Creates a new <code>ConfigurationException</code> instance.
     *
     */
    public ConfigurationException (){

    }

    /**
     * Creates a new <code>ConfigurationException</code> instance.
     *
     * @param s a <code>String</code> value
     */
    public ConfigurationException (String s){
        super(s);
    }

    /**
     * Creates a new <code>ConfigurationException</code> instance.
     *
     * @param s a <code>String</code> value
     * @param e an <code>Exception</code> value
     */
    public ConfigurationException (String s, Throwable e){
        super(s, e);
    }

    /**
     * Describe <code>getCausalException</code> method here.
     *
     * @return an <code>Exception</code> value
     */
    public Throwable getCausalException() {
        return getCause();
    }

}// ConfigurationException
