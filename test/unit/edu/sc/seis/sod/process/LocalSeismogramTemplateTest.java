/**
 * LocalSeismogramTemplateTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process;

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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;

public class LocalSeismogramTemplateTest extends TestCase {

    public void testVelocity() throws Exception {
        BasicConfigurator.configure();
        EventDbObject edb = new EventDbObject(7, MockEventAccessOperations.createEvent());
        ChannelDbObject cdb = new ChannelDbObject(3, MockChannel.createChannel());
        EventChannelPair ecp = new EventChannelPair(edb, cdb, null, 11, Status.get(Stage.DATA_SUBSETTER, Standing.SUCCESS));

        CookieJar cookieJar = new CookieJar(ecp);
        cookieJar.getContext().put("A", new A());
        cookieJar.getContext().put("B", "Test B");
        BufferedReader in = new BufferedReader( new InputStreamReader(LocalSeismogramTemplate.class.getClassLoader().getResourceAsStream("edu/sc/seis/sod/data/templates/waveformArm/localSeismogram.xml")));
        //BufferedReader in = new BufferedReader( new InputStreamReader(LocalSeismogramTemplate.class.getClassLoader().getResourceAsStream("edu/sc/seis/sod/data/templates/waveformArm/simplevelocity.xml")));
        String inString = "";
        String s;
        while((s = in.readLine()) != null) {
            inString += s;
        }
        String out = LocalSeismogramTemplate.getVelocityResult(inString, cookieJar);

        Writer sw = new BufferedWriter(new FileWriter("LocalSeismogramTemplateTest.html"));

        sw.write(out);
        sw.close();
    }

    public class A {
        public A() {
            b = new B();
        }
        public B getB() {return b;}
        public B b;
    }
    public class B {
        public B() {
            c = "Test";
        }
        public String getC() {return c;}
        public String c;
    }
}


