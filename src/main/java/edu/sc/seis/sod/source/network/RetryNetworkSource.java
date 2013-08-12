package edu.sc.seis.sod.source.network;

import java.io.IOException;
import java.util.List;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.SodSourceException;


public class RetryNetworkSource extends AbstractNetworkSource implements NetworkSource {

    public RetryNetworkSource(AbstractNetworkSource wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
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
    public CacheNetworkAccess getNetwork(NetworkAttrImpl attr) {
        return wrapped.getNetwork(attr);
    }

    @Override
    public List<? extends CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound {
        return wrapped.getNetworkByName(name);
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
    public void setConstraints(NetworkQueryConstraints constraints) {
        wrapped.setConstraints(constraints);
    }
    
    AbstractNetworkSource wrapped;
}
