package edu.sc.seis.sod.util.display;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;


public class ThreadSafeDecimalFormat extends NumberFormat {

    public ThreadSafeDecimalFormat(String pattern) {
        this(pattern, null);
    }
    
    public ThreadSafeDecimalFormat(String pattern, DecimalFormatSymbols symbols) {
        this.pattern = pattern;
        this.symbols = symbols;
    }
    
    final String pattern;
    
    final DecimalFormatSymbols symbols;
    
    private final ThreadLocal<DecimalFormat> threadLocal = new ThreadLocal<DecimalFormat>() {  
        @Override  
        protected DecimalFormat initialValue() {
            if (symbols == null) {
                return new DecimalFormat(pattern);  
            } else {
                return new DecimalFormat(pattern, symbols);
            }
        }  
    };

    @Override
    public StringBuffer format(double d, StringBuffer sb, FieldPosition p) {
        return threadLocal.get().format(d, sb, p);
    }

    @Override
    public StringBuffer format(long l, StringBuffer sb, FieldPosition p) {
        return threadLocal.get().format(l, sb, p);
    }

    @Override
    public Number parse(String s, ParsePosition p) {
        return threadLocal.get().parse(s, p);
    } 
}
