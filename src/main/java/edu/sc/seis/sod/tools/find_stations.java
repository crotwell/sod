package edu.sc.seis.sod.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.VelocityContext;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAPException;

import edu.sc.seis.seisFile.client.BoxAreaParser;
import edu.sc.seis.seisFile.client.DonutParser;

public class find_stations extends CommandLineTool {

    public find_stations() throws JSAPException {
        this(new String[0]);
    }

    public find_stations(String[] args) throws JSAPException {
        super(args);
    }

    public VelocityContext getContext() {
        VelocityContext con = super.getContext();
        if((outputFormatFlag != null && !Boolean.FALSE.equals(result.getObject(outputFormatFlag.getID())))
                || needsStationAndSpecified()) {
            con.put("needsStationAND", Boolean.TRUE);
        }
        if (isSpecified(beginParam) || isSpecified(endParam)) {
            con.put("needsEffectiveTimeRange", Boolean.TRUE);
        } else {
            con.put("needsEffectiveTimeRange", Boolean.FALSE);
        }
        return con;
    }

    boolean needsStationAndSpecified() {
        Iterator it = needsStationAndIfSpecified.iterator();
        while(it.hasNext()) {
            FlaggedOption cur = (FlaggedOption)it.next();
            if(isSpecified(cur)) {
                return true;
            }
        }
        return false;
    }

    protected void addParams() throws JSAPException {
        addDefaultParams();
        String lonPrinter = "$station.getLongitude(' ##0.0000;-##0.0000')";
        String latPrinter = "$station.getLatitude(' ##0.0000;-##0.0000')";
        String theRest = "$station.getElevation('###0.') ${station.netCode}.${station.code}";
        outputFormatFlag = OutputFormatParser.createParam(lonPrinter + " "
                + latPrinter + " " + theRest, latPrinter + " " + lonPrinter
                + " " + theRest);
        add(outputFormatFlag);
    }

    protected void addDefaultParams() throws JSAPException {
        super.addParams();
        needsStationAndIfSpecified = new ArrayList();
        add(ServerParser.createParam("edu/iris/dmc/IRIS_NetworkDC",
                                     "The network server to use."));
        addStationParam(BoxAreaParser.createParam("A station constraining box as west/east/south/north"));
        addStationParam(DonutParser.createParam("A donut as lat/lon/minRadius/maxRadius"));
        addStationParam(createListOption("stations",
                                         's',
                                         "stations",
                                         "The codes of stations to retrieve"));
        add(createListOption("networks",
                             'n',
                             "networks",
                             "The codes of networks to retrieve"));
        beginParam = TimeParser.createParam("begin",
                                               null,
                                               "The earliest time to overlap an accepted station",
                                               false);
        addStationParam(beginParam);
        endParam = TimeParser.createParam("end",
                                               null,
                                               "The latest time to overlap an accepted station",
                                               true);
        addStationParam(endParam);
    }

    private void addStationParam(FlaggedOption option) throws JSAPException {
        needsStationAndIfSpecified.add(option);
        add(option);
    }

    protected FlaggedOption beginParam;
    protected FlaggedOption endParam;
    
    private List needsStationAndIfSpecified;

    protected FlaggedOption outputFormatFlag;

    public static void main(String[] args) throws Exception {
        CommandLineTool.run(new find_stations(args));
    }
}
