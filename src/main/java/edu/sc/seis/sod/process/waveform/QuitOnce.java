package edu.sc.seis.sod.process.waveform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.Pass;

public class QuitOnce implements WaveformProcess {

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
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
