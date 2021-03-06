package edu.sc.seis.sod.subsetter.station;

import java.io.IOException;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.FilterNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.FilterNetworkDC;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

/**
 * @author groves Created on Mar 4, 2005
 */
public class StationRegularExpression implements StationSubsetter {

    public StationRegularExpression(Element el) throws IOException {
        if (DOMHelper.hasElement(el, "url")) {
            String url = DOMHelper.extractText(el, "url");
            patterns = FilterNetworkDC.readPattern(url);
        } else if (DOMHelper.hasElement(el, "code")) {
            patterns = new Pattern[] { Pattern.compile(SodUtil.getNestedText(DOMHelper.getElement(el, "code"))) };
        }
    }

    public StringTree accept(StationImpl station, NetworkSource network) throws Exception {
        for(int i = 0; i < patterns.length; i++) {
            if(patterns[i].matcher(FilterNetworkAccess.getStationString(station.get_id()))
                    .matches()) { return new Pass(this); }
        }
        return new Fail(this);
    }

    private Pattern[] patterns;
}