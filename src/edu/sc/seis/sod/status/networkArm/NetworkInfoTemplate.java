/**
 * NetworkInfoTemplate.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.status.FileWritingTemplate;
import java.io.IOException;
import org.apache.log4j.Logger;

public abstract class NetworkInfoTemplate extends FileWritingTemplate{
    private Logger logger = Logger.getLogger(NetworkInfoTemplate.class);

    public NetworkInfoTemplate(String baseDir, String outputLocation) throws IOException{
        super(baseDir, outputLocation);
    }

    public void write(){
        logger.debug("queueing " + getOutputDirectory() + "/" + getFilename());
        super.write();
    }
}


