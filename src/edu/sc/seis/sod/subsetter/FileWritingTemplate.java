package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.CommonAccess;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;

public abstract class FileWritingTemplate extends Template implements GenericTemplate{
    public FileWritingTemplate(String loc) throws IOException {
        this.outputLocation = testOutputLoc(loc);
    }

    private static String testOutputLoc(String loc) throws IOException {
        File outFile = new File(loc);
        outFile.getCanonicalFile().getParentFile().mkdirs();
        return loc;
    }

    public void write() throws IOException {
        File loc = new File(outputLocation);
        File temp = File.createTempFile(loc.getName(), null);
        BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
        writer.write(getResult());
        writer.close();
        loc.delete();
        temp.renameTo(loc);
    }

    public String getResult(){
        StringBuffer buf = new StringBuffer();
        Iterator e = templates.iterator();
        while(e.hasNext()) buf.append(((GenericTemplate)e.next()).getResult());
        return buf.toString();
    }

    protected Object textTemplate(final String text){
        return new GenericTemplate(){
            public String getResult() { return text; }
        };
    }

    public String getFilename() {
        return new File(outputLocation).getName();
    }

    protected File getOutputDirectory(){
        return new File(outputLocation).getParentFile();
    }

    private String outputLocation;

    private static Logger logger = Logger.getLogger(FileWritingTemplate.class);
}
