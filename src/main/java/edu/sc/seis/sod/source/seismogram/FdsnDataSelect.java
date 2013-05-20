package edu.sc.seis.sod.source.seismogram;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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
import edu.sc.seis.seisFile.ChannelTimeWindow;
import edu.sc.seis.seisFile.SeisFileException;
import edu.sc.seis.seisFile.fdsnws.FDSNDataSelectQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNDataSelectQueryParams;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.DataRecordIterator;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;

public class FdsnDataSelect implements SeismogramSourceLocator {

    private String host = FDSNDataSelectQueryParams.IRIS_HOST;

    private int timeoutMillis = 30 * 1000;

    private boolean doBulk = false;

    private String username;
    
    private String password;

    public FdsnDataSelect() {
        host = FDSNDataSelectQueryParams.IRIS_HOST;
        timeoutMillis = 30 * 1000;
        doBulk = false;
        username = "";
        password = "";
    }

    public FdsnDataSelect(Element config) throws MalformedURLException, URISyntaxException {
        this(config, FDSNDataSelectQueryParams.IRIS_HOST);
    }

    public FdsnDataSelect(Element config, String defaultURL) throws MalformedURLException, URISyntaxException {
        doBulk = SodUtil.isTrue(config, "dobulk", true);
        host = SodUtil.loadText(config, "host", FDSNDataSelectQueryParams.IRIS_HOST);
        username = SodUtil.loadText(config, "user", "");
        password = SodUtil.loadText(config, "password", "");
        timeoutMillis = 1000 * SodUtil.loadInt(config, "timeoutSecs", 30);
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
                List<LocalSeismogramImpl> out = new ArrayList<LocalSeismogramImpl>();
                if (request.size() != 0) {
                    FDSNDataSelectQueryParams queryParams = new FDSNDataSelectQueryParams(host);
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
                    FDSNDataSelectQuerier querier = new FDSNDataSelectQuerier(queryParams, queryRequest);
                    if (username.length() != 0 && password.length() != 0) {
                        querier.enableRestrictedData(username, password);
                    }
                    List<DataRecord> drList = new ArrayList<DataRecord>();
                    try {
                    DataRecordIterator drIt = querier.getDataRecordIterator();
                    while (drIt.hasNext()) {
                        drList.add(drIt.next());
                    }
                    List<LocalSeismogramImpl> perRFList = FissuresConvert.toFissures(drList);
                    for (LocalSeismogramImpl seis : perRFList) {
                        // the DataRecords know nothing about channel or network
                        // begin times, so use the request
                        for (RequestFilter rf : request) {
                            // find matching chan id
                            if (ChannelIdUtil.areEqualExceptForBeginTime(rf.channel_id, seis.channel_id)) {
                                seis.channel_id.begin_time = rf.channel_id.begin_time;
                                seis.channel_id.network_id.begin_time = rf.channel_id.network_id.begin_time;
                            }
                        }
                    }
                    out.addAll(perRFList);
                    } catch(FissuresException e) {
                        throw new SeismogramSourceException(e);
                    } catch(SeisFileException e) {
                        throw new SeismogramSourceException(e);
                    } catch(IOException e) {
                        throw new SeismogramSourceException(e);
                    }
                }
                return out;
            }
        };
    }
}
