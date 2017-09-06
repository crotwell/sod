package edu.sc.seis.sod.subsetter.channel;

import java.io.FileNotFoundException;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.seisFile.fdsnws.stationxml.PolesZeros;
import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseStage;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.VelocityFileElementParser;
import edu.sc.seis.sod.util.convert.sac.StationXMLToSacPoleZero;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

/**
 * @author crotwell Created on Jul 19, 2005
 */
public class SacPoleZeroWriter implements ChannelSubsetter {

    public SacPoleZeroWriter(Element config) throws ConfigurationException {
        VelocityFileElementParser parser = new VelocityFileElementParser(config,
                                                                         DEFAULT_DIRECTORY,
                                                                         DEFAULT_TEMPLATE);
        template = parser.getTemplate();
        velocitizer = new PrintlineVelocitizer(new String[] {template});
    }

    public StringTree accept(Channel chan, NetworkSource network)
            throws Exception {
        Response response;
        try {
            response = network.getResponse(chan);
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch(InvalidResponse e) {
            logger.warn("Invalid instrumentation: ", e);
            return new Fail(this, "Invalid instrumentation: "+e.getMessage());
        }
        ResponseStage first = response.getFirstStage();
        if (first.getResponseItem() instanceof PolesZeros) {
            return new Fail(this, "first (sensor) stage is not a PoleZero: "+first.getResponseItem().getClass().getSimpleName());
        }
        String responseOut = StationXMLToSacPoleZero.convert(response)
                .toString();
        try {
            velocitizer.evaluate(template, responseOut, chan);
        } catch(FileNotFoundException fe) {
            GlobalExceptionHandler.handle("Error while writing response file for "
                                                  + ChannelIdUtil.toString(chan),
                                          fe);
            return new Fail(this, "Error while writing response file", fe);
        }
        return new Pass(this);
    }

    public static final String DEFAULT_DIRECTORY = "polezero/";

    public static final String DEFAULT_TEMPLATE = "${channel.codes}.${channel.getStart('yyyy_DDD_HH_mm_ss')}.sacpz";

    private String template;

    private PrintlineVelocitizer velocitizer;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SacPoleZeroWriter.class);
}
