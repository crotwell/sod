package edu.sc.seis.sod.subsetter.origin;



import org.w3c.dom.Element;
import edu.iris.Fissures.FlinnEngdahlType;

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
