/**
 * LegacyExecute.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

public class LegacyExecute implements LocalSeismogramProcess  {

    public LegacyExecute (Element config) {

        Element prefixElement = SodUtil.getElement(config, "prefix");
        if (prefixElement != null) {
            String dssPrefix =
                SodUtil.getText(prefixElement);
            if ( dssPrefix != null && dssPrefix.length() != 0) {
                prefix = dssPrefix;
            } // end of if (dataDirectory.exits())
        }
        Element cmdElement = SodUtil.getElement(config, "command");
        if (cmdElement != null) {
            String cmdString =
                SodUtil.getText(cmdElement);
            if ( cmdString != null && cmdString.length() != 0) {
                command = cmdString;
            } // end of if (dataDirectory.exits())
        }
    }

    /**
     * Removes the mean from the seismograms.
     */
    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar)
        throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        String args = command;
        for (int i=0; i<seismograms.length; i++) {
            out[i] = seismograms[i];
            args += " "+(String)cookieJar.get(SaveSeismogramToFile.getCookieName(prefix, channel.get_id(), i));
        } // end of for (int i=0; i<seismograms.length; i++)

        Process process = Runtime.getRuntime().exec(args);
        process.waitFor();
        return new LocalSeismogramResult(out, new StringTreeLeaf(this, process.exitValue()==0, "exit value="+process.exitValue()));
    }

    String command;

    String prefix = "";

}

