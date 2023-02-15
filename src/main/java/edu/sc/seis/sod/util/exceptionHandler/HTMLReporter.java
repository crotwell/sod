/**
 * HTMLReporter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.util.exceptionHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import edu.sc.seis.sod.util.time.ClockUtil;

public class HTMLReporter implements ExceptionReporter {

    public HTMLReporter(File directory) throws IOException {
        this.directory = directory;
        numErrors = getLastHandledErNo();
        initIndexFile();
    }

    public void report(String message, Throwable e, List sections)
            throws IOException {
        File outFile = new File(directory, "Exception_" + ++numErrors + ".html");
        appendToIndexFile(outFile, e);
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String str = getHeader(e, numErrors);
        str += message;
        str += "\n<br/>\n<br/>\n";
        String stackTrace = "<h2>Stack Trace</h2><br/>";
        str += stackTrace + makeDivider(stackTrace.length()) + "<pre>"
                + ExceptionReporterUtils.getTrace(e) + "</pre>";
        bw.write(constructString(str, sections));
        bw.close();
    }

    protected void initIndexFile() throws IOException {
        char q = '"';
        File index = new File(directory, "index.html");
        if(index.exists()) {
            BufferedWriter out = new BufferedWriter(new FileWriter(index, true));
            writeln(out, "<hr>");
            writeln(out, "Restarted at " + ClockUtil.now() + ".<br>");
            writeln(out, "There have been " + numErrors + " errors so far.");
            writeln(out, "<hr>");
            out.close();
        } else {
            BufferedWriter out = new BufferedWriter(new FileWriter(index));
            writeln(out, "<html>");
            writeln(out, "   <head>");
            writeln(out, "      <title>Errors</title>");
            writeln(out, "      <style media=" + q + "all" + q + ">@import "
                    + q + "../main.css" + q + ";</style>");
            writeln(out, "   </head>");
            writeln(out, "   <body>");
            writeln(out, "      <div id=" + q + "Header" + q
                    + ">Errors found:</div>");
            writeln(out, "<br/>");
            writeln(out, "<div id=" + q + "Content" + q + ">");
            out.close();
        }
        // we do not end the body or html tags to allow simple appends to this
        // file. Most browsers are ok with this
    }

    protected void appendToIndexFile(File errorFile, Throwable t)
            throws IOException {
        File index = new File(directory, "index.html");
        BufferedWriter out = new BufferedWriter(new FileWriter(index, true));
        writeln(out, ClockUtil.now() + " <a href=" + '"'
                + errorFile.getName() + '"' + ">" + t.getClass().getName()
                + "</a><br/>");
        out.close();
    }

    protected void writeln(BufferedWriter out, String s) throws IOException {
        out.write(s);
        out.newLine();
    }
    
    /*
     * Tries to ascertain how many exception files are already in
     * the directory.
     */
    protected int getLastHandledErNo(){
        File[] files = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.substring(0, 10).equals("Exception_"));
            }
        });        
        return files.length;
    }

    private String constructString(String initialBit, List sections) {
        Iterator it = sections.iterator();
        while(it.hasNext()) {
            initialBit += "<hr/><br/>\n" + constructString((Section)it.next());
        }
        return initialBit;
    }

    private String constructString(Section sec) {
        String result = sec.getName() + "<br/>"
                + makeDivider(sec.getName().length());
        return result += "<pre>" + sec.getContents() + "</pre>";
    }

    private String makeDivider(int len) {
        return "<hr/>";
    }

    protected String getHeader(Throwable t, int i) {
        String s = "<html>\n";
        s += "  <head>\n";
        s += "     <title>" + t.getClass().getName() + " " + i + "</title>\n";
        s += "  </head>\n";
        s += "  <body>\n";
        return s;
    }

    private File directory;
    
    private int numErrors;
}