package edu.sc.seis.seiswww;

public class JythonInfoImpls {

    public static final JythonInfo[] origin = new JythonInfo[] {
        new JythonInfo("subsetter/origin",
            "OriginSubsetter",
            "event/origin/externalOriginSubsetter",
            "Operates on a single earthquake.  This includes magnitude estimates, a location estimate, origin time and other basic information about an earthquake")};
                                
    public static final JythonInfo[] networks = new JythonInfo[] {
        new JythonInfo("subsetter/network",
            "NetworkSubsetter",
            "network/network/externalNetworkSubsetter",
            "Makes decisions and works on a seismic network.  That includes the time the network was active, its name, operator, code and description"),
        new JythonInfo("subsetter/station",
            "StationSubsetter",
            "network/station/externalStationSubsetter",
            "A station in a network.  A grouping of recording instruments.  It contains a location, the time the instruments were active, a code, name, comment and operator."),
        new JythonInfo("subsetter/site",
            "SiteSubsetter",
            "network/site/externalSiteSubsetter",
            "A site is the group of channels with the same location code in a station.  It has a more precise location than the station and a code"),
        new JythonInfo("subsetter/channel",
            "ChannelSubsetter",
            "network/channel/externalChannelSubsetter",
            "All of the information in the network arm is available here.  The channel provides its effective time, code, orientation, nominal sampling and has all of the information from its site, station, and network.")};
                                
    public static final JythonInfo[] waveforms = new JythonInfo[] {
        new JythonInfo("subsetter/eventStation",
            "EventStationSubsetter",
            "waveform/eventStation/external",
            "Works on the combination of an event and station.  Existing subsetters check on things like the station's distance from the event and things like that."),
        new JythonInfo("subsetter/eventChannel",
            "EventChannelSubsetter",
            "waveform/eventChannel/external",
            "Works on the combination of event and channel."),
        new JythonInfo("subsetter/request",
            "Request",
            "waveform/requestSubsetter/external",
            "Checks on the properties of a given request."),
        new JythonInfo("subsetter/availableData",
            "AvailableDataSubsetter",
            "waveform/availableData/externalAvailable",
            "Checks the results of asking the server what data it has available for the generated request for acceptability."),
        new JythonInfo("process/waveform",
            "WaveformProcess",
            "waveform/waveformProcess/externalWaveformProcess",
            "Operates on the returned seismograms.")};
                                
    public static final JythonInfo[] vector = new JythonInfo[] {
        new JythonInfo("subsetter/eventChannel/vector",
            "EventVectorSubsetter",
            "waveform/eventVector/external",
            "Looks at the earthquake in combination with a vector of channels"),
        new JythonInfo("subsetter/request/vector",
            "VectorRequest",
            "waveform/vectorRequestSubsetter/external",
            "Checks on the requests for all channels in the vector"),
        new JythonInfo("subsetter/availableData/vector",
            "VectorAvailableDataSubsetter",
            "waveform/vectorAvailableData/external",
            "Checks the available data for the entire vector"),
        new JythonInfo("process/waveform/vector",
            "WaveformVectorProcess",
            "waveform/waveformVectorProcess/external",
            "Works on all seismograms in the vector at once")};
                                
}
