package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;

public class find_seismograms extends CommandLineTool {

    public find_seismograms(String[] args) throws JSAPException {
        super(args);
        requiresStdin = true;
    }
    
    protected boolean requiresAtLeastOneArg(){
        return false;
    }

    protected void addParams() throws JSAPException {
        super.addParams();
        add(ServerParser.createParam("edu/iris/dmc/IRIS_DataCenter",
                                     "Set the seismogram server to use for this search"));
        add(PhaseTimeParser.createParam("begin",
                                        "-2ttp",
                                        "Phase name and offset for the seismogram's begin"));
        add(PhaseTimeParser.createParam("end",
                                        "+5tts",
                                        "Phase name and offset for the seismogram's end"));
        add(createListOption("channels",
                             'c',
                             "channels",
                             "The codes of channels to retrieve",
                             "BH*"));
        add(createListOption("sites",
                             'l',
                             "sites",
                             "The codes of sites(location codes) to retrieve", 
                             null,
                             new SiteCodeParser()));
        add(createListOption("mark-phases",
                             JSAP.NO_SHORTFLAG,
                             "mark-phases",
                             "Phase arrival times to record in the SAC t headers", 
                             null,
                             new SetSACParser()));
    }

    public static void main(String[] args) throws Exception {
        CommandLineTool.run(new find_seismograms(args));
    }
}
