/**
 * GlobalExceptionHandler.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.util.exceptionHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandler {

    private static final String EXITONEXCEPTION = "fissuresUtil.exceptionHandler.exitOnException";

    public static void handle(Throwable thrown) {
        handle("Houston, we have a problem...", thrown);
    }

    public static int getNumHandled() {
        return numHandled;
    }

    public static void handle(String message) {
        handle(message, new Exception());
    }

    public static void handle(String message, Throwable thrown) {
        handle(message, thrown, new ArrayList());
    }

    public static void handle(String message,
                              Throwable thrown,
                              List additonalSections) {
        try {
            Iterator intIt = interceptors.iterator();
            boolean handledByInterceptor = false;
            while(intIt.hasNext()) {
                ExceptionInterceptor interceptor = (ExceptionInterceptor)intIt.next();
                handledByInterceptor = interceptor.handle(message, thrown);
                if(handledByInterceptor)
                    break;
            }
            if(!handledByInterceptor) {
                if(reporters.size() == 0) {
                    System.err.println(message);
                    thrown.printStackTrace(System.err);
                    logger.error("handle exception, but there are no Reporters.",
                                 thrown);
                } else {
                    List parsedContents = new ArrayList(sectionToContents.size());
                    Iterator it = sectionToContents.keySet().iterator();
                    while(it.hasNext()) {
                        String name = (String)it.next();
                        parsedContents.add(new Section(name,
                                                       parse(sectionToContents.get(name))));
                    }
                    if(showSysInfo) {
                        parsedContents.add(new Section("System Information",
                                                       ExceptionReporterUtils.getSysInfo()));
                    }
                    it = additonalSections.iterator();
                    while(it.hasNext()) {
                        parsedContents.add(it.next());
                    }
                    HashMap reporterExceptions = new HashMap();
                    synchronized(reporters) {
                        numHandled++;
                        it = reporters.iterator();
                        while(it.hasNext()) {
                            ExceptionReporter reporter = (ExceptionReporter)it.next();
                            try {
                                reporter.report(message,
                                                                      thrown,
                                                                      parsedContents);
                            } catch(Throwable e) {
                                it.remove();
                                reporterExceptions.put(e, reporter);
                            }
                        }
                    }
                    it = reporterExceptions.keySet().iterator();
                    while(it.hasNext()) {
                        Throwable t = (Throwable)it.next();
                        handle("An exception reporter caused this exception, "+reporterExceptions.get(t)+".  It has been removed from the GlobalExceptionHandler",
                               t);
                    }
                }
                Iterator it = postProcessors.iterator();
                while(it.hasNext()) {
                    try {
                    PostProcess process = (PostProcess)it.next();
                    process.process(message, thrown);
                    } catch (Throwable t) {
                        paranoid(t, thrown);
                    }
                }
            }
        } catch(Throwable e) {
            paranoid(e, thrown);
        }
        if(exitOnException) {
            System.exit(1);
        }
    }

    public static void add(ExceptionInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    /*
     * Add an interceptor someplace other than the end of the list so as to
     * affect its priority over other interceptors. A position of 0 puts the
     * interceptor at the beginning of the list and thus makes it the first one
     * to be run. Hurrah for bombastic documentation!
     */
    public static void add(ExceptionInterceptor interceptor, int position) {
        interceptors.add(position, interceptor);
    }

    public static void remove(ExceptionInterceptor interceptor) {
        interceptors.remove(interceptor);
    }

    public static void add(Extractor extractor) {
        extractors.add(extractor);
    }

    static List getExtractors() {
        return extractors;
    }

    public static void add(ExceptionReporter reporter) {
        reporters.add(reporter);
    }

    public static void remove(ExceptionReporter reporter) {
        reporters.remove(reporter);
    }
    
    public static void add(PostProcess process) {
        postProcessors.add(process);
    }
    
    public static void remove(PostProcess process) {
        postProcessors.remove(process);
    }

    public static void add(String sectionName, File file) {
        sectionToContents.put(sectionName, file);
    }

    public static void append(String sectionName, String contents) {
        List contentsList = null;
        if(sectionToContents.containsKey(sectionName)) {
            contentsList = (List)sectionToContents.get(sectionName);
        } else {
            contentsList = new LinkedList();
            sectionToContents.put(sectionName, contentsList);
        }
        if(contentsList.size() > 1000) {
            contentsList.remove(0);
        }
        contentsList.add(contents);
    }

    /**
     * This supposedly sets a global exception handler in the awt thread only,
     * so that uncaught exceptions can be processed/saved/viewed. Will not
     * necessarily work for future releases (&gt; 1.4). Perhaps it will, perhaps
     * not. NOTE: It does not work for exceptions in other threads. Java1.5 is
     * supposed to have a mechanism to do this.
     */
    public static void registerWithAWTThread() {
        System.setProperty("sun.awt.exception.handler",
                           "edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler");
        // introduced in java1.5
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            public void uncaughtException(Thread thread, Throwable thrown) {
                GlobalExceptionHandler.handle("Thrown in thread "+thread.getName(), thrown);
            }});
    }

    private static String parse(Object item) throws IOException {
        if(item instanceof List)
            return createString((List)item);
        else if(item instanceof File)
            return createString((File)item);
        else
            throw new IllegalArgumentException();
    }

    private static String createString(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuffer message = new StringBuffer();
        String line = reader.readLine();
        while(line != null) {
            message.append(line + "\n");
            line = reader.readLine();
        }
        return message.toString();
    }

    private static String createString(List stringList) {
        StringBuffer message = new StringBuffer();
        Iterator it = stringList.iterator();
        while(it.hasNext()) {
            message.append((String)it.next() + "\n");
        }
        return message.toString();
    }

    private static void paranoid(Throwable e, Throwable thrown) {
        // this is for paranoid coders
        System.err.println("Caught an exception in the exception handler: "
                + e.toString());
        e.printStackTrace(System.err);
        System.err.println("Original exception was:" + thrown.toString());
        thrown.printStackTrace(System.err);
        try {
            logger.error("Caught an exception in the exception handler: ", e);
            logger.error("Original exception was:", thrown);
        } catch(Throwable loggerException) {
            // well, lets hope System.err is good enough.
        }
    }

    private static final boolean exitOnException;
    
    static {
        exitOnException = Boolean.getBoolean(EXITONEXCEPTION);
    }

    private static Map sectionToContents = new HashMap();

    private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static List<ExceptionReporter> reporters = Collections.synchronizedList(new ArrayList<ExceptionReporter>());

    private static List<Extractor> extractors = Collections.synchronizedList(new ArrayList<Extractor>());

    private static List<ExceptionInterceptor> interceptors = Collections.synchronizedList(new ArrayList<ExceptionInterceptor>());
    
    private static List<PostProcess> postProcessors = Collections.synchronizedList(new ArrayList<PostProcess>());

    private static boolean showSysInfo = true;

    private static int numHandled = 0;
    static {
        // always send error to log4j
        add(new Log4jReporter());
        add(new DefaultExtractor());
        add(new QuitOnExceptionPostProcess(java.lang.OutOfMemoryError.class));
    }
}
