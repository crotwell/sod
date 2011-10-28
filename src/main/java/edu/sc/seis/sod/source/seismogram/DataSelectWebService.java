package edu.sc.seis.sod.source.seismogram;

import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.w3c.dom.Element;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ThreadSafeSimpleDateFormat;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.mseed.FissuresConvert;
import edu.sc.seis.fissuresUtil.time.RangeTool;
import edu.sc.seis.seisFile.MSeedQueryReader;
import edu.sc.seis.seisFile.dataSelectWS.DataSelectException;
import edu.sc.seis.seisFile.dataSelectWS.DataSelectReader;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;

public class DataSelectWebService implements SeismogramSourceLocator {

    public DataSelectWebService(Element config) throws MalformedURLException {
        this(config, DEFAULT_WS_URL);
    }

    public DataSelectWebService(Element config, String defaultURL) throws MalformedURLException {
        url = SodUtil.loadText(config, "url", defaultURL);
        String user = SodUtil.loadText(config, "user", "");
        String password = SodUtil.loadText(config, "password", "");
        if (user.length() != 0 && password.length() != 0) {
            Authenticator.setDefault(new MyAuthenticator(user, password));
        }
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
                    MSeedQueryReader dsReader = new DataSelectReader(url);
                    for (RequestFilter rf : request) {
                        MicroSecondDate start = new MicroSecondDate(rf.start_time);
                        String requestURL = dsReader.createQuery(rf.channel_id.network_id.network_code,
                                                              rf.channel_id.station_code,
                                                              rf.channel_id.site_code, 
                                                              rf.channel_id.channel_code, 
                                                              start,
                                                              (float)new MicroSecondDate(rf.end_time).subtract(start).getValue(UnitImpl.SECOND));
                        List<DataRecord> records = dsReader.read(requestURL);
                        List<DataRecord> contiguous = new ArrayList<DataRecord>();
                        DataRecord prev = null;
                        for (DataRecord dr : records) {
                            if (prev == null) {
                                contiguous.add(dr);
                                prev = dr;
                                continue;
                            }
                            // probably should also check for chan match
                            if (RangeTool.areContiguous(FissuresConvert.getTimeRange(prev.getHeader().getBtimeRange()), 
                                                        FissuresConvert.getTimeRange(dr.getHeader().getBtimeRange()),
                                                        FissuresConvert.convertSampleRate(prev).getPeriod())) {
                                prev = dr;
                                contiguous.add(dr);
                            } else {
                                LocalSeismogramImpl seis = FissuresConvert.toFissures(contiguous);
                                out.add(seis);
                                prev = dr;
                                contiguous.clear();
                                contiguous.add(dr);
                            }
                            
                        }
                    }
                    return out;
                } catch(IOException e) {
                    GlobalExceptionHandler.handle(e);
                    throw new FissuresException(e.getMessage(), new edu.iris.Fissures.Error(1, "IOException"));
                } catch(SeedFormatException e) {
                    GlobalExceptionHandler.handle(e);
                    throw new FissuresException(e.getMessage(), new edu.iris.Fissures.Error(1, "SeedFormatException")); 
                } catch(DataSelectException e) {
                    GlobalExceptionHandler.handle(e);
                    throw new FissuresException(e.getMessage(), new edu.iris.Fissures.Error(1, "DataSelectException")); 
                }
            }
        };
    }
    
    protected String url;

    public static final String DEFAULT_WS_URL = "http://www.iris.edu/ws/dataselect/query";
    
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
