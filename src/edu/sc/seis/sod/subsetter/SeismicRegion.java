package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * SeismicRegion.java
 *
 *
 * Created: Tue Mar 19 13:28:29 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class SeismicRegion extends FlinnEngdahlRegion {
    public SeismicRegion (Element config){
	super(config);
    }
    
}// SeismicRegion
