package edu.sc.seis.sod.subsetter.dataCenter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.omg.CORBA.Object;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;

public class TryInOrderDataCenter implements SeismogramDCLocator {

    public TryInOrderDataCenter(Element config)
            throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                SodElement sodElement = (SodElement)SodUtil.load((Element)node,
                                                                 new String[] {"dataCenter"});
                if(sodElement instanceof SeismogramDCLocator) {
                    choices.add((SeismogramDCLocator)sodElement);
                }
            } // end of else
        }
    }

    public ProxySeismogramDC getSeismogramDC(CacheEvent event,
                                             Channel channel,
                                             RequestFilter[] infilters,
                                             CookieJar cookieJar)
            throws Exception {
        Iterator it = choices.iterator();
        ProxySeismogramDC[] out = new ProxySeismogramDC[choices.size()];
        int i = 0;
        while(it.hasNext()) {
            SeismogramDCLocator cur = (SeismogramDCLocator)it.next();
            out[i++] = cur.getSeismogramDC(event, channel, infilters, cookieJar);
        }
        return new TryInOrderDC(out);
    }

    private List choices = new ArrayList();
}

class TryInOrderDC implements ProxySeismogramDC {

    TryInOrderDC(ProxySeismogramDC[] dc) {
        this.dc = dc;
    }

    ProxySeismogramDC best;

    ProxySeismogramDC[] dc;

    void checkBestSet() {
        if(best == null) {
            throw new RuntimeException("Cannot call method until availableData is called to pick the best DC");
        }
    }

    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
        for(int i = 0; i < dc.length; i++) {
            RequestFilter[] out = new RequestFilter[0];
            try {
                out = dc[i].available_data(a_filterseq);
            } catch(org.omg.CORBA.SystemException e) {
                // Go on to next datacenter
            }
            if(out.length > 0) {
                best = dc[i];
                return out;
            }
        }
        best = dc[dc.length - 1];
        return new RequestFilter[0];
    }

    public Object getCorbaObject() {
        checkBestSet();
        return best.getCorbaObject();
    }

    public DataCenterOperations getWrappedDC() {
        checkBestSet();
        return best.getWrappedDC();
    }

    public DataCenterOperations getWrappedDC(Class wrappedClass) {
        checkBestSet();
        return best.getWrappedDC(wrappedClass);
    }

    public void reset() {
        checkBestSet();
        best.reset();
    }

    public void cancel_request(String a_request) throws FissuresException {
        checkBestSet();
        best.cancel_request(a_request);
    }

    public String queue_seismograms(RequestFilter[] a_filterseq)
            throws FissuresException {
        checkBestSet();
        return best.queue_seismograms(a_filterseq);
    }

    public String request_seismograms(RequestFilter[] a_filterseq,
                                      DataCenterCallBack a_client,
                                      boolean long_lived,
                                      Time expiration_time)
            throws FissuresException {
        checkBestSet();
        return best.request_seismograms(a_filterseq,
                                        a_client,
                                        long_lived,
                                        expiration_time);
    }

    public String request_status(String a_request) throws FissuresException {
        checkBestSet();
        return best.request_status(a_request);
    }

    public LocalSeismogram[] retrieve_queue(String a_request)
            throws FissuresException {
        checkBestSet();
        return best.retrieve_queue(a_request);
    }

    public LocalSeismogram[] retrieve_seismograms(RequestFilter[] a_filterseq)
            throws FissuresException {
        checkBestSet();
        return best.retrieve_seismograms(a_filterseq);
    }

    public String getFullName() {
        checkBestSet();
        return best.getFullName();
    }

    public String getServerDNS() {
        checkBestSet();
        return best.getServerDNS();
    }

    public String getServerName() {
        checkBestSet();
        return best.getServerName();
    }

    public String getServerType() {
        checkBestSet();
        return best.getServerType();
    }
}
