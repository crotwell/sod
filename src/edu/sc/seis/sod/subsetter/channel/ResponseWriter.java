package edu.sc.seis.sod.subsetter.channel;

import java.io.FileNotFoundException;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ResponsePrint;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
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

    public StringTree accept(Channel chan, ProxyNetworkAccess network)
            throws Exception {
        try {
            ChannelId channel_id = chan.get_id();
            Instrumentation inst = network.retrieve_instrumentation(channel_id,
                                                                    channel_id.begin_time);
            String response = ResponsePrint.printResponse(channel_id, inst);
            velocitizer.evaluate(template, response, chan);
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch (InstrumentationInvalid e) {
            return new Fail(this, "Invalid instrumentation");
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
}
