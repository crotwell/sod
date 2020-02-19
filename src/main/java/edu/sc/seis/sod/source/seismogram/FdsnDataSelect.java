package edu.sc.seis.sod.source.seismogram;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.ChannelTimeWindow;
import edu.sc.seis.seisFile.SeisFileException;
import edu.sc.seis.seisFile.fdsnws.AbstractFDSNQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNDataSelectQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNDataSelectQueryParams;
import edu.sc.seis.seisFile.fdsnws.FDSNWSException;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.DataRecordIterator;
import edu.sc.seis.sod.BuildVersion;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.source.event.FdsnEvent;
import edu.sc.seis.sod.source.network.FdsnStation;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.source.network.WrappingNetworkSource;
import edu.sc.seis.sod.util.convert.mseed.FissuresConvert;
import edu.sc.seis.sod.util.time.RangeTool;
import edu.sc.seis.sod.util.time.ReduceTool;

public class FdsnDataSelect extends ConstantSeismogramSourceLocator implements SeismogramSourceLocator {

    private FDSNDataSelectQueryParams queryParams = new FDSNDataSelectQueryParams();

    private int timeoutMillis = 10 * 1000;

    FdsnStation fdsnStation = null;

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
        if (username != null && ! username.equals("") && password != null && Start.getNetworkArm() != null) {
            logger.info("User and password set, so including restricted in FdsnStation network source");
            fdsnStation.includeRestricted(true);
        }
    }
    
    public FdsnDataSelect(String host,int port) {
        super(host, 2);
        queryParams.setHost(host);
        queryParams.setPort(port);
    }

    @Override
    public SeismogramSource getSeismogramSource() {
        return new SeismogramSource() {

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

            public List<LocalSeismogramImpl> internalRetrieveData(List<RequestFilter> request)
                    throws SeismogramSourceException {
                List<LocalSeismogramImpl> out = new ArrayList<LocalSeismogramImpl>();
                if (request.size() != 0) {
                    FDSNDataSelectQueryParams newQueryParams = queryParams.clone();
                    List<ChannelTimeWindow> queryRequest = new ArrayList<ChannelTimeWindow>();
                    for (RequestFilter rf : request) {
                        ChannelId c = rf.channelId;
                        queryRequest.add(new ChannelTimeWindow(c.getNetworkCode(),
                                                               c.getStationCode(),
                                                               c.getLocCode(),
                                                               c.getChannelCode(),
                                                               rf.startTime,
                                                               rf.endTime));
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
                                if (RangeTool.seisPartOfRequest(rf, seis)) {
                                    seis.channel_id = rf.getChannelId();
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

                    } else if (querier.getResponseCode() == 400) {
                        // badly formed query, cowardly quit
                        Start.simpleArmFailure(Start.getWaveformArmArray()[0], 
                                               FdsnEvent.BAD_PARAM_MESSAGE+" "+((FDSNWSException)e).getMessage()+" on "+((FDSNWSException)e).getTargetURI());
                        throw new SeismogramSourceException(e);
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
