package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;

import org.w3c.dom.*;

/**
 * MagType.java
 *
 *
 * Created: Tue Apr  2 15:22:02 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public class MagType implements Subsetter{
    public MagType (Element config){
	
	this.config = config;
    }

    public String getType() {

	return SodUtil.getNestedText(config);

    }

    private Element config;
    
}// MagType
