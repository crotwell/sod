package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.CommonAccess;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;

public abstract class FileWritingTemplate extends Template implements GenericTemplate{
    public FileWritingTemplate(String loc){
        this.outputLocation = testOutputLoc(loc);
    }
    
    private static String testOutputLoc(String loc){
        File outFile = new File(loc);
        try {
            outFile.getCanonicalFile().getParentFile().mkdirs();
        } catch (IOException e) {
            CommonAccess.getCommonAccess().handleException(e, "Trouble making directories for output location for an external file template");
        }
        return loc;
    }
    
    public void write(){
        try {
            File loc = new File(outputLocation);
            File temp = File.createTempFile(loc.getName(), null);
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            writer.write(getResult());
            writer.close();
            temp.renameTo(loc);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
