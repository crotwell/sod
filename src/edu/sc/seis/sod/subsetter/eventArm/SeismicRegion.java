package edu.sc.seis.sod.subsetter.eventArm;


import edu.sc.seis.sod.subsetter.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/*
 * <pre>
 * &lt;seismicRegion&gt;&lt;value&gt;314 248 562 0 &lt;/value&gt;&lt;/seismicRegion&gt;
 *</pre>
 * SeismicRegion.java
 *
 *
 * Created: Tue Mar 19 13:28:29 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class SeismicRegion extends FlinnEngdahlRegion {
    /**
     * Creates a new <code>SeismicRegion</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public SeismicRegion (Element config){
	super(config);
    }

    /**
     * Describe <code>getType</code> method here.
     *
     * @return a <code>FlinnEngdahlType</code> value
     */
    public FlinnEngdahlType getType() {

	return FlinnEngdahlType.from_int(0);
    }
    
}// SeismicRegion
