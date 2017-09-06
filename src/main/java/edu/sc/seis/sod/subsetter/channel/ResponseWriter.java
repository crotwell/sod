package edu.sc.seis.sod.subsetter.channel;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.bag.ResponsePrint;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.VelocityFileElementParser;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

public class ResponseWriter implements ChannelSubsetter {

    public ResponseWriter(Element config) throws ConfigurationException {
        VelocityFileElementParser parser = new VelocityFileElementParser(config,
                                                                         DEFAULT_DIRECTORY,
                                                                         DEFAULT_TEMPLATE);
        template = parser.getTemplate();
        velocitizer = new PrintlineVelocitizer(new String[] {template});
    }

    public StringTree accept(Channel chan, NetworkSource network)
            throws Exception {
        try {
            String response = ResponsePrint.printResponse(ChannelId.of(chan), network.getResponse(chan), new TimeRange(chan));
            velocitizer.evaluate(template, response, chan);
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch (InvalidResponse e) {
            return new Fail(this, "Invalid instrumentation: "+e.getMessage());
        } catch(FileNotFoundException fe) {
            GlobalExceptionHandler.handle("Error while writing response file for "
                    + ChannelIdUtil.toString(chan), fe);
            return new Fail(this, "Error while writing response file", fe);
        }
        return new Pass(this);
    }
    
    public static final String DEFAULT_DIRECTORY = "responses/";

    public static final String DEFAULT_TEMPLATE = "${channel.codes}.${channel.getStart('yyyy_DDD_HH_mm_ss')}.resp";

    private String template;

    private PrintlineVelocitizer velocitizer;
    

    private static Logger logger = LoggerFactory.getLogger(ResponseWriter.class);
}
