package edu.sc.seis.sod.subsetter.eventArm;



import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 *<pre>
 * &lt;geographicalRegion&gt;10&lt;/geographicalRegion&gt;
 *</pre>
 */

public class GeographicalRegion  extends FlinnEngdahlRegion{
    public GeographicalRegion (Element config){
        super(config);
    }

    public FlinnEngdahlType getType() {
        return FlinnEngdahlType.from_int(1);
    }

}// GeographicalRegion
