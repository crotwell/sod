/**
 * CommandLineEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import edu.sc.seis.sod.Start;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import org.xml.sax.SAXException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import org.w3c.dom.DOMException;
import org.apache.log4j.BasicConfigurator;



public class CommandLineEditor {

    public CommandLineEditor(String[] args) throws ParserConfigurationException, IOException, SAXException {
        this.args = args;
        processArgs();
    }

    void processArgs() throws DOMException, IOException, ParserConfigurationException, IOException, SAXException {
        boolean help = false;
        for (int i = 0; i < args.length; i++) {
            if (i < args.length-1 && args[i].equals("-f")) {
                configFilename = args[i+1];
                initConfigFile();
            } else if (args[i].equals("-help")) {
                help = true;
            }
        }

        if (help) {
            printOptions(new DataOutputStream(System.out));
        } else {

        }
    }

    void initConfigFile() throws FileNotFoundException, ParserConfigurationException, IOException, SAXException {
        File configFile = new File(configFilename);
        if (configFile.exists()) {
            InputStream in = new BufferedInputStream(new FileInputStream(configFile));
            this.start = new Start(in, configFile.toURL());
        }
    }

    public void printOptions(DataOutputStream out) throws DOMException, IOException {
        Document doc = start.getDocument();
        printOptions(doc.getDocumentElement(), out);
    }

    protected void printOptions(Element element, DataOutputStream out) throws DOMException, IOException {
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n instanceof Element) {
                printOptions((Element)n, out);
            } else if (n instanceof Text) {
                Text textNode = (Text)n;
                if ( ! textNode.getNodeValue().trim().equals("")) {
                    out.writeBytes(getFullName((Element)textNode.getParentNode())+"  "+textNode.getNodeValue()+"\n");
                } else {
                    // ignore whitespace
                }
            }
        }
    }

    protected String getFullName(Element e) {
        if (e.getParentNode() != null && e.getParentNode() instanceof Element) {
            return getFullName((Element)e.getParentNode())+"."+e.getTagName();
        } else {
            return e.getTagName();
        }
    }

    /**
     *
     */
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        BasicConfigurator.configure();
        CommandLineEditor cle = new CommandLineEditor(args);
        System.out.println("Done editing.");
    }

    String[] args;

    String configFilename;

    Start start;
}

