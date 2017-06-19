package edu.sc.seis.sod.source.network;

import java.io.IOException;
import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.sod.model.station.InvalidResponse;
import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.source.SodSourceException;


public class RetryNetworkSource extends WrappingNetworkSource implements NetworkSource {

    public RetryNetworkSource(NetworkSource sodElement) {
        super(sodElement);
        this.wrapped = sodElement;
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public TimeInterval getRefreshInterval() {
        return wrapped.getRefreshInterval();
    }

    @Override
    public List<? extends NetworkAttrImpl> getNetworks() throws SodSourceException {
        int count = 0;
        SodSourceException latest;
        try {
            return wrapped.getNetworks();
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(SodSourceException t) {
            if (t.getCause() instanceof IOException 
                    || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                latest = t;
            } else {
                throw t;
            }
        }
        while(wrapped.getRetryStrategy().shouldRetry(latest, this, count++)) {
            try {
                List<? extends NetworkAttrImpl> result = wrapped.getNetworks();
                wrapped.getRetryStrategy().serverRecovered(this);
                return result;
            } catch(SodSourceException t) {
                if (t.getCause() instanceof IOException 
                        || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                    latest = t;
                } else {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw e;
            }
        }
        throw latest;
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkAttrImpl net) throws SodSourceException {
        int count = 0;
        SodSourceException latest;
        try {
            return wrapped.getStations(net);
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(SodSourceException t) {
            if (t.getCause() instanceof IOException 
                    || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                latest = t;
            } else {
                throw t;
            }
        }
        while(wrapped.getRetryStrategy().shouldRetry(latest, this, count++)) {
            try {
                List<? extends StationImpl> result = wrapped.getStations(net);
                wrapped.getRetryStrategy().serverRecovered(this);
                return result;
            } catch(SodSourceException t) {
                if (t.getCause() instanceof IOException 
                        || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                    latest = t;
                } else {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw e;
            }
        }
        throw latest;
    }

    @Override
    public List<? extends ChannelImpl> getChannels(StationImpl station)  throws SodSourceException {
        int count = 0;
        SodSourceException latest;
        try {
            return wrapped.getChannels(station);
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(SodSourceException t) {
            if (t.getCause() instanceof IOException 
                    || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                latest = t;
            } else {
                throw t;
            }
        }
        while(wrapped.getRetryStrategy().shouldRetry(latest, this, count++)) {
            try {
                List<? extends ChannelImpl> result = wrapped.getChannels(station);
                wrapped.getRetryStrategy().serverRecovered(this);
                return result;
            } catch(SodSourceException t) {
                if (t.getCause() instanceof IOException 
                        || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                    latest = t;
                } else {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw e;
            }
        }
        throw latest;
    }

    @Override
    public QuantityImpl getSensitivity(ChannelImpl chan) throws ChannelNotFound, InvalidResponse, SodSourceException {
        int count = 0;
        SodSourceException latest;
        try {
            return wrapped.getSensitivity(chan);
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(SodSourceException t) {
            if (t.getCause() instanceof IOException 
                    || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                latest = t;
            } else {
                throw t;
            }
        }
        while(wrapped.getRetryStrategy().shouldRetry(latest, this, count++)) {
            try {
                QuantityImpl result = wrapped.getSensitivity(chan);
                wrapped.getRetryStrategy().serverRecovered(this);
                return result;
            } catch(SodSourceException t) {
                if (t.getCause() instanceof IOException 
                        || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                    latest = t;
                } else {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw e;
            }
        }
        throw latest;
    }

    @Override
    public Instrumentation getInstrumentation(ChannelImpl chan) throws ChannelNotFound, InvalidResponse, SodSourceException {
        int count = 0;
        SodSourceException latest;
        try {
            return wrapped.getInstrumentation(chan);
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(SodSourceException t) {
            if (t.getCause() instanceof IOException 
                    || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                latest = t;
            } else {
                throw t;
            }
        }
        while(wrapped.getRetryStrategy().shouldRetry(latest, this, count++)) {
            try {
                Instrumentation result = wrapped.getInstrumentation(chan);
                wrapped.getRetryStrategy().serverRecovered(this);
                return result;
            } catch(SodSourceException t) {
                if (t.getCause() instanceof IOException 
                        || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                    latest = t;
                } else {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw e;
            }
        }
        throw latest;
    }
    @Override
    public Response getResponse(ChannelImpl chan) throws ChannelNotFound, InvalidResponse, SodSourceException {
        int count = 0;
        SodSourceException latest;
        try {
            return wrapped.getResponse(chan);
        } catch(OutOfMemoryError e) {
            throw e;
        } catch(SodSourceException t) {
            if (t.getCause() instanceof IOException 
                    || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                latest = t;
            } else {
                throw t;
            }
        }
        while(wrapped.getRetryStrategy().shouldRetry(latest, this, count++)) {
            try {
                Response result = wrapped.getResponse(chan);
                wrapped.getRetryStrategy().serverRecovered(this);
                return result;
            } catch(SodSourceException t) {
                if (t.getCause() instanceof IOException 
                        || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                    latest = t;
                } else {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw e;
            }
        }
        throw latest;
    }

    @Override
    public void setConstraints(NetworkQueryConstraints constraints) {
        wrapped.setConstraints(constraints);
    }
    
    NetworkSource wrapped;
}
