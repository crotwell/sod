/**
 * NetworkInfoTemplate.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.subsetter.FileWritingTemplate;
import edu.sc.seis.sod.subsetter.NowTemplate;
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
        try {
            write();
        } catch (IOException e) {
            CommonAccess.handleException(e, "trouble writing file");
        }
    }

    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element el) {
        if (tag.equals("now")){
            NowTemplate now = new NowTemplate();
            return now;
        }
        return null;
    }

    public void write() throws IOException{
        logger.debug("writing " + getOutputDirectory() + "/" + getFilename());
        super.write();
    }

}

