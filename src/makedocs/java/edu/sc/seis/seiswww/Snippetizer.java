package edu.sc.seis.seiswww;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class Snippetizer {

    public Snippetizer(String stylesheet) {
        this(stylesheet, "", "", "html");
    }

    public Snippetizer(String stylesheet,
                       String sourceBase,
                       String resultBase,
                       String resultType) {
        try {
            transformer = tFactory.newTransformer(new StreamSource(stylesheet));
        } catch(TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        this.sourceBase = sourceBase;
        this.resultBase = resultBase;
        this.resultType = resultType;
    }

    public void copy(String source) throws IOException {
        MakeSite.copyFile(new File(getSourceLocation(source)),
                          getCopyLocation(source));
    }

    public void transform(String source) throws Exception {
        File dest = new File(getDestLocation(source));
        dest.getParentFile().mkdirs();
        transformer.transform(new StreamSource(getSourceLocation(source)),
                              new StreamResult(new FileOutputStream(dest)));
    }

    public String getResult(String source) throws Exception {
        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(getSourceLocation(source)),
                              new StreamResult(writer));
        return writer.toString();
    }

    public String getDestName(String source) {
        return getName(source, resultType);
    }

    public String getCopyName(String source) {
        return getName(source, "xml");
    }

    private String getCopyLocation(String source) {
        return getDestLocation(source, "xml");
    }

    private String getDestLocation(String source) {
        return getDestLocation(source, resultType);
    }

    private String getDestLocation(String source, String type) {
        return resultBase + getName(source, type);
    }

    private String getName(String source, String type) {
        return source.substring(source.lastIndexOf('/') + 1,
                                source.indexOf(".xml"))
                + "." + type;
    }

    private String getSourceLocation(String source) {
        return sourceBase + source;
    }

    public String getElementResult(String source, String elementName)
            throws Exception {
        return getElementResult(source, elementName, true);
    }

    public String getElementResultWithoutIndentingFirstLine(String source,
                                                            String elementName)
            throws Exception {
        return getElementResult(source, elementName, false);
    }

    public String getElementResult(String source,
                                   String elementName,
                                   boolean indentFirst) throws Exception {
        StringWriter writer = new StringWriter();
        transformer.setParameter("select", elementName);
        transformer.transform(new StreamSource(getSourceLocation(source)),
                              new StreamResult(writer));
        transformer.clearParameters();
        if(indentFirst) {
            return writer.toString();
        }
        return writer.toString().substring(4);
    }

    private Transformer transformer;

    private static TransformerFactory tFactory;
    static {
        tFactory = TransformerFactory.newInstance();
    }

    private String sourceBase, resultBase, resultType;
}
