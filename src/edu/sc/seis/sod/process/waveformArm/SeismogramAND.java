/**
 * SeismogramAND.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;
import java.util.Iterator;
import java.util.LinkedList;
import org.w3c.dom.Element;

public class SeismogramAND extends ForkProcess {

    public SeismogramAND (Element config) throws ConfigurationException {
        super(config);
    }

    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar
                                        ) throws Exception {

        LocalSeismogramProcess processor;
        LinkedList reasons = new LinkedList();
        Iterator it = localSeisProcessList.iterator();
        LocalSeismogramResult result = new LocalSeismogramResult(true, seismograms, new StringTreeLeaf(this, true));
        while (it.hasNext() && result.isSuccess()) {
            processor = (LocalSeismogramProcess)it.next();
            synchronized (processor) {
                result = processor.process(event,
                                           channel,
                                           original,
                                           available,
                                           copySeismograms(seismograms),
                                           cookieJar);
            }
            reasons.addLast(result.getReason());
        } // end of while (it.hasNext())
        if (reasons.size() < localSeisProcessList.size()) {
            reasons.addLast(new StringTreeLeaf("ShortCurcit", result.isSuccess()));
        }
        return new LocalSeismogramResult(result.isSuccess(),
                                         seismograms,
                                         new StringTreeBranch(this,
                                                              result.isSuccess(),
                                                                  (StringTree[])reasons.toArray(new StringTree[0])));
    }


    public String toString() {
        String s = "SeismogramAND(";
        Iterator it = localSeisProcessList.iterator();
        while (it.hasNext()) {
            s+=it.next().toString()+",";
        }
        s = s.substring(0, s.length()-1);
        s+=")";
        return s;
    }

}

