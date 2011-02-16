package edu.sc.seis.sod.process.waveform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.Pass;

public class QuitOnce implements WaveformProcess {

    public WaveformResult accept(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        File f = new File("QuitOnceRan");
        if(f.exists()) {
            logger.info("Found my file");
            return new WaveformResult(seismograms, new Pass(this));
        }
        OutputStream s = new FileOutputStream(f);
        s.write("Hello, World!".getBytes());
        s.close();
        // Sleep for 20 seconds to allow the db to actually output the status
        // changes made in the waveform arm
        Thread.sleep(20 * 1000);
        System.exit(1);
        return new WaveformResult(seismograms, new Pass(this));
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(QuitOnce.class);
}
