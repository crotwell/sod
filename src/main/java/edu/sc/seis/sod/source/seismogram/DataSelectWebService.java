package edu.sc.seis.sod.source.seismogram;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.mseed.FissuresConvert;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;

public class DataSelectWebService implements SeismogramSource, SeismogramSourceLocator {

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

    @Override
    public SeismogramSource getSeismogramSource(CacheEvent event,
                                                ChannelImpl channel,
                                                RequestFilter[] infilters,
                                                CookieJar cookieJar) throws Exception {
        return this;
    }

    @Override
    public List<RequestFilter> available_data(List<RequestFilter> request) {
//bad fix this...
        return request;
    }

    @Override
    public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws FissuresException {
        try {
            List<LocalSeismogramImpl> out = new ArrayList<LocalSeismogramImpl>();
            for (RequestFilter rf : request) {
                String query = "net="+ rf.channel_id.network_id.network_code;
                query += "&sta=" + rf.channel_id.station_code;
                query += "&loc=" + rf.channel_id.site_code;
                query += "&cha=" + rf.channel_id.channel_code;
                query += "&start=" + longFormat.format(new MicroSecondDate(rf.start_time));
                query += "&end=" + longFormat.format(new MicroSecondDate(rf.end_time).add(new TimeInterval(1, UnitImpl.SECOND)));
                URL requestURL = new URL(url + "?"+query);
                HttpURLConnection conn = (HttpURLConnection)requestURL.openConnection();
                conn.connect();
                if (conn.getResponseCode() != 200) {
                    if (conn.getResponseCode() == 404) {
                        logger.debug("no data: "+requestURL);
                        return out;
                    } else {
                        throw new FissuresException("Did not get an OK repsonse code:"+conn.getResponseCode(), null);
                    }
                }
                BufferedInputStream bif = new BufferedInputStream(conn.getInputStream());
                DataInputStream in = new DataInputStream(bif);
                List<DataRecord> records = new ArrayList<DataRecord>();
                while (true) {
                    try {
                    records.add(DataRecord.read(in));
                    } catch (EOFException e) {
                        // end of data?
                        break;
                    }
                }
                LocalSeismogramImpl seis = FissuresConvert.toFissures(records.toArray(new DataRecord[0]));
                out.add(seis);
            }
            return out;
        } catch(IOException e) {
            GlobalExceptionHandler.handle(e);
            throw new FissuresException(e.getMessage(), new edu.iris.Fissures.Error(1, "IOException"));
       } catch(SeedFormatException e) {
            GlobalExceptionHandler.handle(e);
            throw new FissuresException(e.getMessage(), new edu.iris.Fissures.Error(1, "SeedFormatException")); 
        }
    }
    
    protected String url;

    public static final String DEFAULT_WS_URL = "http://www.iris.edu/ws/dataselect/query";
    
    public static SimpleDateFormat longFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DataSelectWebService.class);
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