package edu.sc.seis.sod.source.network;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.QuantityImpl;
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
    public Duration getRefreshInterval() {
        return wrapped.getRefreshInterval();
    }

    @Override
    public List<? extends Network> getNetworks() throws SodSourceException {
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
                List<? extends Network> result = wrapped.getNetworks();
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
    public List<? extends Station> getStations(Network net) throws SodSourceException {
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
                List<? extends Station> result = wrapped.getStations(net);
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
    public List<? extends Channel> getChannels(Station station)  throws SodSourceException {
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
                List<? extends Channel> result = wrapped.getChannels(station);
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
    public Response getResponse(Channel chan) throws ChannelNotFound, InvalidResponse, SodSourceException {
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
