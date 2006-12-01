package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.JSAPException;

public class locate_stations extends CommandLineTool {

    public locate_stations() throws JSAPException {
        this(new String[0]);
    }

    public locate_stations(String[] args) throws JSAPException {
        super(args);
    }

    protected void addParams() throws JSAPException {
        super.addParams();
        add(ServerParser.createParam("edu/iris/dmc/IRIS_NetworkDC",
                                     "The network server to use."));
        add(BoxAreaParser.createParam("A box the stations must be in.  It's specified as west/east/north/south"));
        add(DonutParser.createParam("A donut the stations must be in.  It's specified as centerLat/centerLon/minRadiusDegrees/maxRadiusDegrees"));
        add(createListOption("stations",
                             's',
                             "stations",
                             "The codes of stations to retrieve.  If unspecified, all stations for retrieved networks will be retrieved"));
        add(createListOption("networks",
                             'n',
                             "networks",
                             "The codes of networks to retrieve.  If unspecified, all networks will be retrieved"));
    }

    public static void main(String[] args) throws Exception {
        CommandLineTool.run(new locate_stations(args));
    }
}
