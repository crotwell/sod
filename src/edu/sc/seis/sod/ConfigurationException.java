package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.*;

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

    public ConfigurationException (){
	
    }

    public ConfigurationException (String s){
        super(s);       
    }

    public ConfigurationException (String s, Exception e){
        super(s);
        causalException = e;
    }

    protected Exception causalException = null;

    public Exception getCausalException() {
        return causalException;
    }
    
}// ConfigurationException
