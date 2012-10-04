package edu.sc.seis.sod.source.seismogram;

import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.w3c.dom.Element;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.RequestFilterUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ThreadSafeSimpleDateFormat;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.mseed.FissuresConvert;
import edu.sc.seis.seisFile.SeisFileException;
import edu.sc.seis.seisFile.dataSelectWS.BulkDataSelectReader;
import edu.sc.seis.seisFile.dataSelectWS.DataSelectReader;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.sod.BuildVersion;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;

public class DataSelectWebService implements SeismogramSourceLocator {

    public DataSelectWebService(Element config) throws MalformedURLException {
        this(config, DEFAULT_WS_URL);
    }

    public DataSelectWebService(Element config, String defaultURL) throws MalformedURLException {
        baseUrl = SodUtil.loadText(config, "url", defaultURL);
        String user = SodUtil.loadText(config, "user", "");
        String password = SodUtil.loadText(config, "password", "");
        if (user.length() != 0 && password.length() != 0) {
            Authenticator.setDefault(new MyAuthenticator(user, password));
        }
        timeoutMillis = 1000*SodUtil.loadInt(config, "timeoutSecs", 30);
    }

    public SeismogramSource getSeismogramSource(CacheEvent event,
                                                ChannelImpl channel,
                                                RequestFilter[] infilters,
                                                CookieJar cookieJar) throws Exception {
        return new SeismogramSource() {
    
            public List<RequestFilter> available_data(List<RequestFilter> request) {
//              bad fix this...
                return request;
            }

            public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws FissuresException {
                try {
                    List<LocalSeismogramImpl> out = new ArrayList<LocalSeismogramImpl>();
                    if (doBulk) {
                    if (request.size() != 0) {
                        BulkDataSelectReader dsReader = new BulkDataSelectReader(baseUrl, timeoutMillis);
                        dsReader.setUserAgent("SOD/"+BuildVersion.getVersion());
                        String query = "";
                        for (RequestFilter rf : request) {
                            MicroSecondDate start = new MicroSecondDate(rf.start_time);
                            query += dsReader.createQuery(rf.channel_id.network_id.network_code,
                                                          rf.channel_id.station_code,
                                                          rf.channel_id.site_code, 
                                                          rf.channel_id.channel_code, 
                                                          start,
                                                          new MicroSecondDate(rf.end_time));
                        }
                        List<DataRecord> records =  dsReader.read(query);
                        List<LocalSeismogramImpl> perRFList = FissuresConvert.toFissures(records);
                        RequestFilter rf = request.get(0);
                        for (LocalSeismogramImpl seis : perRFList) {
                            // the DataRecords know nothing about channel or network begin times, so use the request
                            seis.channel_id.begin_time = rf.channel_id.begin_time;
                            seis.channel_id.network_id.begin_time = rf.channel_id.network_id.begin_time;
                        }
                        out.addAll(perRFList);
                    }
                    } else {
                        // nonbulk web service (uses more DMC resources, but easier as is GET instead of POST
                        DataSelectReader dsReader = new DataSelectReader(baseUrl, timeoutMillis);
                        dsReader.setUserAgent("SOD/"+BuildVersion.getVersion());
                        for (RequestFilter rf : request) {
                            MicroSecondDate start = new MicroSecondDate(rf.start_time);
                            List<DataRecord> records =  dsReader.read(rf.channel_id.network_id.network_code,
                                                                  rf.channel_id.station_code,
                                                                  rf.channel_id.site_code, 
                                                                  rf.channel_id.channel_code, 
                                                                  start,
                                                                  new MicroSecondDate(rf.end_time));
                            for (DataRecord dr : records) {
                                if ( ! (rf.channel_id.network_id.network_code.equals(dr.getHeader().getNetworkCode().trim()) &&
                                        rf.channel_id.station_code.equals(dr.getHeader().getStationIdentifier().trim()) &&
                                        rf.channel_id.site_code.equals(dr.getHeader().getLocationIdentifier()) &&
                                        rf.channel_id.channel_code.equals(dr.getHeader().getChannelIdentifier().trim()))) {
                                    throw new RuntimeException("Request: "+RequestFilterUtil.toString(rf)+" did not return matching data: "+dr.toString());
                                }
                            }
                            List<LocalSeismogramImpl> perRFList = FissuresConvert.toFissures(records);
                            for (LocalSeismogramImpl seis : perRFList) {
                                // the DataRecords know nothing about channel or network begin times, so use the request
                                seis.channel_id.begin_time = rf.channel_id.begin_time;
                                seis.channel_id.network_id.begin_time = rf.channel_id.network_id.begin_time;
                            }
                            out.addAll(perRFList);
                        }
                    }
                    return out;
                } catch(IOException e) {
                    GlobalExceptionHandler.handle(e);
                    throw new FissuresException(e.getMessage(), new edu.iris.Fissures.Error(1, "IOException"));
                } catch(SeedFormatException e) {
                    GlobalExceptionHandler.handle(e);
                    throw new FissuresException(e.getMessage(), new edu.iris.Fissures.Error(1, "SeedFormatException")); 
                } catch(SeisFileException e) {
                    GlobalExceptionHandler.handle(e);
                    throw new FissuresException(e.getMessage(), new edu.iris.Fissures.Error(1, "DataSelectException")); 
                }
            }
        };
    }
    
    protected int timeoutMillis;
    
    protected String baseUrl;

    protected boolean doBulk = true;
    public static final String DEFAULT_WS_URL = BulkDataSelectReader.DEFAULT_WS_URL;
  //  public static final String DEFAULT_WS_URL = DataSelectReader.DEFAULT_WS_URL;
    
    public static ThreadSafeSimpleDateFormat longFormat = new ThreadSafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", TimeZone.getTimeZone("GMT"));

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataSelectWebService.class);
}

class MyAuthenticator extends Authenticator {
    String user;
    String password;
    public MyAuthenticator(String user, String password) {
        this.user = user;
        this.password = password;
}
    public PasswordAuthentication getPasswordAuthentication () {
        return new PasswordAuthentication (user, password.toCharArray());
    }
}
