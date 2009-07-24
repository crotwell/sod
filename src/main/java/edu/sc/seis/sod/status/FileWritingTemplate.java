package edu.sc.seis.sod.status;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.eventArm.LastEventTemplate;
import edu.sc.seis.sod.status.waveformArm.NumSuccessfulECPTemplate;
import edu.sc.seis.sod.status.waveformArm.SacDataWrittenTemplate;
import edu.sc.seis.sod.status.waveformArm.WPHTemplate;

public class FileWritingTemplate extends Template implements GenericTemplate, Runnable {
    protected FileWritingTemplate(String baseDir, String loc) throws IOException  {
        this.baseDir = baseDir;
        this.outputLocation = loc;
        testOutputLoc(baseDir + '/' + loc);
    }

    public static String testOutputLoc(String loc) throws IOException  {
        File outFile = new File(loc);
        outFile.getCanonicalFile().getParentFile().mkdirs();
        return loc;
    }

    public void write(){ OutputScheduler.getDefault().schedule(this);}


    public String getResult() {
        StringBuffer buf = new StringBuffer();
        Iterator e = templates.iterator();
        while(e.hasNext()) {
            Object cur = e.next();
            buf.append(((GenericTemplate)cur).getResult());
        }
        return buf.toString();
    }

    protected Object textTemplate(final String text) {
        return new GenericTemplate() {
            public String getResult() { return text; }
        };
    }

    public String getOutputLocation(){ return baseDir + '/' + outputLocation; }

    public String getFilename()  {
        return new File(getOutputLocation()).getName();
    }

    protected File getOutputDirectory() {
        return new File(getOutputLocation()).getParentFile();
    }

    protected Object getTemplate(String tag, Element el) throws ConfigurationException  {
        if (tag.equals("menu")){
            try {
                Element templateEl = TemplateFileLoader.getTemplate(el);
                return new MenuTemplate(templateEl, getOutputLocation(), baseDir);
            } catch (Exception e) {
                GlobalExceptionHandler.handle("Problem getting template for Menu", e);
            }
        }else if(tag.equals("sacDataWritten")){
            return new SacDataWrittenTemplate();
        }else if(tag.equals("lastEvent")){
            return new LastEventTemplate(el);
        }else if(tag.equals("numSuccessfulECP")){
            try {
                return new NumSuccessfulECPTemplate();
            } catch (SQLException e) {
                GlobalExceptionHandler.handle(e);
            }
        }else if(tag.equals("waveformsPerHour")){
            try {
                return new WPHTemplate();
            } catch (SQLException e) {
                GlobalExceptionHandler.handle(e);
            }
        }
        return super.getTemplate(tag, el);
    }

    public void run(){ write(getOutputLocation(), getResult()); }

    public String toString(){ return "FileWriter for " + getOutputLocation(); }

    public static void write(String outputLocation, String output){
        File loc = new File(outputLocation);
        try {
            loc.getParentFile().mkdirs();
            File temp = File.createTempFile(loc.getName(), null, loc.getParentFile());
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            writer.write(output);
            writer.close();
            loc.delete();
            temp.renameTo(loc);
        } catch (IOException e) {
            GlobalExceptionHandler.handle(e);
        }
    }

    public static String getBaseDirectoryName() {
        return Start.getRunProps().getStatusBaseDir();
    }

    private String baseDir;
    private String outputLocation;
    private static Logger logger = Logger.getLogger(FileWritingTemplate.class);
}

