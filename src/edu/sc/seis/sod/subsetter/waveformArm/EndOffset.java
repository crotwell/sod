package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.subsetter.Interval;
import org.w3c.dom.Element;

/**
 * specifies the endOffset
 *<pre>
 *  &lt;endOffset&gt;
 *          &lt;unit&gt;SECOND&lt;/unit&gt;
 *          &lt;value&gt;-120&lt;/value&gt;
 *  &lt;/endOffset&gt;
 *</pre>
 */

public class EndOffset extends Interval {
    public EndOffset (Element config){
        super(config);
    }

    public boolean accept(EventAccessOperations event,  Channel channel){
        return true;
    }
}// EndOffset
