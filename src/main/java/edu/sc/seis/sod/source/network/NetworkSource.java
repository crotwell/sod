package edu.sc.seis.sod.source.network;

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
import edu.sc.seis.sod.retry.RetryStrategy;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.Source;

public interface NetworkSource extends Source {

    public TimeInterval getRefreshInterval();

    public List<? extends NetworkAttrImpl> getNetworks() throws SodSourceException;

    public List<? extends StationImpl> getStations(NetworkAttrImpl net) throws SodSourceException;

    public List<? extends ChannelImpl> getChannels(StationImpl station) throws SodSourceException;

    public QuantityImpl getSensitivity(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse, SodSourceException;

    public Instrumentation getInstrumentation(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse, SodSourceException;
    public Response getResponse(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse, SodSourceException;

    public void setConstraints(NetworkQueryConstraints constraints);
    
    public int getRetries();

    public RetryStrategy getRetryStrategy();
}