package edu.sc.seis.sod;

import org.w3c.dom.*;
import java.lang.reflect.*;

/**
 * SubsetterUtil.java
 *
 *
 * Created: Tue Mar 19 12:00:31 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class SubsetterUtil {
    /**
     * Creates a new <code>SubsetterUtil</code> instance.
     *
     */
    public SubsetterUtil (){
	
    }
    
    /**
     * Describe <code>load</code> method here.
     *
     * @param config an <code>Element</code> value
     * @return a <code>SodElement</code> value
     * @exception ConfigurationException if an error occurs
     */
    public static SodElement load(Element config) 
	throws ConfigurationException {
	
	try {
	    Class[] constructorArgTypes = new Class[1];
	    constructorArgTypes[0] = Element.class;

	    Class subsetterSubclass = 
		Class.forName("edu.sc.seis.sod.subsetter."+
			      config.getTagName());
	    Constructor constructor = 
		subsetterSubclass.getConstructor(constructorArgTypes);
	    Object[] constructorArgs = new Object[1];
	    constructorArgs[0] = config;
	    Object obj = 
		constructor.newInstance(constructorArgs);
	    return (Subsetter)obj;
	} catch (InvocationTargetException e) {
	    // occurs if the constructor throws an exception
	    // don't repackage ConfigurationException
	    Throwable subException = e.getTargetException();
	    if (subException instanceof ConfigurationException) {
		throw (ConfigurationException)subException;
	    } else if (subException instanceof Exception) {
		throw new ConfigurationException("Problem creating "+
						 config.getTagName(),
						 (Exception)subException);
	    } else {
		// not an Exception, so must be an Error
		throw (java.lang.Error)subException;
	    } // end of else
	} catch (Exception e) {
	    throw new ConfigurationException("Problem understanding "+
					     config.getTagName(), e);
	} // end of try-catch
    }

}// SubsetterUtil
