package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.Subsetter;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * TimeRange.java
 *
 *
 * Created: Tue Mar 19 13:27:02 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public abstract class TimeRange implements Subsetter{
    
    /**
     * Creates a new <code>TimeRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public TimeRange (Element config){
        Element childElement = null;
        NodeList children = config.getChildNodes();
        Node node;
        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(tagName.equals("timeRange") ){
                    childElement =(Element)node;
                }
            }
            
        }
        children = childElement.getChildNodes();
        for(int  i = 0; i < children.getLength(); i ++) {
            node = children.item(i);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(tagName.equals("startTime")) startTime = getTime((Element)node);
                else if(tagName.equals("endTime")) endTime = getTime((Element)node);
            }
        }
        timeRange = new MicroSecondTimeRange(startTime, endTime);
    }
    
    public Time getStartTime() { return startTime.getFissuresTime(); }
    
    public Time getEndTime() { return endTime.getFissuresTime(); }
    
    private MicroSecondDate getTime(Element e){
        return new MicroSecondDate(new Time(SodUtil.getNestedText(e), 0));
    }
    
    public edu.iris.Fissures.TimeRange getTimeRange() {
        return new edu.iris.Fissures.TimeRange(getStartTime(), getEndTime());
    }
    
    public String toString(){ return timeRange.toString(); }
    
    public MicroSecondTimeRange getMSTR(){ return timeRange; }
    
    public MicroSecondDate getStartMSD(){ return startTime; }
    
    public MicroSecondDate getEndMSD(){ return endTime; }
    
    private MicroSecondTimeRange timeRange;
    
    private MicroSecondDate startTime;
    
    private MicroSecondDate endTime;
    
    static Logger logger = Logger.getLogger(TimeRange.class);
}// TimeRange
