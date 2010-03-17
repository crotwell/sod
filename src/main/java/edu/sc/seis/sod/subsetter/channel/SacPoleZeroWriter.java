package edu.sc.seis.sod.subsetter.channel;

import java.io.FileNotFoundException;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.FilterType;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.VelocityFileElementParser;
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

    public StringTree accept(ChannelImpl chan, NetworkSource network)
            throws Exception {
        ChannelId channel_id = chan.get_id();
        Instrumentation inst;
        try {
            inst = network.getInstrumentation(channel_id);
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch(InstrumentationInvalid e) {
            return new Fail(this, "Invalid instrumentation", e);
        }
        if(inst.the_response.stages[0].filters[0].discriminator().value() != FilterType._POLEZERO) {
            String filter;
            if (inst.the_response.stages[0].filters[0].discriminator().value() == FilterType.COEFFICIENT.value()) {
                filter = "COEFFICIENT";
            } else if (inst.the_response.stages[0].filters[0].discriminator().value() == FilterType.LIST.value()) {
                filter = "LIST";
            } else {
                filter = "UNKNOWN: "+inst.the_response.stages[0].filters[0].discriminator().value();
            }
            return new Fail(this, "first (sensor) stage is not a PoleZero filter: "+filter);
        }
        String response = FissuresToSac.getPoleZero(inst.the_response)
                .toString();
        try {
            velocitizer.evaluate(template, response, chan);
        } catch(FileNotFoundException fe) {
            GlobalExceptionHandler.handle("Error while writing response file for "
                                                  + ChannelIdUtil.toString(chan.get_id()),
                                          fe);
            return new Fail(this, "Error while writing response file", fe);
        }
        return new Pass(this);
    }

    public static final String DEFAULT_DIRECTORY = "polezero/";

    public static final String DEFAULT_TEMPLATE = "${channel.codes}.${channel.getStart('yyyy_DDD_HH_mm_ss')}.sacpz";

    private String template;

    private PrintlineVelocitizer velocitizer;
}
