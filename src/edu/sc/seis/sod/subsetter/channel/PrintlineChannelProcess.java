package edu.sc.seis.sod.subsetter.channel;

import java.io.IOException;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

/**
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell </a>
 */
public class PrintlineChannelProcess implements ChannelSubsetter {

    public PrintlineChannelProcess(Element config) throws ConfigurationException {
        filename = DOMHelper.extractText(config, "filename", "");
        template = DOMHelper.extractText(config, "template", DEFAULT_TEMPLATE);
        velocitizer = new PrintlineVelocitizer(new String[]{filename, template});
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network)
            throws IOException {
        velocitizer.evaluate(filename, template, channel);
        return new StringTreeLeaf(this, true);
    }

    public static final String DEFAULT_TEMPLATE = "Channel: $channel";

    private PrintlineVelocitizer velocitizer;

    private String filename, template;
}// PrintlineChannelProcessor
