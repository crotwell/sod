/**
 * TimeTemplate.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.status;

import java.util.TimeZone;

import org.w3c.dom.Element;

import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.util.display.ThreadSafeSimpleDateFormat;
import edu.sc.seis.sod.util.time.ClockUtil;

public class TimeTemplate implements GenericTemplate {

    public TimeTemplate(Element config, boolean representTimeInFuture) {
        sdf = createSDF(config);
        this.representTimeInFuture = representTimeInFuture;
    }

    public TimeTemplate(Element config, MicroSecondDate time) {
        this(config, true);
        setTime(time);
    }

    public static ThreadSafeSimpleDateFormat createSDF(Element el) {
        if(el != null) {
            String nestedText = SodUtil.getNestedText(el);
            if(nestedText != null) return new ThreadSafeSimpleDateFormat(nestedText, TimeZone.getTimeZone("GMT"));
        }
        return new ThreadSafeSimpleDateFormat("yyyy-MM-dd HH:mm:ss z", TimeZone.getTimeZone("GMT"));
    }

    public void setTime(MicroSecondDate t) {
        time = new MicroSecondDate(t);
    }

    public String getResult(MicroSecondDate t) {
        setTime(t);
        return getResult();
    }

    public String getResult() {
        if(!representTimeInFuture && time.after(ClockUtil.tomorrow())) return "-";
        return sdf.format(time);
    }

    private ThreadSafeSimpleDateFormat sdf;

    private MicroSecondDate time;

    private boolean representTimeInFuture = true;
}