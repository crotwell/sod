package edu.sc.seis.sod.subsetter.request;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.ConfigurationException;

public class NetDCRequest extends BreqFastRequest {

    public NetDCRequest(Element config) throws ConfigurationException {
        super(config);
        netDCTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    protected void insertRequest(Channel channel,
                                 RequestFilter[] request,
                                 Writer out,
                                 int i) throws IOException {
        MicroSecondDate start = new MicroSecondDate(request[i].start_time);
        MicroSecondDate end = new MicroSecondDate(request[i].end_time);
        out.write(".DATA * " + channel.get_id().network_id.network_code + " "
                + channel.get_id().station_code + " "
                + channel.get_id().site_code + " "
                + channel.get_code() + " "
                + netDCTimeFormat.format(start) + " " 
                + netDCTimeFormat.format(end)
                + nl);
    }

    protected void insertEventHeader(EventAccessOperations event, Writer out, String label)
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
    
    private DateFormat netDCTimeFormat = new SimpleDateFormat("'\"'yyyy MM dd HH mm ss.SSSS'\"'");
}
