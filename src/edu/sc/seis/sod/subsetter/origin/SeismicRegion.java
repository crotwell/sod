package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.FlinnEngdahlType;
import org.w3c.dom.Element;
/*
 * <pre>
 * &lt;seismicRegion&gt;&lt;value&gt;0&lt;/value&gt;&lt;/seismicRegion&gt;
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
    public SeismicRegion (Element config){
        super(config);
    }

    public FlinnEngdahlType getType() {
        return FlinnEngdahlType.from_int(0);
    }

}// SeismicRegion
