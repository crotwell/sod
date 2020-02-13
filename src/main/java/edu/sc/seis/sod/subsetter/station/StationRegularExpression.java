package edu.sc.seis.sod.subsetter.station;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathException;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.station.StationIdUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

/**
 * @author groves Created on Mar 4, 2005
 */
public class StationRegularExpression implements StationSubsetter {

    public StationRegularExpression(Element el) throws IOException, XPathException {
        if (DOMHelper.hasElement(el, "url")) {
            String url = DOMHelper.extractText(el, "url");
            patterns = readPattern(url);
        } else if (DOMHelper.hasElement(el, "code")) {
            patterns = new Pattern[] { Pattern.compile(SodUtil.getNestedText(DOMHelper.getElement(el, "code"))) };
        }
    }

    public StringTree accept(Station station, NetworkSource network) throws Exception {
        for(int i = 0; i < patterns.length; i++) {
            if(patterns[i].matcher(StationIdUtil.toStringNoDates(station))
                    .matches()) { return new Pass(this); }
        }
        return new Fail(this);
    }

    public static Pattern[] readPattern(String filterURL) throws IOException {
        InputStream filterStream = new URL(filterURL.trim()).openStream();
        Reader reader = new BufferedReader(new InputStreamReader(filterStream));
        int curInt;
        StringBuffer buff = new StringBuffer();
        List gottenPatterns = new ArrayList();
        while((curInt = reader.read()) != -1) {
            char curChar = (char)curInt;
            if(curChar != '\n') {
                buff.append(curChar);
            } else {
                gottenPatterns.add(Pattern.compile(buff.toString()));
                buff = new StringBuffer();
            }
        }
        return (Pattern[])gottenPatterns.toArray(new Pattern[0]);
    }
    
    private Pattern[] patterns;
}