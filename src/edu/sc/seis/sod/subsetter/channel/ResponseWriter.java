package edu.sc.seis.sod.subsetter.channel;

import java.io.FileNotFoundException;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ResponsePrint;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.VelocityFileElementParser;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

public class ResponseWriter implements ChannelSubsetter {

    public ResponseWriter(Element config) throws ConfigurationException {
        VelocityFileElementParser parser = new VelocityFileElementParser(config,
                                                                         "responses/",
                                                                         DEFAULT_TEMPLATE);
        template = parser.getTemplate();
        velocitizer = new PrintlineVelocitizer(new String[] {template});
    }

    public boolean accept(Channel chan, ProxyNetworkAccess network)
            throws Exception {
        try {
            ChannelId channel_id = chan.get_id();
            Instrumentation inst = network.retrieve_instrumentation(channel_id,
                                                                    channel_id.begin_time);
            String response = ResponsePrint.printResponse(channel_id, inst);
            velocitizer.evaluate(template, response, chan);
        } catch(ChannelNotFound ex) {
            GlobalExceptionHandler.handle("Channel not found: "
                    + ChannelIdUtil.toString(chan.get_id()), ex);
            return false;
        } catch(FileNotFoundException fe) {
            GlobalExceptionHandler.handle("Error while response file for "
                    + ChannelIdUtil.toString(chan.get_id()), fe);
            return false;
        }
        return true;
    }

    public static final String DEFAULT_TEMPLATE = "${channel.codes}.${channel.getStart('yyyy_DDD_HH_mm_ss')}.resp";

    private String template;

    private PrintlineVelocitizer velocitizer;
}
