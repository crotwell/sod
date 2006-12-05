package edu.sc.seis.sod.tools;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;

public class find_responses extends find_stations {

    public find_responses(String[] args) throws JSAPException {
        super(args);
    }

    protected void addParams() throws JSAPException {
        super.addParams();
        add(createListOption("channels",
                             'c',
                             "channels",
                             "The codes of channels to retrieve.  If unspecified, all channels for retrieved sites will be retrieved"));
        add(createListOption("sites",
                             'l',
                             "sites",
                             "The codes of sites(location codes) to retrieve.  If unspecified, all sites for retrieved stations will be retrieved"));
        add(new FlaggedOption("responseDirectory",
                              JSAP.STRING_PARSER,
                              "responses",
                              false,
                              'o',
                              "output-directory",
                              "Directory to write responses to"));
    }

    public static void main(String[] args) throws Exception {
        CommandLineTool.run(new find_responses(args));
    }
}
