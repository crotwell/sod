package edu.sc.seis.sod.subsetter.channel;

import java.io.FileNotFoundException;

import org.codehaus.stax2.validation.XMLValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.ResponsePrint;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.VelocityFileElementParser;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

public class ResponseWriter implements ChannelSubsetter {

    public ResponseWriter(Element config) throws ConfigurationException {
        VelocityFileElementParser parser = new VelocityFileElementParser(config,
                                                                         DEFAULT_DIRECTORY,
                                                                         DEFAULT_TEMPLATE);
        template = parser.getTemplate();
        velocitizer = new PrintlineVelocitizer(new String[] {template});
    }

    public StringTree accept(ChannelImpl chan, NetworkSource network)
            throws Exception {
        try {
            Instrumentation inst = network.getInstrumentation(chan);
            String response = ResponsePrint.printResponse(chan.getId(), inst);
            velocitizer.evaluate(template, response, chan);
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch (InvalidResponse e) {
            return new Fail(this, "Invalid instrumentation: "+e.getMessage());
        } catch(FileNotFoundException fe) {
            GlobalExceptionHandler.handle("Error while writing response file for "
                    + ChannelIdUtil.toString(chan.get_id()), fe);
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
