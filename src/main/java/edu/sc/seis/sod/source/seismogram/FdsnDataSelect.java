package edu.sc.seis.sod.source.seismogram;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Element;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.RequestFilterUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.CoarseAvailableData;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;
import edu.sc.seis.fissuresUtil.mseed.FissuresConvert;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.time.ReduceTool;
import edu.sc.seis.seisFile.ChannelTimeWindow;
import edu.sc.seis.seisFile.SeisFileException;
import edu.sc.seis.seisFile.fdsnws.AbstractFDSNQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNDataSelectQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNDataSelectQueryParams;
import edu.sc.seis.seisFile.fdsnws.FDSNStationQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNStationQueryParams;
import edu.sc.seis.seisFile.fdsnws.FDSNWSException;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.DataAvailability;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.NetworkIterator;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationIterator;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.DataRecordIterator;
import edu.sc.seis.sod.BuildVersion;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.source.AbstractSource;
import edu.sc.seis.sod.source.network.FdsnStation;
import edu.sc.seis.sod.source.network.InstrumentationFromDB;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.source.network.WrappingNetworkSource;

public class FdsnDataSelect extends ConstantSeismogramSourceLocator implements SeismogramSourceLocator {

    private FDSNDataSelectQueryParams queryParams = new FDSNDataSelectQueryParams();

    private int timeoutMillis = 10 * 1000;


    FdsnStation fdsnStation = null;
    
    CoarseAvailableData availableData;

    private String username;

    private String password;

    public FdsnDataSelect() {
        super("DefaultFDSNDataSelect");
        timeoutMillis = 10 * 1000;
        username = "";
        password = "";
        checkFdsnStationLinkage();
    }

    public FdsnDataSelect(Element config) throws MalformedURLException, URISyntaxException {
        this(config, FDSNDataSelectQueryParams.IRIS_HOST);
    }

    public FdsnDataSelect(Element config, String defaultHost) throws MalformedURLException, URISyntaxException {
        super(config, "DefaultFDSNDataSelect", 2);

        int port = SodUtil.loadInt(config, "port", -1);
        if (port > 0) {
            queryParams.setPort(port);
        }
        String host = SodUtil.loadText(config, "host", defaultHost);
        if (host != null && host.length() != 0) {
            queryParams.setHost(host);
        }
        // mainly for beta testing
        String fdsnwsPath = SodUtil.loadText(config, "fdsnwsPath", null);
        if (fdsnwsPath != null && fdsnwsPath.length() != 0) {
            queryParams.setFdsnwsPath(fdsnwsPath);
        }
        username = SodUtil.loadText(config, "user", "");
        password = SodUtil.loadText(config, "password", "");
        timeoutMillis = 1000 * SodUtil.loadInt(config, "timeoutSecs", 10);

        checkFdsnStationLinkage();
    }
    
    private void checkFdsnStationLinkage() {
        NetworkSource wrappedNetSource = ((WrappingNetworkSource)Start.getNetworkArm().getNetworkSource());
        while (wrappedNetSource instanceof WrappingNetworkSource) {
            wrappedNetSource = ((WrappingNetworkSource)wrappedNetSource).getWrapped();
        }
        if (wrappedNetSource instanceof FdsnStation) {
            fdsnStation = (FdsnStation)wrappedNetSource;
        } else {
            logger.warn("Can't do FdsnStation Linkage, net source no FdsnStation: "+wrappedNetSource.getClass().getCanonicalName());
        }

        // check if username and password, and if so enable restricted on the network source
        if ( ! username.equals("") && ! password.equals("") && Start.getNetworkArm() != null) {
            logger.info("User and password set, so including restricted in FdsnStation network source");
            fdsnStation.includeRestricted(true);
        }
        if ( fdsnStation != null) {
            availableData = fdsnStation.getAvailableData();
        }
        if (availableData == null) {
            logger.warn("CoarseAvailableData is null, cannot do available data check.");
        }
    }
    
    public FdsnDataSelect(String host,int port, CoarseAvailableData fdsnStationAvailability) {
        super(host, 2);
        queryParams.setHost(host);
        queryParams.setPort(port);
        this.availableData = fdsnStationAvailability;
    }

    @Override
    public SeismogramSource getSeismogramSource() {
        return new SeismogramSource() {

            @Override
            public List<RequestFilter> availableData(List<RequestFilter> request) throws SeismogramSourceException {
                if ( availableData == null) {
                    return internalAvailableData(request);
                }
                List<RequestFilter> out = new ArrayList<RequestFilter>();
                for (RequestFilter rf : request) {
                    if (availableData.isCached(rf.channel_id)) {
                        List<MicroSecondTimeRange> avail = availableData.get(rf.channel_id);
                        MicroSecondTimeRange reqRange = new MicroSecondTimeRange(rf);
                        for (MicroSecondTimeRange range : avail) {
                            MicroSecondTimeRange intersect = reqRange.intersection(range);
                            if (intersect != null) {
                                out.add(new RequestFilter(rf.channel_id, intersect.getBeginTime().getFissuresTime(), intersect.getEndTime().getFissuresTime()));
                            }
                        }
                    } else {
                        //fdsnStation.getChannels(MockStation.)
                    }
                }
                return out;
            }

            @Override
            public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException {
                int count = 0;
                SeismogramSourceException latest = null;
                
                while (count == 0 || getRetryStrategy().shouldRetry(latest, this, count++)) {
                    try {
                        List<LocalSeismogramImpl> result = internalRetrieveData(request);
                        getRetryStrategy().serverRecovered(this);
                        return result;
                    } catch(SeismogramSourceException t) {
                        latest = t;
                        Throwable rootCause = AbstractFDSNQuerier.extractRootCause(t);
                        if (t.getCause() == null) {
                            throw t;
                        } else if (rootCause instanceof IOException) {
                            // try again on IOException
                        } else if (t.getCause() instanceof FDSNWSException && ((FDSNWSException)t.getCause()).getHttpResponseCode() != 200) {
                            // try again on IOException
                        } else {
                            throw t;
                        }
                    } catch(OutOfMemoryError e) {
                        throw new RuntimeException("Out of memory", e);
                    }
                }
                throw latest;
            }

            public List<RequestFilter> internalAvailableData(List<RequestFilter> request)
                    throws SeismogramSourceException {
                if ( availableData == null) {
                    return request;
                }
                
                try {
                    List<RequestFilter> out = new ArrayList<RequestFilter>();
                    if (request.size() != 0) {
                        FDSNStationQueryParams staQueryParams = new FDSNStationQueryParams(queryParams.getHost());
                        if (queryParams.getPort() > 0) {
                            staQueryParams.setPort(queryParams.getPort());
                        }
                        staQueryParams.setIncludeAvailability(true);
                        staQueryParams.setLevel("channel");
                        for (RequestFilter rf : request) {
                            ChannelId c = rf.channel_id;
                            staQueryParams.appendToNetwork(c.network_id.network_code);
                            staQueryParams.appendToStation(c.station_code);
                            staQueryParams.appendToLocation(c.site_code);
                            staQueryParams.appendToChannel(c.channel_code);
                        }
                        try {
                            logger.info("availavle data query: "+staQueryParams.formURI());
                        } catch(URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        FDSNStationQuerier querier = new FDSNStationQuerier(staQueryParams);
                        NetworkIterator nIt = querier.getFDSNStationXML().getNetworks();
                        while (nIt.hasNext()) {
                            Network n = nIt.next();
                            StationIterator sIt = n.getStations();
                            while (sIt.hasNext()) {
                                Station s = sIt.next();
                                List<Channel> chanList = s.getChannelList();
                                for (Channel channel : chanList) {
                                    DataAvailability da = channel.getDataAvailability();
                                    if (da != null && da.getExtent() != null) {
                                        out.add(new RequestFilter(new ChannelId(new NetworkId(n.getCode(),
                                                                                              new MicroSecondDate(n.getStartDate()).getFissuresTime()),
                                                                                s.getCode(),
                                                                                channel.getLocCode(),
                                                                                channel.getCode(),
                                                                                new MicroSecondDate(channel.getStartDate()).getFissuresTime()),
                                                                  new MicroSecondDate(da.getExtent().getStart()).getFissuresTime(),
                                                                  new MicroSecondDate(da.getExtent().getEnd()).getFissuresTime()));
                                    } else {
//                                        logger.info("No DataAvailability for "+n.getCode()+"."+s.getCode()+"."+
//                                                                                channel.getLocCode()+"."+
//                                                                                channel.getCode());
                                    }
                                }
                            }
                        }
                    }
                    return ReduceTool.trimTo(out, request);
                } catch(FDSNWSException e) {
                    throw new SeismogramSourceException(e);
                } catch(SeisFileException e) {
                    throw new SeismogramSourceException(e);
                } catch(XMLStreamException e) {
                    throw new SeismogramSourceException(e);
                }
            }

            public List<LocalSeismogramImpl> internalRetrieveData(List<RequestFilter> request)
                    throws SeismogramSourceException {
                List<LocalSeismogramImpl> out = new ArrayList<LocalSeismogramImpl>();
                if (request.size() != 0) {
                    FDSNDataSelectQueryParams newQueryParams = queryParams.clone();
                    List<ChannelTimeWindow> queryRequest = new ArrayList<ChannelTimeWindow>();
                    for (RequestFilter rf : request) {
                        ChannelId c = rf.channel_id;
                        queryRequest.add(new ChannelTimeWindow(c.network_id.network_code,
                                                               c.station_code,
                                                               c.site_code,
                                                               c.channel_code,
                                                               new MicroSecondDate(rf.start_time),
                                                               new MicroSecondDate(rf.end_time)));
                    }
                    List<DataRecord> drList = retrieveData(newQueryParams, queryRequest, getRetries());
                    try {
                        List<LocalSeismogramImpl> perRFList = FissuresConvert.toFissures(drList);
                        perRFList = Arrays.asList(ReduceTool.merge(perRFList.toArray(new LocalSeismogramImpl[0])));
                        for (LocalSeismogramImpl seis : perRFList) {
                            // the DataRecords know nothing about channel or
                            // network
                            // begin times, so use the request
                            for (RequestFilter rf : request) {
                                // find matching chan id
                                if (rf.channel_id.network_id.network_code.equals(seis.channel_id.network_id.network_code)) {
                                    seis.channel_id.network_id.begin_time = rf.channel_id.network_id.begin_time;
                                }
                                if (ChannelIdUtil.areEqualExceptForBeginTime(rf.channel_id, seis.channel_id)) {
                                    seis.channel_id.begin_time = rf.channel_id.begin_time;
                                    break;
                                }
                            }
                        }
                        out.addAll(perRFList);
                    } catch(SeisFileException e) {
                        throw new SeismogramSourceException(e);
                    } catch(FissuresException e) {
                        throw new SeismogramSourceException(e);
                    }
                }
                return out;
            }

            public List<DataRecord> retrieveData(FDSNDataSelectQueryParams queryParams,
                                                 List<ChannelTimeWindow> queryRequest,
                                                 int tryCount) throws SeismogramSourceException {
                List<DataRecord> drList = new ArrayList<DataRecord>();
                FDSNDataSelectQuerier querier = new FDSNDataSelectQuerier(queryParams, queryRequest);
                querier.setConnectTimeout(timeoutMillis);
                querier.setReadTimeout(timeoutMillis);
                String restrictedStr = "query: ";
                if (username != null && username.length() != 0 && password != null && password.length() != 0) {
                    querier.enableRestrictedData(username, password);
                    restrictedStr = "restricted "+restrictedStr;
                }
                try {
                    logger.info(restrictedStr+queryParams.formURI());
                } catch(URISyntaxException e) {
                    throw new SeismogramSourceException("Error with URL syntax", e);
                }
                querier.setUserAgent("SOD/" + BuildVersion.getVersion());
                try {
                    DataRecordIterator drIt = querier.getDataRecordIterator();
                    while (drIt.hasNext()) {
                        drList.add(drIt.next());
                    }
                } catch(FDSNWSException e) {
                    if (querier.getResponseCode() == 401 || querier.getResponseCode() == 403) {
                        throw new SeismogramAuthorizationException("Authorization failure to " + e.getTargetURI(), e);
                    } else {
                        throw new SeismogramSourceException(e);
                    }
                } catch(SeisFileException e) {
                    throw new SeismogramSourceException(e);
                } catch(SocketTimeoutException e) {
                    tryCount--;
                    logger.info("Timeout, will retry "+tryCount+" more times");
                    if (tryCount > 0) {
                        return retrieveData(queryParams, queryRequest, tryCount);
                    } else {
                        // not sure I like this...
                        throw new SeismogramSourceException("Retries exceeded", e);
                    }
                } catch(IOException e) {
                    throw new SeismogramSourceException(e);
                }
                return drList;
            }
        };
    }
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FdsnDataSelect.class);
}
