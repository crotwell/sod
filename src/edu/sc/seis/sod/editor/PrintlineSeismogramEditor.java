package edu.sc.seis.sod.editor;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.process.waveform.PrintlineSeismogramProcess;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

/**
 * @author groves
 * 
 * Created on May 30, 2005
 */
public class PrintlineSeismogramEditor extends PrintlineEditor {

    protected String getTitle() {
        return "Seismogram Printer";
    }

    protected String getDefaultTemplateValue() {
        return PrintlineSeismogramProcess.DEFAULT_TEMPLATE;
    }

    protected String evaluate(String template) {
        LocalSeismogramImpl spike = SimplePlotUtil.createSpike();
        LocalSeismogramImpl[] seis = {spike};
        RequestFilter req = new RequestFilter(spike.channel_id,
                                              spike.begin_time,
                                              spike.getEndTime()
                                                      .getFissuresTime());
        RequestFilter[] orig = {req};
        RequestFilter[] avail = {req};
        return sv.evaluate(template,
                           MockEventAccessOperations.createEvent(),
                           MockChannel.createChannel(),
                           orig,
                           avail,
                           seis,
                           null);
    }

    private SimpleVelocitizer sv = new SimpleVelocitizer();
}
