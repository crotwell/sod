package edu.sc.seis.sod.subsetter.request;

import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

public class NetDCRequest extends BreqFastRequest {

    public NetDCRequest(Element config) throws ConfigurationException {
        super(config);
    }

    protected void insertRequest(Channel channel,
                                 RequestFilter[] request,
                                 Writer out,
                                 int i) throws IOException {
        Instant start = request[i].startTime;
        Instant end = request[i].endTime;
        out.write(".DATA * " + channel.getNetworkCode() + " "
                + channel.getStationCode() + " "
                + channel.getLocCode() + " "
                + channel.getCode() + " "
                + netDCTimeFormat.format(start) + " " 
                + netDCTimeFormat.format(end)
                + nl);
    }

    protected void insertEventHeader(CacheEvent event, Writer out, String label)
            throws IOException {
        out.write(".NETDC_REQUEST" + nl);
        insert(out, "name");
        insert(out, "inst");
        optInsert(out, "mail");
        insert(out, "email");
        optInsert(out, "phone");
        optInsert(out, "fax");
        out.write(".LABEL " + label + nl);
        optInsert(out, "media");
        optInsert(out, "altmedia", "ALTERNATIVE MEDIA");
        optInsert(out, "format_waveform");
        optInsert(out, "format_response");
        optInsert(out, "merge_data");
        optInsert(out, "disposition");
        out.write(".END" + nl);
        out.write(nl);
    }
    
    protected String getDefaultWorkingDir(){
        return "netdc";
    }
    
    protected String getDefaultFileTemplate(){
        return "${event.getTime('yyyy.DDD.HH.mm.ss.SSSS')}.netdc";
    }
    
    private DateTimeFormatter netDCTimeFormat = TimeUtils.createFormatter("'\"'yyyy MM dd HH mm ss.SSSS'\"'");
}
