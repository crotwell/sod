/**
 * SimpleGUI.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import java.io.*;
import javax.swing.*;

import edu.sc.seis.fissuresUtil.exceptionHandler.GUIReporter;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.Writer;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;



public class SimpleGUIEditor extends CommandLineEditor {

    public SimpleGUIEditor(String[] args) throws TransformerException, ParserConfigurationException, IOException, DOMException, SAXException {
        super(args);
        GlobalExceptionHandler.add(new GUIReporter());
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            //Oh well, go with the default look and feel
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-tabs")) {
                tabs = true;
            }
        }
    }

    public void start() {
        frame = new JFrame(frameName);
        frame.getContentPane().setLayout(new BorderLayout());
        Document doc = getDocument();
        if (tabs) {
            JTabbedPane tabs = new JTabbedPane();
            frame.getContentPane().add(new JScrollPane(tabs), BorderLayout.CENTER);
            // put each top level sod element in a panel
            NodeList list = doc.getDocumentElement().getChildNodes();
            JPanel panel;
            for (int j = 0; j < list.getLength(); j++) {
                if (list.item(j) instanceof Element) {
                    panel = new JPanel();
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridx=0;
                    gbc.gridy=0;
                    gbc.anchor = gbc.WEST;
                    gbc.fill = gbc.HORIZONTAL;
                    gbc.weightx = 1;
                    gbc.weighty = 1;
                    panel.setLayout(new GridBagLayout());
                    tabs.add(((Element)list.item(j)).getTagName(), panel);
                    addElementToPanel(panel, (Element)list.item(j), gbc);
                }
            }
        } else {
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=0;
            gbc.anchor = gbc.WEST;
            gbc.fill = gbc.HORIZONTAL;
            gbc.weightx = 1;
            gbc.weighty = 1;
            addElementToPanel(panel, doc.getDocumentElement(), gbc);
            frame.getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
        }
        frame.pack();
        frame.show();
        frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        save(System.out);
                        System.exit(0);
                    }
                });
    }

    protected void save(File file) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(file);
        save(fos);
        fos.close();
    }

    protected void save(OutputStream out) {
        BufferedWriter buf =
            new BufferedWriter(new OutputStreamWriter(out));
        Writer xmlWriter = new Writer();
        xmlWriter.setOutput(buf);
        xmlWriter.write(getDocument());
    }

    void addElementToPanel(JPanel panel, Element element, GridBagConstraints gbc) {
        JLabel label = new JLabel(element.getTagName());
        panel.add(label, gbc);
        gbc.gridx++;
        addChildrenToPanel(panel, element, gbc);
        gbc.gridx--;
    }

    void addChildrenToPanel(JPanel panel, Element element, GridBagConstraints gbc) {
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
    }

    void addTextNodeToPanel(JPanel panel, Text text, GridBagConstraints gbc) {
        if (text.getNodeValue().trim().equals("")) {
            return;
        }
        JTextField textField = new JTextField();
        textField.setText(text.getNodeValue().trim());
        TextListener textListen = new TextListener(text);
        textField.getDocument().addDocumentListener(textListen);
        panel.add(textField, gbc);
    }

    /**
     *
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException, DOMException, SAXException {
        BasicConfigurator.configure();
        SimpleGUIEditor gui = new SimpleGUIEditor(args);
        gui.start();
        System.out.println("Done editing.");
    }

    String frameName = "Simple XML Editor GUI";

    boolean tabs = false;

    JFrame frame;

    private static Logger logger = Logger.getLogger(SimpleGUIEditor.class);


}


