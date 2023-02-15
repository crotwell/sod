package edu.sc.seis.sod.util.display;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;


/**
 * @author groves Created on Mar 30, 2005
 */
public class ChoiceDecimalFormat extends NumberFormat {

    /**
     * for each double in limits, if a number is passed into format that is less
     * than it the corresponding format from formats is used. if the number is
     * greater than the last value in limits, the last format is used.
     */
    public ChoiceDecimalFormat(double[] limits, ThreadSafeDecimalFormat[] formats) {
        if(limits.length != formats.length) { throw new IllegalArgumentException("Must be an equal number of limits and formats"); }
        this.limits = limits;
        this.formats = formats;
    }

    public Number parse(String source, ParsePosition parsePosition) {
        throw new UnsupportedOperationException("ChoiceDecimalFormat aint your number parser.  Use one of the java classes DecimalFormat or ChoiceFormat");
    }

    public StringBuffer format(double number,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        for(int i = 0; i < limits.length; i++) {
            if(number < limits[i]) { return formats[i].format(number,
                                                              toAppendTo,
                                                              pos); }
        }
        return formats[formats.length - 1].format(number, toAppendTo, pos);
    }

    public StringBuffer format(long number,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        for(int i = 0; i < limits.length; i++) {
            if(number < limits[i]) { return formats[i].format(number,
                                                              toAppendTo,
                                                              pos); }
        }
        return formats[formats.length - 1].format(number, toAppendTo, pos);
    }

    /**
     * This creates a ChoiceDecimalFormat where numbers &lt; 100 have a single decimal, and numbers $gt;=100
     *  have none
     */
    public static ChoiceDecimalFormat createTomStyleA() {
        return new ChoiceDecimalFormat(new double[] {100, 100},
                                       new ThreadSafeDecimalFormat[] {new ThreadSafeDecimalFormat("0.0"),
                                                            new ThreadSafeDecimalFormat("0")});
    }

    /**
     * This creates a ChoiceDecimalFormat where numbers &lt; 10 have a single decimal, and numbers $gt;=10
     * have none
     */
    public static ChoiceDecimalFormat createTomStyleB() {
        return new ChoiceDecimalFormat(new double[] {10, 10},
                                       new ThreadSafeDecimalFormat[] {new ThreadSafeDecimalFormat("0.0"),
                                                            new ThreadSafeDecimalFormat("0")});
    }

    private double[] limits;

    private ThreadSafeDecimalFormat[] formats;
}