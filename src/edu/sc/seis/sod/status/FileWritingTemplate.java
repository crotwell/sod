package edu.sc.seis.sod.status;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.eventArm.LastEventTemplate;
import edu.sc.seis.sod.status.waveformArm.NumSuccessfulECPTemplate;
import edu.sc.seis.sod.status.waveformArm.SacDataWrittenTemplate;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import java.sql.SQLException;

public class FileWritingTemplate extends Template implements GenericTemplate {
    protected FileWritingTemplate(String baseDir, String loc) throws IOException  {
        this.baseDir = baseDir;
        this.outputLocation = loc;
        testOutputLoc(baseDir + '/' + loc);
    }

    private static String testOutputLoc(String loc) throws IOException  {
        File outFile = new File(loc);
        outFile.getCanonicalFile().getParentFile().mkdirs();
        return loc;
    }

    public void write(){ w.actIfPeriodElapsed(); }

    private MicroSecondDate lastWriteTime;

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
                return new MenuTemplate(TemplateFileLoader.getTemplate(el), getOutputLocation(), baseDir);
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
        }
        return super.getTemplate(tag, el);
    }

    private class Writer extends PeriodicAction{
        public void act(){
            File loc = new File(getOutputLocation());
            try {
                File temp = File.createTempFile(loc.getName(), null);
                BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
                writer.write(getResult());
                writer.close();
                loc.delete();
                temp.renameTo(loc);
            } catch (IOException e) {}
        }
    }

    public static String getBaseDirectoryName() {
        return Start.getProperties().getProperty(FileWritingTemplate.BASE_DIR_PROP_NAME, "status");
    }

    public static final String BASE_DIR_PROP_NAME = "sod.start.StatusBaseDirectory";

    private String baseDir;
    private Writer w = new Writer();
    private String outputLocation;
    private static Logger logger = Logger.getLogger(FileWritingTemplate.class);
}

