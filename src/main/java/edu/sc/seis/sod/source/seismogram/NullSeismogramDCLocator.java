package edu.sc.seis.sod.source.seismogram;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

public class NullSeismogramDCLocator implements SodElement, SeismogramSourceLocator {

    public SeismogramSource getSeismogramSource(CacheEvent event,
                                             Channel channel,
                                             RequestFilter[] infilters,
                                             MeasurementStorage cookieJar)
            throws Exception {
        throw new ConfigurationException("Cannot use NullSeismogramDCLocator to get a seismogramDC. There must be another type of SeismogramDCLocator within the configuration script");
    }
} // NullSeismogramDCLocator
