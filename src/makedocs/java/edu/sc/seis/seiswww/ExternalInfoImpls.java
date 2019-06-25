package edu.sc.seis.seiswww;

public class ExternalInfoImpls {

    public static final ExternalInfo[] origin = new ExternalInfo[] {
        new ExternalInfo("subsetter/origin",
            "OriginSubsetter",
            "event/origin/externalOriginSubsetter",
            "Operates on a single earthquake.  This includes magnitude estimates, a location estimate, origin time and other basic information about an earthquake")};
                                
    public static final ExternalInfo[] networks = new ExternalInfo[] {
        new ExternalInfo("subsetter/network",
            "NetworkSubsetter",
            "externalNetworkSubsetter",
            "Makes decisions and works on a seismic network.  That includes the time the network was active, its name, operator, code and description"),
        new ExternalInfo("subsetter/station",
            "StationSubsetter",
            "externalStationSubsetter",
            "A station in a network.  A grouping of recording instruments.  It contains a location, the time the instruments were active, a code, name, comment and operator."),
        new ExternalInfo("subsetter/channel",
            "ChannelSubsetter",
            "externalChannelSubsetter",
            "All of the information in the network arm is available here.  The channel provides its effective time, code, orientation, nominal sampling and has all of the information from its site, station, and network.")};
                                
    public static final ExternalInfo[] waveforms = new ExternalInfo[] {
        new ExternalInfo("subsetter/eventStation",
            "EventStationSubsetter",
            "externalEventStation",
            "Works on the combination of an event and station.  Existing subsetters check on things like the station's distance from the event and things like that."),
        new ExternalInfo("subsetter/eventChannel",
            "EventChannelSubsetter",
            "externalEventChannelSubsetter",
            "Works on the combination of event and channel."),
        new ExternalInfo("subsetter/request",
            "Request",
            "externalRequestSubsetter",
            "Checks on the properties of a given request."),
        new ExternalInfo("subsetter/availableData",
            "AvailableDataSubsetter",
            "externalAvailable",
            "Checks the results of asking the server what data it has available for the generated request for acceptability."),
        new ExternalInfo("process/waveform",
            "WaveformProcess",
            "externalWaveformProcess",
            "Operates on the returned seismograms.")};
                                
    public static final ExternalInfo[] vector = new ExternalInfo[] {
        new ExternalInfo("subsetter/eventChannel/vector",
            "EventVectorSubsetter",
            "waveform/eventVector/external",
            "Looks at the earthquake in combination with a vector of channels"),
        new ExternalInfo("subsetter/request/vector",
            "VectorRequest",
            "waveform/vectorRequestSubsetter/external",
            "Checks on the requests for all channels in the vector"),
        new ExternalInfo("subsetter/availableData/vector",
            "VectorAvailableDataSubsetter",
            "waveform/vectorAvailableData/external",
            "Checks the available data for the entire vector"),
        new ExternalInfo("process/waveform/vector",
            "WaveformVectorProcess",
            "waveform/waveformVectorProcess/external",
            "Works on all seismograms in the vector at once")};
                                
}
