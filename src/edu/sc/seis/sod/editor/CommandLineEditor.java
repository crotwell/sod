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



public class CommandLineEditor {

    public CommandLineEditor(String[] args) throws ParserConfigurationException, IOException, SAXException {
        this.args = args;
        processArgs();
    }

    void processArgs() {

        for (int i = 0; i < args.length; i++) {
            if (i < args.length-1 && args[i].equals("-f")) {
                configFilename = args[i+1];
            }
        }
    }

    void initConfigFile() throws FileNotFoundException, ParserConfigurationException, IOException, SAXException {
        File configFile = new File(configFilename);
        if (configFile.exists()) {
            InputStream in = new BufferedInputStream(new FileInputStream(configFile));
            this.start = new Start(in, configFile.toURL());
        }
    }

    public void printOptions(String configFilename, DataOutputStream out) throws DOMException, IOException {
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
                out.writeBytes(textNode.getParentNode().getNodeName()+"  "+textNode.getNodeValue());
            }
        }
    }

    /**
     *
     */
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        CommandLineEditor cle = new CommandLineEditor(args);
    }

    String[] args;

    String configFilename;

    Start start;
}

