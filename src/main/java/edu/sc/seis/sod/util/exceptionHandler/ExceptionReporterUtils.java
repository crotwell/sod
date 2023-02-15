/**
 * ExceptionReporterUtils.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.util.exceptionHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import edu.sc.seis.sod.util.time.ClockUtil;

public class ExceptionReporterUtils{
    public static String getTrace(Throwable exception) {
        String traceString = "";
        List extractors = GlobalExceptionHandler.getExtractors();
        Iterator it = extractors.iterator();
        while (it.hasNext()) {
            Extractor ext = (Extractor)it.next();
            if (ext.canExtract(exception)) {
                traceString += ext.extract(exception)+"\n";
                Throwable t = ext.getSubThrowable(exception);
                if (t != null) {
                    traceString += getTrace(t)+"\n";
                }
            }
        }
        traceString += extractTrace(exception);
        return traceString;
    }

    public static String getSysInfo() {
        String sysInfo = "";
        //Date does local time
        sysInfo += "Date : "+new java.util.Date().toString()+"\n";
        //MicroSecondDate does GMT by default
        sysInfo += "Date(GMT) : "+ClockUtil.now().toString()+"\n";
        try {
            sysInfo += "Server offset : "+ClockUtil.getServerTimeOffset()+"\n";
        } catch (IOException e) {
            sysInfo += "Server offset : "+e.toString()+"\n";
        }
        sysInfo += "os.name : "+System.getProperty("os.name")+"\n";
        sysInfo += "os.version : "+System.getProperty("os.version")+"\n";
        sysInfo += "os.arch : "+System.getProperty("os.arch")+"\n";
        sysInfo += "java.runtime.version : "+System.getProperty("java.runtime.version")+"\n";
        sysInfo += "java.class.version : "+System.getProperty("java.class.version")+"\n";
        sysInfo += "java.class.path : "+System.getProperty("java.class.path")+"\n";
        sysInfo += "edu.sc.seis.gee.configuration : "+System.getProperty("edu.sc.seis.gee.configuration")+"\n";
        sysInfo += "user.name : "+System.getProperty("user.name")+"\n";
        sysInfo += "user.timeZone : "+System.getProperty("user.timeZone")+"\n";
        sysInfo += "user.region : "+System.getProperty("user.region")+"\n";
        sysInfo += "Memory : "+ExceptionReporterUtils.getMemoryUsage()+"\n";

        sysInfo += "\n\n\n Other Properties:\n";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        java.util.Properties props = System.getProperties();
        props.list(printWriter);
        printWriter.close();
        sysInfo += stringWriter.getBuffer();
        return sysInfo;
    }

    private static String extractTrace(Throwable e) {
        StringWriter  stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    public static String getClassName(Object o){
        String defaultName = o.getClass().toString();
        int lastPeriod = defaultName.lastIndexOf(".");
        if(lastPeriod != -1) defaultName = defaultName.substring(++lastPeriod);
        return defaultName;
    }

    public static String getMemoryUsage() {
        return ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/MB)+"/"+
            (Runtime.getRuntime().totalMemory()/MB)+"/"+
            (Runtime.getRuntime().maxMemory()/MB+" Mb");
    }

    private static final long MB = 1024*1024;
}

