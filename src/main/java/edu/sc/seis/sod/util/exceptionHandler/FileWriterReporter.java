/**
 * FileWriterReporter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.util.exceptionHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;



public class FileWriterReporter implements ExceptionReporter{
    public FileWriterReporter(File file){
        setFile(file);
    }
    
    public void setFile(File file){
        this.file = file;
    }
    
    public void report(String message, Throwable e, List sections) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        String str = message+ "\n\n";
        String stackTrace = "Stack Trace";
        str +=   stackTrace + makeDivider(stackTrace.length()) + ExceptionReporterUtils.getTrace(e);
        bw.write(constructString(str, sections));
        bw.close();
    }
    
    private String constructString(String initialBit, List sections){
        Iterator it = sections.iterator();
        while(it.hasNext()){
            initialBit += "\n" + constructString((Section)it.next());
        }
        return initialBit;
    }
    
    private String constructString(Section sec) {
        String result = sec.getName() + makeDivider(sec.getName().length());
        return result += sec.getContents();
    }
    
    private String makeDivider(int len){
        StringBuffer div = new StringBuffer(len + 2);
        div.append('\n');
        for (int i = 0; i < len; i++) {
            div.append('=');
        }
        div.append('\n');
        return div.toString();
    }
    
    private File file;
    
}

