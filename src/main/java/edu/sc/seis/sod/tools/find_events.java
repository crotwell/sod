package edu.sc.seis.sod.tools;

import org.apache.log4j.BasicConfigurator;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.Switch;

import edu.sc.seis.seisFile.client.BoxAreaParser;
import edu.sc.seis.seisFile.client.DonutParser;
import edu.sc.seis.seisFile.client.RangeParser;

public class find_events extends CommandLineTool {

    public find_events(String[] args) throws JSAPException {
        super(args);
    }

    protected void addParams() throws JSAPException {
        super.addParams();
        add(new Switch("allowDupes",
                       JSAP.NO_SHORTFLAG,
                       "allow-duplicates",
                       "Without this very similar events are rejected"));
        add(BoxAreaParser.createParam("Event constraining box as west/east/south/north"));
        add(DonutParser.createParam("Event constraining donut as lat/lon/minRadius/maxRadius"));
        add(TimeParser.createYesterdayParam("begin",
                                            "The earliest time for an accepted event, like -3d or 2015-01-26",
                                            false));
        add(TimeParser.createParam("end",
                                   "now",
                                   "The latest time for an accepted event, like -2d or 2015-01-29",
                                   true));
        add(RangeParser.createParam("magnitude",
                                    "0",
                                    "10",
                                    "The range of acceptable magnitudes"));
        add(createListOption("types",
                             't',
                             "types",
                             "The types of magnitudes to retrieve."));
        add(RangeParser.createParam("depth",
                                    "0",
                                    "10000",
                                    "The range of acceptable depths in kilometers",
                                    'D'));
        String latPrinter = "$event.getLatitude('##0.0000;-##0.0000')";
        String lonPrinter = "$event.getLongitude('##0.0000;-##0.0000')";
        String theRest = "$event.getDepth('###0.##') ${event.getTime('yyyy_MM_dd_HH_mm_ss_SSS')} $event.magnitudeValue$event.magnitudeType";
        add(OutputFormatParser.createParam(lonPrinter + " " + latPrinter + " "
                + theRest, latPrinter + " " + lonPrinter + " " + theRest));
        add(createListOption("catalogs",
                             'c',
                             "catalogs",
                             "A comma separated list of catalogs to search"));
        add(createListOption("contributors",
                             'C',
                             "contributors",
                             "A comma separated list of contributors to search"));
        add(createListOption("seismicRegions",
                             JSAP.NO_SHORTFLAG,
                             "seis-regions",
                             "A comma separated list of seismic regions"));
        add(createListOption("geographicRegions",
                             JSAP.NO_SHORTFLAG,
                             "geo-regions",
                             "A comma separated list of geographic regions"));
    }

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        CommandLineTool.run(new find_events(args));
    }
}
