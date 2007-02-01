package edu.sc.seis.sod.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;

public class find_responses extends find_stations {

    public find_responses(String[] args) throws JSAPException {
        super(args);
    }

    protected void addParams() throws JSAPException {
        super.addParams();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        add(TimeParser.createParam("begin",
                                   df.format(new Date(0)),
                                   "The earliest time for an accepted channel"));
        add(TimeParser.createParam("end",
                                   "now",
                                   "The latest time for an accepted channel"));
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
        add(new FlaggedOption("responseDirectory",
                              JSAP.STRING_PARSER,
                              "responses",
                              false,
                              'D',
                              "directory",
                              "Directory to write responses to"));
        add(new FlaggedOption("format",
                              new ResponseFormatParser(),
                              "polezero",
                              false,
                              'f',
                              "format",
                              "The format to store responses in.  Can be polezero or resp"));
    }

    public static void main(String[] args) throws Exception {
        CommandLineTool.run(new find_responses(args));
    }
}
