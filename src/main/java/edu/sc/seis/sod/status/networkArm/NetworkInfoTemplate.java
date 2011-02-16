/**
 * NetworkInfoTemplate.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.status.networkArm;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.sod.status.FileWritingTemplate;

public abstract class NetworkInfoTemplate extends FileWritingTemplate{
    private Logger logger = LoggerFactory.getLogger(NetworkInfoTemplate.class);

    public NetworkInfoTemplate(String baseDir, String outputLocation) throws IOException{
        super(baseDir, outputLocation);
    }

    public void write(){
        super.write();
    }
}


