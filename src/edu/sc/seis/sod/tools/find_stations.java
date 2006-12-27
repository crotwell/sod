package edu.sc.seis.sod.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.velocity.VelocityContext;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAPException;

public class find_stations extends CommandLineTool {

    public find_stations() throws JSAPException {
        this(new String[0]);
    }

    public find_stations(String[] args) throws JSAPException {
        super(args);
    }

    public VelocityContext getContext() {
        VelocityContext con = super.getContext();
        if(!Boolean.FALSE.equals(result.getObject(outputFormatFlag.getID()))
                || needsStationAndSpecified()) {
            con.put("needsStationAND", Boolean.TRUE);
        }
        return con;
    }

    private boolean needsStationAndSpecified() {
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
        super.addParams();
        needsStationAndIfSpecified = new ArrayList();
        add(ServerParser.createParam("edu/iris/dmc/IRIS_NetworkDC",
                                     "The network server to use."));
        addStationParam(BoxAreaParser.createParam("A box the stations must be in.  It's specified as west/east/north/south"));
        addStationParam(DonutParser.createParam("A donut the stations must be in.  It's specified as centerLat/centerLon/minRadiusDegrees/maxRadiusDegrees"));
        addStationParam(createListOption("stations",
                                         's',
                                         "stations",
                                         "The codes of stations to retrieve.  If unspecified, all stations for retrieved networks will be retrieved"));
        add(createListOption("networks",
                             'n',
                             "networks",
                             "The codes of networks to retrieve.  If unspecified, all networks will be retrieved"));
        outputFormatFlag = OutputFormatParser.createParam("$station.getLongitude(' ##0.0000;-##0.0000') $station.getLatitude(' ##0.0000;-##0.0000') $station.getElevation('###0.') $station.code",
                                                          "http://www.seis.sc.edu/sod/ingredients/event/origin/printline.html");
        add(outputFormatFlag);
    }

    private void addStationParam(FlaggedOption option) throws JSAPException {
        needsStationAndIfSpecified.add(option);
        add(option);
    }

    private List needsStationAndIfSpecified;

    private FlaggedOption outputFormatFlag;

    public static void main(String[] args) throws Exception {
        CommandLineTool.run(new find_stations(args));
    }
}
