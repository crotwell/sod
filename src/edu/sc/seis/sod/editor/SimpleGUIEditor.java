/**
 * SimpleGUI.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import java.io.*;
import javax.swing.*;
import org.w3c.dom.*;

import edu.sc.seis.fissuresUtil.exceptionHandler.GUIReporter;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.Writer;
import edu.sc.seis.sod.CommonAccess;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
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

        JMenuBar menubar = new JMenuBar();
        frame.setJMenuBar(menubar);
        JMenu fileMenu = new JMenu("File");
        menubar.add(fileMenu);
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        File configFile = new File(configFilename);
                        try {
                            save(configFile);
                        } catch (IOException ex) {
                            CommonAccess.handleException("Unable to save "+configFile, ex);
                        }
                    }
                });
        fileMenu.add(save);
        JMenuItem saveAs = new JMenuItem("Save As...");
        fileMenu.add(saveAs);
        saveAs.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        FileDialog fileDialog = new FileDialog(frame);
                        fileDialog.show();
                        String outfilename = fileDialog.getFile();
                        if (outfilename != null) {
                        File outfile = new File(outfilename);
                        try {
                            save(outfile);
                        } catch (IOException ex) {
                            CommonAccess.handleException("Unable to save to "+outfile, ex);
                        }
                    }
                    }
                });
        JMenuItem quit = new JMenuItem("Quit");
        fileMenu.add(quit);
        quit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }
                });

        frame.getContentPane().setLayout(new BorderLayout());
        Document doc = getDocument();
        if (tabs) {
            try {
                props.load((SimpleGUIEditor.class).getClassLoader().getResourceAsStream(NAME_PROPS ));
            }catch(IOException e)
            {
                CommonAccess.handleException("Error in loading names Prop file",e);
            }
            frame.getContentPane().add(new JScrollPane(getTabPane()), BorderLayout.CENTER);
            // put each top level sod element in a panel
            NodeList list = doc.getDocumentElement().getChildNodes();
            for (int j = 0; j < list.getLength(); j++) {
                if (list.item(j) instanceof Element) {
                    Box box = Box.createVerticalBox();
                    NodeList sublist = ((Element)list.item(j)).getChildNodes();
                    for (int i = 0; i < sublist.getLength(); i++) {
                        if (sublist.item(i) instanceof Element) {
                            box.add(getCompForElement((Element)sublist.item(i)));
                            box.add(Box.createRigidArea(new Dimension(3, 3)));
                        }
                    }
                    box.add(Box.createGlue());
                    JPanel panel = new JPanel();
                    panel.setLayout(new BorderLayout());
                    panel.add(box, BorderLayout.NORTH);
                    String tabName = props.getProperty(((Element)list.item(j)).getTagName(),
                                                      ((Element)list.item(j)).getTagName());
                    tabPane.add(EditorUtil.capFirstLetter(tabName),
                                                       panel);
                }
            }
        } else {
            JComponent comp = getCompForElement(doc.getDocumentElement());
            Box box = Box.createVerticalBox();
            box.add(comp);
            box.add(Box.createGlue());
            frame.getContentPane().add(new JScrollPane(box), BorderLayout.CENTER);
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

    JComponent getCompForElement(Element element) {
        return getDefaultCompForElement(element);
    }

    JComponent getDefaultCompForElement(Element element) {
        JLabel label = new JLabel(element.getTagName());
        Box box = Box.createVerticalBox();
        JComponent comp = getCompForAttributes(element);
        if (comp != null) {
            box.add(comp);
        }

        NamedNodeMap attrList = element.getAttributes();

        NodeList list = element.getChildNodes();
        // simple case of only 1 child Text node and no attributes
        if (list.getLength() == 1 && list.item(0) instanceof Text && attrList.getLength() == 0) {
            comp = getCompForTextNode((Text)list.item(0));
            if (comp != null) {
                box = Box.createHorizontalBox();
                box.add(label);
                box.add(new JLabel(" = "));
                box.add(comp);
                return box;
            }
        } else {
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i) instanceof Element) {
                    box.add(getCompForElement((Element)list.item(i)));
                } else if (list.item(i) instanceof Text) {
                    Text text = (Text)list.item(i);
                    comp = getCompForTextNode(text);
                    if (comp != null) {
                        box.add(comp);
                    }
                }
            }
        }
        return indent(label, box);
    }

    JComponent getCompForAttributes(Element element) {
        Box box = Box.createHorizontalBox();
        Box nameCol = Box.createVerticalBox();
        box.add(nameCol);
        Box valCol = Box.createVerticalBox();
        box.add(valCol);
        NamedNodeMap list = element.getAttributes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i) instanceof Attr) {
                Attr attr = (Attr)list.item(i);
                JLabel label = new JLabel(attr.getName());
                nameCol.add(label);
                JTextField textField = new JTextField();
                textField.setText(attr.getNodeValue());
                TextListener textListen = new TextListener(attr);
                textField.getDocument().addDocumentListener(textListen);
                valCol.add(textField);
            }
        }
        return box;
    }

    JComponent getCompForTextNode(Text text) {
        if (text.getNodeValue().trim().equals("")) {
            return null;
        }
        JTextField textField = new JTextField();
        textField.setText(text.getNodeValue().trim());
        TextListener textListen = new TextListener(text);
        textField.getDocument().addDocumentListener(textListen);
        return textField;
    }

    /** creates a JPanel with the bottom component slightly indented relative
     to the bottome one. */
    public Box indent(JComponent top, JComponent bottom) {
        Box box = Box.createVerticalBox();
        Box topRow = Box.createHorizontalBox();
        box.add(topRow);
        Box botRow = Box.createHorizontalBox();
        box.add(botRow);

        topRow.add(top);
        topRow.add(Box.createGlue());
        botRow.add(Box.createRigidArea(new Dimension(10, 10)));
        botRow.add(bottom);
        botRow.add(Box.createGlue());
        return box;
    }

    public JFrame getFrame() {
        return frame;
    }

    public JTabbedPane getTabPane() {
        return tabPane;
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

    JTabbedPane tabPane = new JTabbedPane();

    Properties props = new Properties();

    private static String NAME_PROPS = "edu/sc/seis/sod/editor/names.prop";

    private static Logger logger = Logger.getLogger(SimpleGUIEditor.class);


}




