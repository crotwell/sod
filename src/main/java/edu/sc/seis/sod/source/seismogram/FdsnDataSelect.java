package edu.sc.seis.sod.source.seismogram;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Element;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.mseed.FissuresConvert;
import edu.sc.seis.fissuresUtil.time.ReduceTool;
import edu.sc.seis.seisFile.ChannelTimeWindow;
import edu.sc.seis.seisFile.SeisFileException;
import edu.sc.seis.seisFile.fdsnws.FDSNDataSelectQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNDataSelectQueryParams;
import edu.sc.seis.seisFile.fdsnws.FDSNWSException;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.DataRecordIterator;
import edu.sc.seis.sod.BuildVersion;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.AbstractSource;

public class FdsnDataSelect extends AbstractSource implements SeismogramSourceLocator {

    private String host = FDSNDataSelectQueryParams.IRIS_HOST;

    private int port = -1;

    private int timeoutMillis = 10 * 1000;

    private boolean doBulk = false;

    private String username;

    private String password;

    public FdsnDataSelect() {
        super("DefaultFDSNDataSelect");
        host = FDSNDataSelectQueryParams.IRIS_HOST;
        timeoutMillis = 10 * 1000;
        doBulk = false;
        username = "";
        password = "";
    }

    public FdsnDataSelect(Element config) throws MalformedURLException, URISyntaxException {
        this(config, FDSNDataSelectQueryParams.IRIS_HOST);
    }

    public FdsnDataSelect(Element config, String defaultHost) throws MalformedURLException, URISyntaxException {
        super(config, "DefaultFDSNDataSelect", 2);
        doBulk = SodUtil.isTrue(config, "dobulk", true);
        host = SodUtil.loadText(config, "host", defaultHost);
        port = SodUtil.loadInt(config, "port", -1);
        username = SodUtil.loadText(config, "user", "");
        password = SodUtil.loadText(config, "password", "");
        timeoutMillis = 1000 * SodUtil.loadInt(config, "timeoutSecs", 10);
    }

    @Override
    public SeismogramSource getSeismogramSource(CacheEvent event,
                                                ChannelImpl channel,
                                                RequestFilter[] infilters,
                                                CookieJar cookieJar) throws Exception {
        return new SeismogramSource() {

            @Override
            public List<RequestFilter> available_data(List<RequestFilter> request) {
                return request; // no-op,
            }

            @Override
            public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException {
                int count = 0;
                SeismogramSourceException latest;
                try {
                    return internalRetrieveData(request);
                } catch(OutOfMemoryError e) {
                    throw new RuntimeException("Out of memory", e);
                } catch(SeismogramSourceException t) {
                    if (t.getCause() instanceof IOException 
                            || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                        latest = t;
                    } else {
                        throw new RuntimeException(t);
                    }
                }
                while(getRetryStrategy().shouldRetry(latest, this, count++)) {
                    try {
                        List<LocalSeismogramImpl> result = internalRetrieveData(request);
                        getRetryStrategy().serverRecovered(this);
                        return result;
                    } catch(SeismogramSourceException t) {
                        if (t.getCause() instanceof IOException 
                                || (t.getCause() != null && t.getCause().getCause() instanceof IOException)) {
                            latest = t;
                        } else {
                            throw new RuntimeException(t);
                        }
                    } catch(OutOfMemoryError e) {
                        throw new RuntimeException("Out of memory", e);
                    }
                }
                throw new RuntimeException(latest);
            }
            
            public List<LocalSeismogramImpl> internalRetrieveData(List<RequestFilter> request) throws SeismogramSourceException {
                List<LocalSeismogramImpl> out = new ArrayList<LocalSeismogramImpl>();
                if (request.size() != 0) {
                    FDSNDataSelectQueryParams queryParams = new FDSNDataSelectQueryParams(host);
                    if (port > 0) {
                        queryParams.setPort(port);
                    }
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
                    List<DataRecord> drList = retrieveData(queryParams, queryRequest, getRetries());
                    try {
                    List<LocalSeismogramImpl> perRFList = FissuresConvert.toFissures(drList);
                    perRFList = Arrays.asList(ReduceTool.merge(perRFList.toArray(new LocalSeismogramImpl[0])));
                    for (LocalSeismogramImpl seis : perRFList) {
                        // the DataRecords know nothing about channel or network
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
                    } catch (FissuresException e) {
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
                if (username != null && username.length() != 0 && password != null && password.length() != 0) {
                    querier.enableRestrictedData(username, password);
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
                    System.out.println("*** TIMEOUT RETRY: "+tryCount+" left");
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
}
