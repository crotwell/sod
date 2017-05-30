/**
 * LocalSeismogramTemplateTest.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;

import org.apache.log4j.BasicConfigurator;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.hibernate.eventpair.EventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventStationPair;
import edu.sc.seis.sod.mock.MockStatefulEvent;
import edu.sc.seis.sod.mock.station.MockChannel;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.model.status.Stage;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.model.status.Status;
import edu.sc.seis.sod.status.waveformArm.LocalSeismogramTemplate;
import junit.framework.TestCase;

public class LocalSeismogramTemplateTest extends TestCase {

    public void testVelocity() throws Exception {
        BasicConfigurator.configure();
        StatefulEvent event = MockStatefulEvent.create();
        event.setDbid(7);
        ChannelImpl c = MockChannel.createChannel();
        ChannelImpl cdb = new ChannelImpl(c.get_id(),
                                          c.getName(),
                                          c.getOrientation(),
                                          c.getSamplingInfo(),
                                          c.getEffectiveTime(),
                                          c.getSite(),
                                          3);
        EventChannelPair ecp = new EventChannelPair(event,
                                                    cdb,
                                                    Status.get(Stage.DATA_RETRIEVAL,
                                                               Standing.SUCCESS),
                                                    new EventStationPair(event, 
                                                                         (StationImpl)cdb.getSite().getStation(), 
                                                                         Status.get(Stage.EVENT_CHANNEL_POPULATION, 
                                                                                    Standing.SUCCESS)));
        CookieJar cookieJar = ecp.getCookieJar();
        BufferedReader in = new BufferedReader(new InputStreamReader(LocalSeismogramTemplate.class.getClassLoader()
                .getResourceAsStream("edu/sc/seis/sod/data/templates/waveformArm/localSeismogram.xml")));
        // BufferedReader in = new BufferedReader( new
        // InputStreamReader(LocalSeismogramTemplate.class.getClassLoader().getResourceAsStream("edu/sc/seis/sod/data/templates/waveformArm/simplevelocity.xml")));
        String inString = "";
        String s;
        while((s = in.readLine()) != null) {
            inString += s;
        }
        String out = LocalSeismogramTemplate.getVelocityResult(inString,
                                                               cookieJar);
        File outputLoc = new File("LocalSeismogramTemplateTest.html");
        outputLoc.deleteOnExit();
        Writer sw = new BufferedWriter(new FileWriter(outputLoc));
        sw.write(out);
        sw.close();
    }
}
