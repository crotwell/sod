/**
 * EventSorter.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.sc.seis.sod.subsetter.NameGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;

public class EventSorter{
    public EventSorter(){ this(null); }
    
    public EventSorter(Element config){
        setSorting(config);
    }
    
    public void setSorting(Element config){
        if(config == null || config.getChildNodes().getLength() == 0){
            sorter = new Sorter();
        }else{
            Element sortType = (Element)config.getFirstChild();
            String ordering = sortType.getAttribute("order");
            if(sortType.getNodeName().equals("addition")){
                sorter = new Sorter();
            }else if(sortType.getNodeName().equals("date")){
                sorter = new DateSorter();
            }else if(sortType.getNodeName().equals("magnitude")){
                sorter = new MagnitudeSorter();
            }else if(sortType.getNodeName().equals("location")){
                sorter = new LocationSorter();
            }else if(sortType.getNodeName().equals("depth")){
                sorter = new DepthSorter();
            }
            if(ordering.equals("reverse")) sorter = new ReverseSorter(sorter);
        }
        sorted.clear();
        Iterator it = additionOrdered.iterator();
        synchronized(additionOrdered){
            while(it.hasNext()) sort((EventAccessOperations)it.next());
        }
    }
    
    public List getSortedEvents() {
        return sorted;
    }
    
    public void add(EventAccessOperations event) {
        additionOrdered.add(event);
        sort(event);
    }
    
    private void sort(EventAccessOperations event){
        sorted.add(sorter.getPosition(event), event);
    }
    
    private class Sorter{
        public int getPosition(EventAccessOperations event){
            return sorted.size();
        }
    }
    
    private class DateSorter extends Sorter{
        public int getPosition(EventAccessOperations event) {
            int i = 0;
            MicroSecondDate eventOrigin = new MicroSecondDate(getOrigin(event).origin_time);
            Iterator it = sorted.iterator();
            while(it.hasNext()){
                EventAccessOperations cur = (EventAccessOperations)it.next();
                MicroSecondDate curOriginDate = new MicroSecondDate(getOrigin(cur).origin_time);
                if(!curOriginDate.before(eventOrigin)) break;
                i++;
            }
            return i;
        }
    }
    
    private class MagnitudeSorter extends Sorter{
        public int getPosition(EventAccessOperations event){
            int i = 0;
            float eventMag = getOrigin(event).magnitudes[0].value;
            Iterator it = sorted.iterator();
            while(it.hasNext()){
                EventAccessOperations cur = (EventAccessOperations)it.next();
                float curMag = getOrigin(cur).magnitudes[0].value;
                if(curMag > eventMag) break;
                i++;
            }
            return i;
        }
    }
    
    private class DepthSorter extends Sorter{
        public int getPosition(EventAccessOperations event){
            int i = 0;
            QuantityImpl eventDepth = (QuantityImpl)getOrigin(event).my_location.depth;
            Iterator it = sorted.iterator();
            while(it.hasNext()){
                EventAccessOperations cur = (EventAccessOperations)it.next();
                QuantityImpl curDepth = (QuantityImpl)getOrigin(cur).my_location.depth;
                if(curDepth.greaterThan(eventDepth)) break;
                i++;
            }
            return i;
        }
    }
    
    private class LocationSorter extends Sorter{
        public int getPosition(EventAccessOperations event){
            int i = 0;
            String loc = NameGenerator.getRegionName(event);
            Iterator it = sorted.iterator();
            while(it.hasNext()){
                EventAccessOperations cur = (EventAccessOperations)it.next();
                String curLoc = NameGenerator.getRegionName(cur);
                if(loc.compareTo(curLoc) < 0) break;
                i++;
            }
            return i;
        }
    }
    
    private class ReverseSorter extends Sorter{
        public ReverseSorter(Sorter reversedSorter){
            this.reversedSorter = reversedSorter;
        }
        
        public int getPosition(EventAccessOperations event){
            return sorted.size() - reversedSorter.getPosition(event);
        }
        
        private Sorter reversedSorter;
    }
    
    private Origin getOrigin(EventAccessOperations event){
        Origin o = event.get_origins()[0];
        try {
            o = event.get_preferred_origin();
        } catch (NoPreferredOrigin e) {}
        return o;
    }
    
    private Sorter sorter;
    
    private List sorted = Collections.synchronizedList(new ArrayList());
    
    private List additionOrdered = Collections.synchronizedList(new ArrayList());
}
