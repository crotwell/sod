package edu.sc.seis.sod.editor;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;

/**
 * @author groves
 * 
 * Created on May 30, 2005
 */
public abstract class PrintlineEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws Exception {
        JComponent templateEditor = createVelocityEditor(element,
                                                         getDefaultTemplateValue(),
                                                         "template",
                                                         "Output Template",
                                                         false);
        JComponent filenameEditor = createVelocityEditor(element,
                                                         "",
                                                         "filename",
                                                         "Output File",
                                                         true);
        Box vert = Box.createVerticalBox();
        vert.add(templateEditor);
        vert.add(filenameEditor);
        vert.setBorder(new TitledBorder(getTitle()));
        return vert;
    }

    protected abstract String getTitle();

    protected abstract String getDefaultTemplateValue();

    protected abstract String evaluate(String template);

    public JComponent createVelocityEditor(Element el,
                                           String defaultText,
                                           String elementName,
                                           String name,
                                           boolean useSystemOutLabel) {
        final Text template = getTextChildFromPossiblyNonexistentElement(el,
                                                                         elementName,
                                                                         defaultText);
        JTextField jtf = new JTextField();
        jtf.setText(template.getData());
        final JLabel results;
        if(useSystemOutLabel) {
            results = new SystemOutOnEmptyLabel(evaluate(template.getData()));
        } else {
            results = new MaxLengthLabel(evaluate(template.getData()));
        }
        jtf.getDocument().addDocumentListener(new VelocityUpdater(template,
                                                                  results));
        Box vertBox = Box.createVerticalBox();
        vertBox.add(EditorUtil.labelTextField(name, jtf));
        Box labelBox = Box.createHorizontalBox();
        labelBox.add(results);
        labelBox.add(Box.createHorizontalGlue());
        vertBox.add(labelBox);
        return vertBox;
    }

    private class VelocityUpdater implements DocumentListener {

        private final Text template;

        private final JLabel results;

        private VelocityUpdater(Text template, JLabel results) {
            super();
            this.template = template;
            this.results = results;
        }

        public void changedUpdate(DocumentEvent e) {
            handleEvent(e);
        }

        public void insertUpdate(DocumentEvent e) {
            handleEvent(e);
        }

        public void removeUpdate(DocumentEvent e) {
            handleEvent(e);
        }

        private void handleEvent(DocumentEvent e) {
            Document doc = e.getDocument();
            try {
                String text = doc.getText(0, doc.getLength());
                template.setData(text);
                results.setText(evaluate(template.getData()));
            } catch(BadLocationException e1) {
                throw new RuntimeException("Shouldn't happen since we're getting the full length",
                                           e1);
            }
        }
    }

    private class MaxLengthLabel extends JLabel {

        public MaxLengthLabel(String text) {
            super(text, JLabel.LEFT);
        }

        public void setText(String text) {
            if(text.length() < 60) {
                super.setText("Result: " + text);
            } else {
                super.setText("Result: " + text.substring(0, 57) + "...");
                setToolTipText(text);
            }
        }
    }

    private class SystemOutOnEmptyLabel extends MaxLengthLabel {

        public SystemOutOnEmptyLabel(String text) {
            super(text);
        }

        public void setText(String text) {
            if(text.equals("")) {
                super.setText("Outputting to standard out");
            } else {
                super.setText(text);
            }
        }
    }

    private static Text getTextChildFromPossiblyNonexistentElement(Element parentOfElement,
                                                                   String elementName,
                                                                   String defaultText) {
        Element filenameElement = DOMHelper.extractOrCreateElement(parentOfElement,
                                                                   elementName);
        return DOMHelper.extractOrCreateTextNode(filenameElement, defaultText);
    }
}
