/**
 * ResponseWriterEditor.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.editor;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.process.waveform.PrintlineSeismogramProcess;
import edu.sc.seis.sod.subsetter.channel.ResponseWriter;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public class ResponseWriterEditor extends PrintlineEditor {

    protected String getTitle() {
        return "File Pattern";
    }

    protected String getDefaultTemplateValue() {
        return PrintlineSeismogramProcess.DEFAULT_TEMPLATE;
    }

    protected String evaluate(String template) {
        return sv.evaluate(template, MockChannel.createChannel());
    }

    private SimpleVelocitizer sv = new SimpleVelocitizer();

    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createVerticalBox();
        b.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        JComponent comp = createVelocityFilenameEditor(element,
                                               ResponseWriter.DEFAULT_TEMPLATE,
                                               "responseFileTemplate",
                                               "Pattern",
                                               false);
        comp.setBorder(new TitledBorder(getTitle()));
        b.add(comp);
        return b;
    }
    
}