/**
 * SodGUIEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import java.awt.GridBagConstraints;
import java.io.IOException;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.BasicConfigurator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import javax.swing.JLabel;
import org.w3c.dom.*;
import org.apache.xpath.XPathAPI;
import javax.swing.SwingConstants;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JOptionPane;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import java.awt.FileDialog;
import edu.sc.seis.sod.Start;
import javax.swing.JComponent;



public class SodGUIEditor extends SimpleGUIEditor {

    SodGUIEditor(String[] args) throws IOException, ParserConfigurationException, TransformerException, DOMException, SAXException {
        super(args);
        frameName = "SOD Editor";
        tabs = true;
    }


    public void start() {
        super.start();
        JMenuBar menubar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        File configFile = new File(configFilename);
                        try {
                            save(configFile);
                        } catch (IOException ex) {
                            GlobalExceptionHandler.handle("Unable to save "+configFile, ex);
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
                            GlobalExceptionHandler.handle("Unable to save to "+outfile, ex);
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
    }

    JComponent getCompForElement(Element element) {
        try {
            if (element.getTagName().equals("property")) {
                PropertyEditor edit = new PropertyEditor();
                return edit.getGUI(element);
            } else {
                return super.getCompForElement(element);
            }
        } catch (Exception e) {
            return super.getCompForElement(element);
        }
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException, DOMException, SAXException {
        BasicConfigurator.configure();
        SodGUIEditor gui = new SodGUIEditor(args);
        gui.start();
        System.out.println("Done editing.");
    }

}


