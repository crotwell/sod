package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * GeographicalRegion.java
 *
 *
 * Created: Tue Mar 19 13:42:28 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class GeographicalRegion  extends FlinnEngdahlRegion{
    public GeographicalRegion (Element config){
	super(config);
    }
    
}// GeographicalRegion
