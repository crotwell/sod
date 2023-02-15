/**
 * Log4jReporter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.util.exceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log4jReporter implements ExceptionReporter {

    public void report(String message, Throwable e, List sections)
            throws IOException {
        if(e != null) {
            logger.error(message, e);
            if(e.getCause() != null) {
                logger.error("...caused by:", e.getCause());
            }
            Iterator it = GlobalExceptionHandler.getExtractors().iterator();
            while(it.hasNext()) {
                Extractor ext = (Extractor)it.next();
                if(ext.canExtract(e)) {
                    logger.error(ext.getClass().getSimpleName()+": "+ext.extract(e));
                    if(ext.getSubThrowable(e) != null) {
                        report("...caused by subthrowable:",
                               ext.getSubThrowable(e),
                               new ArrayList());
                    }
                }
            }
        } else {
            logger.error(message);
        }
        Iterator sectionIt = sections.iterator();
        while(sectionIt.hasNext()) {
            Section section = (Section)sectionIt.next();
            logger.debug(section.getName() + ":" + section.getContents());
        }
    }

    private static Logger logger = LoggerFactory.getLogger(Log4jReporter.class);
}
