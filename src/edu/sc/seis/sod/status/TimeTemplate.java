/**
 * TimeTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;


import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.SodUtil;
import java.text.SimpleDateFormat;
import org.w3c.dom.Element;

public class TimeTemplate implements GenericTemplate {
    public TimeTemplate(Element config){ sdf = createSDF(config); }
    
    public TimeTemplate(Element config, Time time){
        this(config);
        setTime(time);
    }
    
    public static SimpleDateFormat createSDF(Element el){
        String nestedText = SodUtil.getNestedText(el);
        if (nestedText != null) return new SimpleDateFormat(nestedText);
        return new SimpleDateFormat();
    }
    
    public void setTime(Time t){ time = t; }
    
    public String getResult(Time t){
        setTime(t);
        return getResult();
    }
    
    public String getResult() { return sdf.format(new MicroSecondDate(time));
    }
    
    private SimpleDateFormat sdf;
    
    private Time time;
}

