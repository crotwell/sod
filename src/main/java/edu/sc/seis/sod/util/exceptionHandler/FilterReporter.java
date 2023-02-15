/**
 * FilterReporter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.util.exceptionHandler;
import java.util.Iterator;
import java.util.List;



public class FilterReporter implements ExceptionReporter {

    /** Configure this reporter with a pass through Reporter and a List
     *  that contains Throwable Classes that are to be ignored. All
     * Throwables that are not instanceof the classes in the list
     * are passed on to the reporter. Those in the list are silently
     * ignored. */
    public FilterReporter(ExceptionReporter reporter, List ignoreList) {
        this.reporter = reporter;
        this.ignoreList = ignoreList;
    }

    public void report(String message, Throwable e, List sections) throws Exception {
        Iterator it = ignoreList.iterator();
        while (it.hasNext()) {
            Class c = (Class)it.next();
            if (c.isInstance(e)) {
                return;
            }
        }
        reporter.report(message, e, sections);
    }

    ExceptionReporter reporter;

    List ignoreList;

}

