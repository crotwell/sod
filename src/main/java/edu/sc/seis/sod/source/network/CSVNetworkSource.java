package edu.sc.seis.sod.source.network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.csvreader.CsvReader;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Comment;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Operator;
import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.Site;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.model.station.StationIdUtil;
import edu.sc.seis.sod.source.AbstractCSVSource;
import edu.sc.seis.sod.subsetter.AreaSubsetter;
import edu.sc.seis.sod.util.time.ClockUtil;

public class CSVNetworkSource extends AbstractCSVSource implements NetworkSource {

    public CSVNetworkSource(Element config) throws ConfigurationException {
        super(config, "CSVNetworkSource");
        initStations(config);
        initChannels(config);
    }
    
    protected void initStations(Element config) throws ConfigurationException {
        if (DOMHelper.hasElement(config, "stationFile")) {
            String filename = DOMHelper.extractText(config, "stationFile");
            this.csvFilename = filename;
            try {
                stations = getStationsFromReader(AreaSubsetter.makeRelativeOrRecipeDirReader(csvFilename));
            } catch(FileNotFoundException e) {
                throw new UserConfigurationException(e.getMessage() + " as a station CSV file.");
            } catch(IOException e) {
                throw new ConfigurationException("Unable to read " + csvFilename, e);
            }
        } else if (DOMHelper.hasElement(config, "stations")) {
            try {
                stations = getStationsFromReader(new StringReader(DOMHelper.extractText(config, "stations").trim()));
            } catch(IOException e) {
                throw new ConfigurationException("Unable to read stations from:"
                        + DOMHelper.extractText(config, "stations"), e);
            }
        } else {
            throw new ConfigurationException("Can't find stationFile or stations in configuration.");
        }
        networks = getNetworksFromStations(stations);
    }
    
    protected void initChannels(Element config) throws ConfigurationException {
        if (DOMHelper.hasElement(config, "channelFile")) {
            String filename = DOMHelper.extractText(config, "channelFile");
            try {
                channels = getChannelsFromReader(AreaSubsetter.makeRelativeOrRecipeDirReader(filename),
                                                 stations);
            } catch(FileNotFoundException e) {
                throw new UserConfigurationException(e.getMessage() + " as a channel CSV file.", e);
            } catch(IOException e) {
                throw new ConfigurationException("Unable to read " + filename, e);
            }
        } else if (DOMHelper.hasElement(config, "channels")) {
            try {
                channels = getChannelsFromReader(new StringReader(DOMHelper.extractText(config, "channels").trim()),
                                                 stations);
            } catch(IOException e) {
                throw new ConfigurationException("Unable to read channels from:"
                        + DOMHelper.extractText(config, "channels"), e);
            }
        } else {
            throw new ConfigurationException("Can't find channelFile or channels in configuration.");
        }
    }

    public CSVNetworkSource(String stationFile, String channelFile) throws ConfigurationException, FileNotFoundException, IOException {
        super("CSVNetworkSource");
        stations = getStationsFromReader(AreaSubsetter.makeRelativeOrRecipeDirReader(stationFile));
        channels = getChannelsFromReader(AreaSubsetter.makeRelativeOrRecipeDirReader(channelFile),
                                         stations);
    }

    public String getDescription() {
        return "CSVNetworkSource: " + csvFilename;
    }

    public List<Network> getNetworksFromStations(List<Station> staList) {
        Map<String, Network> nets = new HashMap<String, Network>();
        for (Station sta : staList) {
            nets.put(StationIdUtil.toStringNoDates(sta),
                     sta.getNetwork());
        }
        List<Network> out = new ArrayList<Network>();
        for (Network net : nets.values()) {
            out.add(net);
        }
        return out;
    }

    public List<Station> getStationsFromReader(Reader reader) throws IOException, FileNotFoundException,
            ConfigurationException {
        List<Station> stations = new ArrayList<Station>();
        CsvReader csvReader = new CsvReader(reader);
        List<String> headers = validateHeaders(csvReader);
        while (csvReader.readRecord()) {
            // time to start populating field values
            // first up: the only required field...
            String netCode = csvReader.get(NET_CODE);
            String staCode = csvReader.get(CODE);
            float latitude = loadFloat(headers, csvReader, LATITUDE, 0);
            float longitude = loadFloat(headers, csvReader, LONGITUDE, 0);
            double elevation = loadDouble(headers, csvReader, ELEVATION, 0);
            double depth = loadDouble(headers, csvReader, DEPTH, 0);
            UnitImpl elevationUnit = loadUnit(headers, csvReader, ELEVATION_UNITS, UnitImpl.METER);
            UnitImpl depthUnit = loadUnit(headers, csvReader, DEPTH_UNITS, UnitImpl.METER);
            Location location = new Location(latitude,
                                             longitude,
                                             new QuantityImpl(elevation, elevationUnit),
                                             new QuantityImpl(depth, depthUnit));
            Instant staBegin = loadTime(headers, csvReader, START, DEFAULT_TIME);
            Network network = new Network(netCode);
            network.setStartDateTime(loadTime(headers, csvReader, NET_START, DEFAULT_TIME));
            Station station = new Station(network, staCode);
            station.setLatitude(latitude);
            station.setLongitude(longitude);
            station.setElevation((float) elevation);
            station.setStartDateTime(staBegin);
            station.setName(loadString(headers, csvReader, NAME, ""));
            station.addOperator(new Operator(loadString(headers, csvReader, OPERATOR, "")));
            station.setSite(new Site("", loadString(headers, csvReader, DESCRIPTION, ""), "","", "", ""));
            station.addComment(new Comment(loadString(headers, csvReader, COMMENT, "")));
            stations.add(station);
        }
        return stations;
    }

    protected Station getStationForChannel(String netCode, String staCode) {
        for (Station stationImpl : stations) {
            if (netCode.equals(stationImpl.getNetworkCode())
                    && staCode.equals(stationImpl.getCode())) {
                return stationImpl;
            }
        }
        return null;
    }
    
    public List<Channel> getChannelsFromReader(Reader reader, List<Station> stations) throws IOException,
            FileNotFoundException, ConfigurationException {
        List<Channel> channels = new ArrayList<Channel>();
        CsvReader csvReader = new CsvReader(reader);
        List<String> headers = validateHeaders(csvReader);
        while (csvReader.readRecord()) {
            String netCode = csvReader.get(NET_CODE);
            String staCode = csvReader.get(STATION_CODE);
            String siteCode = edu.sc.seis.seisFile.fdsnws.stationxml.Channel.fixLocCode(csvReader.get(SITE_CODE));
            String chanCode = csvReader.get(CODE);
            Station curStation = getStationForChannel(netCode, staCode);
            if (curStation == null) {
                throw new UserConfigurationException("Station " + netCode + "." + staCode
                        + " is not a known station. Add it to the stations section.");
            }
            Location location;
            if (headers.contains(LATITUDE) || headers.contains(LONGITUDE) || headers.contains(ELEVATION)
                    || headers.contains(DEPTH)) {
                float latitude = loadFloat(headers, csvReader, LATITUDE, 0);
                float longitude = loadFloat(headers, csvReader, LONGITUDE, 0);
                double elevation = loadDouble(headers, csvReader, ELEVATION, 0);
                double depth = loadDouble(headers, csvReader, DEPTH, 0);
                UnitImpl elevationUnit = loadUnit(headers, csvReader, ELEVATION_UNITS, UnitImpl.METER);
                UnitImpl depthUnit = loadUnit(headers, csvReader, DEPTH_UNITS, UnitImpl.METER);
                location = new Location(latitude,
                                        longitude,
                                        new QuantityImpl(elevation, elevationUnit),
                                        new QuantityImpl(depth, depthUnit));
            } else {
                location = Location.of(curStation);
            }
            Instant chanBegin = loadTime(headers, csvReader, START, DEFAULT_TIME);
            float azimuth = loadFloat(headers, csvReader, AZIMUTH, ChannelIdUtil.getDefaultAzimuth(chanCode));
            float dip = loadFloat(headers, csvReader, DIP, ChannelIdUtil.getDefaultDip(chanCode));
            SamplingImpl sampling;
            if (headers.contains(SAMPLE_PERIOD)) {
                sampling = SamplingImpl.ofSamplesSeconds(1, loadFloat(headers, csvReader, SAMPLE_PERIOD, 1));
            } else if (headers.contains(SAMPLE_FREQUENCY)) {
                sampling = SamplingImpl.ofSamplesSeconds(1, 1 / loadFloat(headers, csvReader, SAMPLE_FREQUENCY, 1));
            } else {
                sampling = SamplingImpl.ofSamplesSeconds(1, 1);
            }
            TimeRange chanTime = new TimeRange(chanBegin, loadTime(headers, csvReader, END, DEFAULT_END));
            Channel channel = new Channel(curStation, siteCode, chanCode, chanTime.getBeginTime().toInstant(), chanTime.getEndTime().toInstant());
            channel.setAzimuth(azimuth);
            channel.setDip(dip);
            channel.setSampleRate((float) sampling.getFrequency().getValue(UnitImpl.HERTZ));
            channels.add(channel);
        }
        return channels;
    }

    @Override

    public void setConstraints(NetworkQueryConstraints constraints) {
        // no op
    }

    public String toString() {
        return "CSVNetworkSource using " + csvFilename;
    }

    protected List<Network> networks;

    protected List<Station> stations;

    protected List<Channel> channels;

    // required
    public static final String NET_CODE = "net.code";

    public static final String STATION_CODE = "station.code";

    public static final String SITE_CODE = "site.code";

    public static final String CODE = "code";

    // optional
    public static final String NET_START = "net.start";

    public static final String NET_END = "net.end";

    public static final String SAMPLE_PERIOD = "sampling.period";

    public static final String SAMPLE_FREQUENCY = "sampling.frequency";

    public static final String START = "start";

    public static final String END = "end";

    public static final String OPERATOR = "operator";

    public static final String COMMENT = "comment";

    public static final String DESCRIPTION = "description";

    // defaultable
    public static final String AZIMUTH = "azimuth";

    public static final String DIP = "dip";

    @Override
    public List<? extends Channel> getChannels(Station station) {
        List<Channel> out = new ArrayList<Channel>();
        for (Channel chan : channels) {
            if (StationIdUtil.areEqual(station, chan.getStation())) {
                out.add(chan);
            }
        }
        return out;
    }

    @Override
    public Response getResponse(Channel chan) throws ChannelNotFound, InvalidResponse {
        throw new ChannelNotFound("Response not in CSVNetworkSource", chan);
    }

    @Override
    public List<? extends Network> getNetworks() {
        return Collections.unmodifiableList(networks);
    }

    @Override
    public QuantityImpl getSensitivity(Channel chan) throws ChannelNotFound, InvalidResponse {
        throw new ChannelNotFound("Response not in CSVNetworkSource", chan);
    }

    @Override
    public List<? extends Station> getStations(Network net) {
        List<Station> staList = new ArrayList<Station>();
        for (Station sta : stations) {
            if (NetworkIdUtil.areEqual(net, sta.getNetwork())) {
                staList.add(sta);
            }
        }
        return staList;
    }

    public String[] getFields() {
        return networkFields;
    }

    private static final String[] networkFields = new String[] {NET_CODE,
                                                                STATION_CODE,
                                                                SITE_CODE,
                                                                CODE,
                                                                LONGITUDE,
                                                                LATITUDE,
                                                                ELEVATION,
                                                                ELEVATION_UNITS,
                                                                DEPTH,
                                                                DEPTH_UNITS,
                                                                SAMPLE_PERIOD,
                                                                SAMPLE_FREQUENCY,
                                                                NET_START,
                                                                NET_END,
                                                                START,
                                                                END,
                                                                NAME,
                                                                FE_SEIS_REGION,
                                                                FE_GEO_REGION,
                                                                FE_REGION,
                                                                FE_REGION_TYPE,
                                                                AZIMUTH,
                                                                DIP,
                                                                OPERATOR,
                                                                COMMENT,
                                                                DESCRIPTION};

    @Override
    public Duration getRefreshInterval() {
        return ClockUtil.ZERO_DURATION;
    }

    @Override
    public String getName() {
        if (csvFilename != null && csvFilename.length() != 0) {
            return csvFilename;
        }
        return "inline";
    }

}