package edu.sc.seis.sod.subsetter.waveFormArm;
import edu.sc.seis.sod.subsetter.*;

import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.WaveFormStatus;
import edu.sc.seis.sod.subsetter.ExternalFileTemplate;
import edu.sc.seis.sod.subsetter.GenericTemplate;
import edu.sc.seis.sod.subsetter.eventArm.MapEventStatus;
import java.io.IOException;
import org.w3c.dom.Element;

public class WaveformEventTemplate extends ExternalFileTemplate implements WaveFormStatus{
    public WaveformEventTemplate(Element el) throws IOException{
        super(el);
    }
    
    public WaveformEventTemplate(Element el, String outputLocation){
        super(el, outputLocation);
    }
    
    public void update(EventChannelPair ecp) {
    }
    
    protected Object getTemplate(String tag, Element el) {
        if(tag.equals("channel")) return new ChannelFormatter(el);
        return null;
    }
    
    private class ImageLinkMapStatus extends MapEventStatus implements GenericTemplate{
        public ImageLinkMapStatus(Element el){ super(el); }
        
        public String getResult() { return "<img src=\"" + fileLoc + "\"/>"; }
    }
}
