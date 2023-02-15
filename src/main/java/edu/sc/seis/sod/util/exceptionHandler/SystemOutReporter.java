/**
 * SystemOutReporter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.util.exceptionHandler;
import java.util.List;



public class SystemOutReporter implements ExceptionReporter {
	
	public void report(String message, Throwable e, List sections) throws Exception {
		System.out.println(message);
		System.out.println(ExceptionReporterUtils.getTrace(e));
	}
	
	
}

