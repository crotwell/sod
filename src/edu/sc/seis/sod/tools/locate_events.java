package edu.sc.seis.sod.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.martiansoftware.jsap.JSAPException;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

public class locate_events extends CommandLineTool {

    public locate_events(String[] args) throws JSAPException {
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
                                   df.format(now.subtract(new TimeInterval(2,
                                                                           UnitImpl.WEEK))),
                                   "The earliest time for an accepted event.  Must be in 'YYYY-MM-DD' format."));
        add(TimeParser.createParam("end",
                                   df.format(now),
                                   "The latest time for an accepted event.  Must be in 'YYYY-MM-DD' format."));
        add(RangeParser.createParam("magnitude",
                                    "0-10",
                                    "The range of acceptable magnitudes."));
        add(RangeParser.createParam("depth",
                                    "0-10000",
                                    "The range of acceptable depths in kilometers.",
                                    'D'));
        add(createListOption("catalogs",
                             'c',
                             "catalogs",
                             "A comma separated list of catalogs to search.  If unspecified, all catalogs will be searched"));
    }

    public static void main(String[] args) throws Exception {
        CommandLineTool.run(new locate_events(args));
    }
}
