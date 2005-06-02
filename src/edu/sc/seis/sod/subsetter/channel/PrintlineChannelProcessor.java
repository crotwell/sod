package edu.sc.seis.sod.subsetter.channel;

import java.io.IOException;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;

/**
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell </a>
 */
public class PrintlineChannelProcessor implements ChannelSubsetter {

    public PrintlineChannelProcessor(Element config) {
        filename = DOMHelper.extractText(config, "filename", "");
        template = DOMHelper.extractText(config, "template", DEFAULT_TEMPLATE);
    }

    public boolean accept(Channel channel, NetworkAccess network) throws IOException {
        velocitizer.evaluate(filename, template, channel);
        return true;
    }

    public static final String DEFAULT_TEMPLATE = "Channel: $channel";

    private PrintlineVelocitizer velocitizer = new PrintlineVelocitizer();

    private String filename, template;
}// PrintlineChannelProcessor
