package edu.sc.seis.sod.status;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.Start;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

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

    private static final TimeInterval TWO_MINUTES = new TimeInterval(2, UnitImpl.MINUTE);

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
                CommonAccess.handleException("Problem getting template for Menu", e);
            }
        }

        return getCommonTemplate(tag, el);
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

