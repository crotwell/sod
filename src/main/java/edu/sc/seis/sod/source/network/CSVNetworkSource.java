package edu.sc.seis.sod.source.network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.csvreader.CsvReader;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.source.AbstractCSVSource;
import edu.sc.seis.sod.subsetter.AreaSubsetter;

public class CSVNetworkSource extends AbstractCSVSource implements NetworkSource {

    public CSVNetworkSource(Element config) throws ConfigurationException {
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
        stations = getStationsFromReader(AreaSubsetter.makeRelativeOrRecipeDirReader(stationFile));
        channels = getChannelsFromReader(AreaSubsetter.makeRelativeOrRecipeDirReader(channelFile),
                                         stations);
    }

    public String getDescription() {
        return "CSVNetworkSource: " + csvFilename;
    }

    public List<NetworkAttrImpl> getNetworksFromStations(List<StationImpl> staList) {
        Map<String, NetworkAttrImpl> nets = new HashMap<String, NetworkAttrImpl>();
        for (StationImpl sta : staList) {
            nets.put(StationIdUtil.toStringNoDates(sta.getId()),
                     new NetworkAttrImpl(sta.getId().network_id, "", "", ""));
        }
        List<NetworkAttrImpl> out = new ArrayList<NetworkAttrImpl>();
        for (NetworkAttrImpl net : nets.values()) {
            out.add(net);
        }
        return out;
    }

    public List<StationImpl> getStationsFromReader(Reader reader) throws IOException, FileNotFoundException,
            ConfigurationException {
        List<StationImpl> stations = new ArrayList<StationImpl>();
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
            Unit elevationUnit = loadUnit(headers, csvReader, ELEVATION_UNITS, UnitImpl.METER);
            Unit depthUnit = loadUnit(headers, csvReader, DEPTH_UNITS, UnitImpl.METER);
            Location location = new Location(latitude,
                                             longitude,
                                             new QuantityImpl(elevation, elevationUnit),
                                             new QuantityImpl(depth, depthUnit),
                                             LocationType.GEOGRAPHIC);
            NetworkId netId = new NetworkId(netCode, loadTime(headers, csvReader, NET_START, DEFAULT_TIME));
            Time staBegin = loadTime(headers, csvReader, START, DEFAULT_TIME);
            StationId staId = new StationId(netId, staCode, staBegin);
            StationImpl station = new StationImpl(staId,
                                                  loadString(headers, csvReader, NAME, ""),
                                                  location,
                                                  loadString(headers, csvReader, OPERATOR, ""),
                                                  loadString(headers, csvReader, DESCRIPTION, ""),
                                                  loadString(headers, csvReader, COMMENT, ""),
                                                  new NetworkAttrImpl(netId, "", "", ""));
            stations.add(station);
        }
        return stations;
    }

    protected StationImpl getStationForChannel(String netCode, String staCode) {
        for (StationImpl stationImpl : stations) {
            if (netCode.equals(stationImpl.getNetworkAttrImpl().get_code())
                    && staCode.equals(stationImpl.get_code())) {
                return stationImpl;
            }
        }
        return null;
    }
    
    public List<ChannelImpl> getChannelsFromReader(Reader reader, List<StationImpl> stations) throws IOException,
            FileNotFoundException, ConfigurationException {
        List<ChannelImpl> channels = new ArrayList<ChannelImpl>();
        CsvReader csvReader = new CsvReader(reader);
        List<String> headers = validateHeaders(csvReader);
        while (csvReader.readRecord()) {
            String netCode = csvReader.get(NET_CODE);
            String staCode = csvReader.get(STATION_CODE);
            String siteCode = csvReader.get(SITE_CODE);
            String chanCode = csvReader.get(CODE);
            StationImpl curStation = getStationForChannel(netCode, staCode);
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
                Unit elevationUnit = loadUnit(headers, csvReader, ELEVATION_UNITS, UnitImpl.METER);
                Unit depthUnit = loadUnit(headers, csvReader, DEPTH_UNITS, UnitImpl.METER);
                location = new Location(latitude,
                                        longitude,
                                        new QuantityImpl(elevation, elevationUnit),
                                        new QuantityImpl(depth, depthUnit),
                                        LocationType.GEOGRAPHIC);
            } else {
                location = curStation.getLocation();
            }
            Time chanBegin = loadTime(headers, csvReader, START, DEFAULT_TIME);
            float azimuth = loadFloat(headers, csvReader, AZIMUTH, ChannelImpl.getAzimuth(chanCode));
            float dip = loadFloat(headers, csvReader, DIP, ChannelImpl.getDip(chanCode));
            SamplingImpl sampling;
            if (headers.contains(SAMPLE_PERIOD)) {
                sampling = new SamplingImpl(1, new TimeInterval(loadFloat(headers, csvReader, SAMPLE_PERIOD, 1),
                                                                UnitImpl.SECOND));
            } else if (headers.contains(SAMPLE_PERIOD)) {
                sampling = new SamplingImpl(1, new TimeInterval(1 / loadFloat(headers, csvReader, SAMPLE_FREQUENCY, 1),
                                                                UnitImpl.SECOND));
            } else {
                sampling = new SamplingImpl(1, new TimeInterval(1, UnitImpl.SECOND));
            }
            TimeRange chanTime = new TimeRange(chanBegin, loadTime(headers, csvReader, END, DEFAULT_END));
            ChannelImpl channel = new ChannelImpl(new ChannelId(curStation.get_id().network_id,
                                                                staCode,
                                                                siteCode,
                                                                chanCode,
                                                                chanBegin),
                                                  loadString(headers, csvReader, NAME, ""),
                                                  new Orientation(azimuth, dip),
                                                  sampling,
                                                  chanTime,
                                                  new SiteImpl(new SiteId(curStation.get_id().network_id,
                                                                          staCode,
                                                                          siteCode,
                                                                          chanBegin), location, curStation, ""));
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

    protected List<NetworkAttrImpl> networks;

    protected List<StationImpl> stations;

    protected List<ChannelImpl> channels;

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
    public List<? extends ChannelImpl> getChannels(StationImpl station) {
        List<ChannelImpl> out = new ArrayList<ChannelImpl>();
        for (ChannelImpl chan : channels) {
            if (StationIdUtil.areEqual(station.getId(), chan.getStationImpl().getId())) {
                out.add(chan);
            }
        }
        return out;
    }

    @Override
    public Instrumentation getInstrumentation(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse {
        throw new ChannelNotFound();
    }

    @Override
    public CacheNetworkAccess getNetwork(NetworkAttrImpl attr) {
        return new CacheNetworkAccess(null, attr);
    }

    @Override
    public List<? extends CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound {
        throw new NetworkNotFound();
    }

    @Override
    public List<? extends NetworkAttrImpl> getNetworks() {
        return Collections.unmodifiableList(networks);
    }

    @Override
    public QuantityImpl getSensitivity(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse {
        throw new ChannelNotFound();
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkAttrImpl net) {
        List<StationImpl> staList = new ArrayList<StationImpl>();
        for (StationImpl sta : stations) {
            if (NetworkIdUtil.areEqual(net.getId(), sta.getId().network_id)) {
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
    public TimeInterval getRefreshInterval() {
        return new TimeInterval(0, UnitImpl.MILLISECOND);
    }

    @Override
    public String getName() {
        if (csvFilename != null && csvFilename.length() != 0) {
            return csvFilename;
        }
        return "inline";
    }

}