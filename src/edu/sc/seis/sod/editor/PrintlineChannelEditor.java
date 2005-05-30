package edu.sc.seis.sod.editor;

import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.subsetter.channel.PrintlineChannelProcessor;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

/**
 * @author groves
 * 
 * Created on May 30, 2005
 */
public class PrintlineChannelEditor extends PrintlineEditor {

    protected String getTitle() {
        return "Channel Printer";
    }

    protected String getDefaultTemplateValue() {
        return PrintlineChannelProcessor.DEFAULT_TEMPLATE;
    }

    private SimpleVelocitizer sv = new SimpleVelocitizer();

    protected String evaluate(String template) {
        return sv.evaluate(template, MockChannel.createChannel());
    }
}
