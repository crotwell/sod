package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.subsetter.Interval;
import org.w3c.dom.Element;

/**
 * specifies the beginOffset
 *<pre>
 *  &lt;beginOffset&gt;
 *          &lt;unit&gt;SECOND&lt;/unit&gt;
 *          &lt;value&gt;-120&lt;/value&gt;
 *  &lt;/beginOffset&gt;
 *</pre>
 */
public class BeginOffset extends Interval {
    public BeginOffset (Element config){
        super(config);
    }

}// BeginOffset
