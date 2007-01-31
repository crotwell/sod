package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.JSAPException;

public class find_seismograms extends CommandLineTool {

    public find_seismograms(String[] args) throws JSAPException {
        super(args);
        requiresStdin = true;
    }

    protected void addParams() throws JSAPException {
        super.addParams();
        add(ServerParser.createParam("edu/iris/dmc/IRIS_BudDataCenter",
                                     "The seismogram server to use."));
        add(PhaseTimeParser.createParam("begin",
                                        "-10origin",
                                        "Phase name and offset for the seismogram's begin"));
        add(PhaseTimeParser.createParam("end",
                                        "20ttp",
                                        "Phase name and offset for the seismogram's end"));
        add(createListOption("channels",
                             'c',
                             "channels",
                             "The codes of channels to retrieve"));
        add(createListOption("sites",
                             'l',
                             "sites",
                             "The codes of sites(location codes) to retrieve"));
    }

    public static void main(String[] args) throws Exception {
        CommandLineTool.run(new find_seismograms(args));
    }
}
