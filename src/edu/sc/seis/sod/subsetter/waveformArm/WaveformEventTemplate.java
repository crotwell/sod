/**
 * WaveformEventTemplate.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.waveFormArm;



import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.WaveFormStatus;
import edu.sc.seis.sod.subsetter.ExternalFileTemplate;
import java.io.IOException;
import org.w3c.dom.Element;

public class WaveformEventTemplate extends ExternalFileTemplate implements WaveFormStatus{
    public WaveformEventTemplate(Element el) throws IOException{
        super(el);
    }
    
    public void update(EventChannelPair ecp) {
        // TODO
    }
    
    protected boolean isInterpreted(String tag) {
        // TODO
        return false;
    }
    
    protected Object getInterpreter(String tag, Element el) {
        // TODO
        return null;
    }
    
    
}
