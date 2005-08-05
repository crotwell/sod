package edu.sc.seis.sod.editor;

import java.awt.Color;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

/**
 * @author groves
 * 
 * Created on May 30, 2005
 */
public abstract class PrintlineEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws Exception {
        JComponent templateEditor = createVelocityTemplateEditor(element,
                                                                 getDefaultTemplateValue(),
                                                                 "template",
                                                                 "Output Template");
        JComponent filenameEditor = createVelocityFilenameEditor(element,
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

    public JComponent createVelocityTemplateEditor(Element el,
                                                   String defaultText,
                                                   String elementName,
                                                   String name) {
        return createVelocityEditor(el,
                                    defaultText,
                                    elementName,
                                    name,
                                    new MaxLengthLabel("", false),
                                    false);
    }

    public JComponent createVelocityFilenameEditor(Element el,
                                                   String defaultText,
                                                   String elementName,
                                                   String name,
                                                   boolean useSystemOutLabel) {
        JLabel results = null;
        if(useSystemOutLabel) {
            results = new SystemOutOnEmptyLabel("");
        } else {
            results = new MaxLengthLabel("", true);
        }
        return createVelocityEditor(el,
                                    defaultText,
                                    elementName,
                                    name,
                                    results,
                                    true);
    }

    public JComponent createVelocityEditor(Element el,
                                           String defaultText,
                                           String elementName,
                                           String name,
                                           JLabel results,
                                           boolean filize) {
        final Text template = DOMHelper.getTextChildFromPossiblyNonexistentElement(el,
                                                                         elementName,
                                                                         defaultText);
        JTextArea jta = new JTextArea(1, 30);
        jta.setEditable(true);
        jta.setText(template.getData());
        results.setText(evaluate(template.getData()));
        jta.getDocument().addDocumentListener(new VelocityUpdater(template,
                                                                  results));
        Box vertBox = Box.createVerticalBox();
        JScrollPane jtaScrollPane = new JScrollPane(jta);
        vertBox.add(EditorUtil.labelJComponent(name, jtaScrollPane));
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

        public MaxLengthLabel(String text, boolean filize) {
            super(text, JLabel.LEFT);
            this.filize = filize;
        }

        public void setText(String text) {
            if(text.startsWith(SimpleVelocitizer.ERR_PREFIX)) {
                setForeground(Color.RED);
                setTextIgnoreFilize(SimpleVelocitizer.cleanUpErrorStringForDisplay(text));
            } else {
                setForeground(Color.BLACK);
                if(filize) {
                    text = FissuresFormatter.filize(text);
                }
                if(text.length() < 60) {
                    super.setText("Result: " + text);
                } else {
                    super.setText("Result: " + text.substring(0, 57) + "...");
                    setToolTipText(text);
                }
            }
        }

        public void setTextIgnoreFilize(String text) {
            super.setText(text);
        }

        private boolean filize;
    }

    private class SystemOutOnEmptyLabel extends MaxLengthLabel {

        public SystemOutOnEmptyLabel(String text) {
            super(text, true);
        }

        public void setText(String text) {
            if(text.equals("")) {
                setForeground(Color.BLACK);
                super.setTextIgnoreFilize("Outputting to standard out");
            } else {
                super.setText(text);
            }
        }
    }
}