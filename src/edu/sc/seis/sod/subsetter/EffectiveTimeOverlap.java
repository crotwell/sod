package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeUtils;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.Subsetter;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class EffectiveTimeOverlap implements Subsetter{
    public EffectiveTimeOverlap(edu.iris.Fissures.TimeRange range) {
        start = new MicroSecondDate(range.start_time);
        end = new MicroSecondDate(range.end_time);
    }
    
    public EffectiveTimeOverlap (Element config){
        Element childElement = null;
        NodeList children = config.getChildNodes();
        Node node;
        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(tagName.equals("effectiveTimeOverlap") ){
                    childElement =(Element)node;
                }
            }
        }
        children = childElement.getChildNodes();
        for(int  i = 0; i < children.getLength(); i ++) {
            node = children.item(i);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                Element el = (Element)node;
                if(tagName.equals("min")) {
                    start = new MicroSecondDate(getEffectiveTime(el));
                } else if (tagName.equals("max")) {
                    end = new MicroSecondDate(getEffectiveTime(el));
                }
            }
        }
    }
    
    public static edu.iris.Fissures.Time getEffectiveTime(Element el) {
        if(el == null) return null;
        String effectiveTime = SodUtil.getNestedText(el);
        edu.iris.Fissures.Time rtnTime = new edu.iris.Fissures.Time(effectiveTime,0);
        return rtnTime;
    }
    
    public boolean overlaps(edu.iris.Fissures.TimeRange otherRange) {
        MicroSecondDate otherStart = new MicroSecondDate(otherRange.start_time);
        MicroSecondDate otherEnd;
        if (otherRange.end_time.date_time.equals(edu.iris.Fissures.TIME_UNKNOWN.value)) {
            otherEnd = new MicroSecondDate(TimeUtils.future);
        } else {
            otherEnd = new MicroSecondDate(otherRange.end_time);
        } // end of else
        if (end == null && start == null) {
            return true;
        } else if (end == null && start.before(otherEnd)) {
            return true;
        } else if (start == null && end.after(otherStart)) {
            return true;
        } else if(otherStart.after(end) || otherEnd.before(start) ) {
            return false;
        } else {
            return true;
        }
    }
    
    private MicroSecondDate start, end;
    private static Logger logger = Logger.getLogger(EffectiveTimeOverlap.class);
}// EffectiveTimeOverlap
