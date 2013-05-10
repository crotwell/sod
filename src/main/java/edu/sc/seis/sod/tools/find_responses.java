package edu.sc.seis.sod.tools;

import org.apache.velocity.VelocityContext;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;

import edu.sc.seis.sod.subsetter.channel.ResponseWriter;
import edu.sc.seis.sod.subsetter.channel.SacPoleZeroWriter;

public class find_responses extends find_stations {

    public find_responses(String[] args) throws JSAPException {
        super(args);
    }

    public VelocityContext getContext() {
        VelocityContext con = super.getContext();
        if(result.getString(FILE_TEMPLATE_OPTION).equals(DEFAULT_TEMPLATE)) {
            if(result.getString("type").equals("polezero")) {
                con.put(FILE_TEMPLATE_OPTION, SacPoleZeroWriter.DEFAULT_DIRECTORY
                        + SacPoleZeroWriter.DEFAULT_TEMPLATE);
            } else {
                con.put(FILE_TEMPLATE_OPTION, ResponseWriter.DEFAULT_DIRECTORY
                        + ResponseWriter.DEFAULT_TEMPLATE);
            }
        }
        return con;
    }

    protected void addParams() throws JSAPException {
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
        add(new FlaggedOption(FILE_TEMPLATE_OPTION,
                              JSAP.STRING_PARSER,
                              DEFAULT_TEMPLATE,
                              false,
                              'f',
                              "filename",
                              "Filename template for responses"));
        add(new FlaggedOption("type",
                              new ResponseFormatParser(),
                              "polezero",
                              false,
                              't',
                              "type",
                              "The type of responses to write out.  Can be polezero or resp"));
    }

    private static final String FILE_TEMPLATE_OPTION = "fileTemplate";

    private static final String DEFAULT_TEMPLATE = "DEFAULT_TEMPLATE";

    public static void main(String[] args) throws Exception {
        CommandLineTool.run(new find_responses(args));
    }
}
