package edu.sc.seis.sod.editor;

import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.sod.subsetter.origin.PrintlineEventProcess;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

/**
 * @author groves
 * 
 * Created on May 29, 2005
 */
public class PrintlineEventEditor extends PrintlineEditor {

    protected String getTitle() {
        return "Event Printer";
    }

    protected String getDefaultTemplateValue() {
        return PrintlineEventProcess.DEFAULT_TEMPLATE;
    }

    protected String evaluate(String template) {
        return sv.evaluate(template, MockEventAccessOperations.createEvent());
    }

    private SimpleVelocitizer sv = new SimpleVelocitizer();
}
