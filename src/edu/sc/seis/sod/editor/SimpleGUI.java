/**
 * SimpleGUI.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import edu.sc.seis.sod.Start;
import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.swing.JLabel;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import edu.sc.seis.fissuresUtil.xml.Writer;



public class SimpleGUI extends CommandLineEditor {

    public SimpleGUI(String[] args) throws TransformerException, ParserConfigurationException, IOException, DOMException, SAXException {
        super(args);

    }

    public void start() {
        JFrame frame = new JFrame("SOD Simple GUI");
        JPanel panel = new JPanel();
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = gbc.WEST;
        gbc.fill = gbc.HORIZONTAL;
        panel.setLayout(new GridBagLayout());
        Document doc = start.getDocument();
        addElementToPanel(panel, doc.getDocumentElement(), gbc);
        frame.pack();
        frame.show();
        frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        BufferedWriter buf =
                            new BufferedWriter(new OutputStreamWriter(System.out));
                        Writer xmlWriter = new Writer();
                        xmlWriter.setOutput(buf);
                        xmlWriter.write(start.getDocument());
                        System.exit(0);
                    }
                });
    }

    void addElementToPanel(JPanel panel, Element element, GridBagConstraints gbc) {
        JLabel label = new JLabel(element.getTagName());
        panel.add(label, gbc);
        gbc.gridx++;
        NodeList list = element.getChildNodes();
        // simple case of only 1 child Text node
        if (list.getLength() == 1 && list.item(0) instanceof Text) {
            addTextNodeToPanel(panel, (Text)list.item(0), gbc);
        } else {
            gbc.gridy++;
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i) instanceof Element) {
                    addElementToPanel(panel, (Element)list.item(i), gbc);
                } else if (list.item(i) instanceof Text) {
                    Text text = (Text)list.item(i);
                    addTextNodeToPanel(panel, text, gbc);
                }
                gbc.gridy++;
            }
        }
        gbc.gridy++;
        gbc.gridx--;
    }


    void addTextNodeToPanel(JPanel panel, Text text, GridBagConstraints gbc) {
        if (text.getNodeValue().trim().equals("")) {
            return;
        }
        JTextField textField = new JTextField();
        textField.setText(text.getNodeValue().trim());
        TextListener textListen = new SimpleGUI.TextListener(text);
        textField.getDocument().addDocumentListener(textListen);
        panel.add(textField, gbc);
    }

    /**
     *
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException, DOMException, SAXException {
        BasicConfigurator.configure();
        SimpleGUI gui = new SimpleGUI(args);
        gui.start();
        System.out.println("Done editing.");
    }

    private static Logger logger = Logger.getLogger(SimpleGUI.class);

    class TextListener implements DocumentListener {
        Text text;

        TextListener(Text text) {
            this.text = text;
        }
        /**
         * Gives notification that an attribute or set of attributes changed.
         *
         * @param e the document event
         */
        public void changedUpdate(DocumentEvent e) {
            try {
                text.setData(e.getDocument().getText(0, e.getDocument().getLength()));
            } catch (DOMException ex) {
                logger.error(ex);
            } catch (BadLocationException ex) {
                logger.error(ex);
            }
        }

        /**
         * Gives notification that there was an insert into the document.  The
         * range given by the DocumentEvent bounds the freshly inserted region.
         *
         * @param e the document event
         */
        public void insertUpdate(DocumentEvent e) {
            try {
                text.setData(e.getDocument().getText(0, e.getDocument().getLength()));
            } catch (DOMException ex) {
                logger.error(ex);
            } catch (BadLocationException ex) {
                logger.error(ex);
            }
        }

        /**
         * Gives notification that a portion of the document has been
         * removed.  The range is given in terms of what the view last
         * saw (that is, before updating sticky positions).
         *
         * @param e the document event
         */
        public void removeUpdate(DocumentEvent e) {
            try {
                text.setData(e.getDocument().getText(0, e.getDocument().getLength()));
            } catch (DOMException ex) {
                logger.error(ex);
            } catch (BadLocationException ex) {
                logger.error(ex);
            }
        }


    }
}

