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
import edu.sc.seis.sod.status.waveformArm.LocalSeismogramTemplate;
import junit.framework.TestCase;

public class LocalSeismogramTemplateTest extends TestCase {

    public void testVelocity() throws Exception {
        CookieJar cookieJar = new CookieJar(MockEventAccessOperations.createEvent(),
                                            MockChannel.createChannel());
        cookieJar.getContext().put("A", new A());
        BufferedReader in = new BufferedReader( new InputStreamReader(LocalSeismogramTemplate.class.getClassLoader().getResourceAsStream("edu/sc/seis/sod/data/templates/waveformArm/localSeismogram.xml")));
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

