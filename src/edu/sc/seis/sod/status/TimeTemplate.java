/**
 * TimeTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;


import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.SodUtil;
import java.text.SimpleDateFormat;
import org.w3c.dom.Element;

public class TimeTemplate implements GenericTemplate {
    public TimeTemplate(Element config, boolean representTimeInFuture){
        sdf = createSDF(config);
        this.representTimeInFuture = representTimeInFuture;
    }
    
    public TimeTemplate(Element config, Time time){
        this(config, true);
        setTime(time);
    }
    
    public static SimpleDateFormat createSDF(Element el){
        String nestedText = SodUtil.getNestedText(el);
        if (nestedText != null) return new SimpleDateFormat(nestedText);
        return new SimpleDateFormat();
    }
    
    public void setTime(Time t){ time = new MicroSecondDate(t); }
    
    public String getResult(Time t){
        setTime(t);
        return getResult();
    }
    
    public String getResult() {
        if(!representTimeInFuture && time.after(ClockUtil.future())) return "-";
        return sdf.format(time);
    }
    
    private SimpleDateFormat sdf;
    
    private MicroSecondDate time;
    
    private boolean representTimeInFuture = true;
}

