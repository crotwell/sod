package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.JSAPException;

public class find_seismograms extends CommandLineTool {

    public find_seismograms(String[] args) throws JSAPException {
        super(args);
    }

    protected void addParams() throws JSAPException {
        super.addParams();
        add(ServerParser.createParam("edu/iris/dmc/IRIS_BudDataCenter",
                                     "The seismogram server to use."));
        add(PhaseTimeParser.createParam("beginPhase",
                                        "-10origin",
                                        "The begin time for seismograms specified as a phase name and an offset in minutes like 12ttp or -3s."));
        add(PhaseTimeParser.createParam("endPhase",
                                        "20ttp",
                                        "The end time for seismograms specified as a phase name and an offset in minutes like 12ttp or -3s."));
        add(createListOption("channels",
                             'c',
                             "channels",
                             "The codes of channels to retrieve.  If unspecified, all channels for retrieved sites will be retrieved"));
        add(createListOption("sites",
                             'l',
                             "sites",
                             "The codes of sites(location codes) to retrieve.  If unspecified, all sites for retrieved stations will be retrieved"));
    }

    public static void main(String[] args) throws Exception {
        CommandLineTool.run(new find_seismograms(args));
    }
}
