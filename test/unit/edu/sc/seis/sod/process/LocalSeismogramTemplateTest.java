/**
 * LocalSeismogramTemplateTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process;

import java.io.*;

import edu.sc.seis.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import edu.sc.seis.sod.status.waveformArm.LocalSeismogramTemplate;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;

public class LocalSeismogramTemplateTest extends TestCase {

    public void testVelocity() throws Exception {
        BasicConfigurator.configure();
        EventDbObject edb = new EventDbObject(7, MockEventAccessOperations.createEvent());
        ChannelDbObject cdb = new ChannelDbObject(3, MockChannel.createChannel());
        EventChannelPair ecp = new EventChannelPair(edb, cdb, null, 11, Status.get(Stage.DATA_SUBSETTER, Standing.SUCCESS));

        CookieJar cookieJar = new CookieJar(ecp);
        BufferedReader in = new BufferedReader( new InputStreamReader(LocalSeismogramTemplate.class.getClassLoader().getResourceAsStream("edu/sc/seis/sod/data/templates/waveformArm/localSeismogram.xml")));
        //BufferedReader in = new BufferedReader( new InputStreamReader(LocalSeismogramTemplate.class.getClassLoader().getResourceAsStream("edu/sc/seis/sod/data/templates/waveformArm/simplevelocity.xml")));
        String inString = "";
        String s;
        while((s = in.readLine()) != null) {
            inString += s;
        }
        String out = LocalSeismogramTemplate.getVelocityResult(inString, cookieJar);
        File outputLoc = new File("LocalSeismogramTemplateTest.html");
        outputLoc.deleteOnExit();
        Writer sw = new BufferedWriter(new FileWriter(outputLoc));

        sw.write(out);
        sw.close();
    }

}


