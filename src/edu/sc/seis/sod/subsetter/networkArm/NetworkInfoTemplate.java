/**
 * NetworkInfoTemplate.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.NowTemplate;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public abstract class NetworkInfoTemplate extends FileWritingTemplate{

    private RunStatus status;
    private Logger logger = Logger.getLogger(NetworkInfoTemplate.class);


    public NetworkInfoTemplate(String outputLocation) throws IOException{
        super(outputLocation);
    }

    public void changeStatus(RunStatus status) throws IOException {
        this.status = status;
        write();
    }

    public void write(){
        logger.debug("queueing " + getOutputDirectory() + "/" + getFilename());
        super.write();
    }

}

