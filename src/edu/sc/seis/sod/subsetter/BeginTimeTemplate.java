/**
 * BeginTimeTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.SodUtil;
import java.text.SimpleDateFormat;
import org.w3c.dom.Element;

public class BeginTimeTemplate implements GenericTemplate {
    private Time time;
    
    public BeginTimeTemplate(Element config){
        String nestedText = SodUtil.getNestedText(config);
        if (nestedText != null)
            sdf = new SimpleDateFormat(nestedText);
        else
            sdf = new SimpleDateFormat();
        time = new Time();
    }
    
    public BeginTimeTemplate(Element config, Time time){
        this(config);
        setTime(time);
    }
    
    public void setTime(Time t){
        time = t;
    }
    
    public String getResult() {
        MicroSecondDate d = new MicroSecondDate(time);
        return sdf.format(d);
    }
    
    SimpleDateFormat sdf;
}

