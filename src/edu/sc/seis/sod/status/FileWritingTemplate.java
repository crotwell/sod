package edu.sc.seis.sod.status;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.CommonAccess;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class FileWritingTemplate extends Template implements GenericTemplate {
    protected FileWritingTemplate(String loc) throws IOException  {
        this.outputLocation = testOutputLoc(loc);
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
        while(e.hasNext()) buf.append(((GenericTemplate)e.next()).getResult());
        return buf.toString();
    }

    protected Object textTemplate(final String text) {
        return new GenericTemplate() {
            public String getResult() { return text; }
        };
    }

    public String getOutputLocation(){ return outputLocation; }

    public String getFilename()  {
        return new File(outputLocation).getName();
    }

    protected File getOutputDirectory() {
        return new File(outputLocation).getParentFile();
    }

    private class Writer extends PeriodicAction{
        public void act(){
            logger.debug("writing " + outputLocation);
            File loc = new File(outputLocation);
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

    private Writer w = new Writer();
    private String outputLocation;
    private static Logger logger = Logger.getLogger(FileWritingTemplate.class);
}
