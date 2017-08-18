package edu.sc.seis.sod.source.network;

import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.retry.RetryStrategy;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.Source;

public interface NetworkSource extends Source {

    public TimeInterval getRefreshInterval();

    public List<? extends Network> getNetworks() throws SodSourceException;

    public List<? extends Station> getStations(Network net) throws SodSourceException;

    public List<? extends Channel> getChannels(Station station) throws SodSourceException;

    public QuantityImpl getSensitivity(Channel chanId) throws ChannelNotFound, InvalidResponse, SodSourceException;

    public Response getResponse(Channel chanId) throws ChannelNotFound, InvalidResponse, SodSourceException;

    public void setConstraints(NetworkQueryConstraints constraints);
    
    public int getRetries();

    public RetryStrategy getRetryStrategy();
}