package edu.sc.seis.sod.util.display;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Deprecated
public class ThreadSafeSimpleDateFormat extends DateFormat {

    public ThreadSafeSimpleDateFormat(String format) {
        this(format, TimeZone.getTimeZone("GMT"));
    }
    
    public ThreadSafeSimpleDateFormat(String format, TimeZone zone) {
        this.format = format;
        this.zone = zone;
    }
    
    protected ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(zone);
            return sdf;
        }
    };

    protected final SimpleDateFormat get() {
        return threadLocal.get();
    }
    
    public Date parse(String s) throws ParseException {
        return get().parse(s);
    }
    
    public String toPattern() {
        return format;
    }
    
    @Override
    public void setTimeZone(TimeZone tz) {
        throw new RuntimeException("Should not set time zone after creation as this uses many DateFormat objects in a ThreadLocal");
    }
    
    protected String format;
    
    protected TimeZone zone;

    @Override
    public StringBuffer format(Date d, StringBuffer sb, FieldPosition p) {
        return get().format(d, sb, p);
    }

    @Override
    public Date parse(String s, ParsePosition p) {
        return get().parse(s, p);
    }
}
