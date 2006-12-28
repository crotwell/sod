package edu.sc.seis.sod.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.martiansoftware.jsap.JSAPException;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

public class find_events extends CommandLineTool {

    public find_events(String[] args) throws JSAPException {
        super(args);
    }

    protected void addParams() throws JSAPException {
        super.addParams();
        add(ServerParser.createParam("edu/iris/dmc/IRIS_EventDC",
                                     "The event server to use."));
        add(BoxAreaParser.createParam("A box the events must be in.  It's specified as west/east/north/south"));
        add(DonutParser.createParam("A donut the events must be in.  It's specified as centerLat/centerLon/minRadiusDegrees/maxRadiusDegrees"));
        MicroSecondDate now = ClockUtil.now();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        add(TimeParser.createParam("begin",
                                   df.format(now.subtract(new TimeInterval(1,
                                                                           UnitImpl.DAY))),
                                   "The earliest time for an accepted event.  Must be in 'YYYY-MM-DD' format."));
        add(TimeParser.createParam("end",
                                   "now",
                                   "The latest time for an accepted event.  Must be in 'YYYY-MM-DD' format or 'now' for the current time."));
        add(RangeParser.createParam("magnitude",
                                    "0-10",
                                    "The range of acceptable magnitudes."));
        add(createListOption("types",
                             't',
                             "types",
                             "The types of magnitudes to retrieve.  If unspecified, all magnitude types will be retrieved"));
        add(RangeParser.createParam("depth",
                                    "0-10000",
                                    "The range of acceptable depths in kilometers.",
                                    'D'));
        add(OutputFormatParser.createParam("$event.getLongitude('##0.0000;-##0.0000') $event.getLatitude('##0.0000;-##0.0000') $event.getDepth('###0.##') ${event.getTime('yyyy_DDD_HH_mm_sss')} $event.magnitudeValue$event.magnitudeType",
                                           "http://www.seis.sc.edu/sod/ingredients/event/origin/printline.html"));
        add(createListOption("catalogs",
                             'c',
                             "catalogs",
                             "A comma separated list of catalogs to search.  If unspecified, all catalogs will be searched"));
        add(createListOption("seismicRegions",
                             's',
                             "seismic-regions",
                             "A comma separated list of seismic Flinn-Engdahl regions.  An event must be in one of these regions to pass.  If unspecified, all regions will be acceptable"));
        add(createListOption("geographicRegions",
                             'g',
                             "geographic-regions",
                             "A comma separated list of geographic Flinn-Engdahl regions.  An event must be in one of these regions to pass.  If unspecified, all regions will be acceptable"));
    }

    public static void main(String[] args) throws Exception {
        CommandLineTool.run(new find_events(args));
    }
}
