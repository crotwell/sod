package edu.sc.seis.sod.tools;

import org.apache.velocity.VelocityContext;

import com.martiansoftware.jsap.JSAPException;

public class find_channels extends find_stations {

    public find_channels() throws JSAPException {
        this(new String[0]);
    }

    public find_channels(String[] args) throws JSAPException {
        super(args);
    }

    public VelocityContext getContext() {
        VelocityContext con = super.getContext();
        // override needsStationAND as output is only for channels
        if(needsStationAndSpecified()) {
            con.put("needsStationAND", Boolean.TRUE);
        } else {
            con.put("needsStationAND", Boolean.FALSE);
        }
        return con;
    }

    protected void addParams() throws JSAPException {
        addDefaultParams();
        String lonPrinter = "$station.getLongitude(' ##0.0000;-##0.0000')";
        String latPrinter = "$station.getLatitude(' ##0.0000;-##0.0000')";
        String theRest = "$station.getElevation('###0.') ${network.code}.${station.code}.${site.code}.$channel.code $channel.azimuth $channel.dip $channel.getStart('yyyy-MM-dd') $channel.getEnd('yyyy-MM-dd')";
        outputFormatFlag = OutputFormatParser.createParam(lonPrinter + " "
                + latPrinter + " " + theRest, latPrinter + " " + lonPrinter
                + " " + theRest);
        add(outputFormatFlag);
    }

    protected void addDefaultParams() throws JSAPException {
        super.addDefaultParams();

        add(createListOption("sites",
                             'l',
                             "sites",
                             "The codes of sites(location codes) to retrieve",
                             null,
                             new SiteCodeParser()));
        add(createListOption("channels",
                             'c',
                             "channels",
                             "The codes of channels to retrieve",
                             "BH*"));
    }

    public static void main(String[] args) throws Exception {
        CommandLineTool.run(new find_channels(args));
    }
}
