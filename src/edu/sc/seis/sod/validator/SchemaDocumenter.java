/**
 * SchemaDocumenter.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

import edu.sc.seis.sod.validator.ModelWalker;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.FormPrinter;
import edu.sc.seis.sod.validator.model.MultigenitorForm;
import edu.sc.seis.sod.validator.model.NamedElement;
import edu.sc.seis.sod.validator.model.StAXModelBuilder;
import java.io.File;
import java.io.FileWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class SchemaDocumenter {
    public static void main(String[] args) throws Exception{
        StAXModelBuilder handler = new StAXModelBuilder("../../relax/sod.rng");
        handler.getRoot().accept(new FormPrinter(4));
        //        VelocityEngine ve = new VelocityEngine();
        //        ve.init();
        //        VelocityContext c = new VelocityContext();
        //        c.put("root", handler.getRoot());
        //        TransformerFactory tFactory = TransformerFactory.newInstance();
        //        Transformer transformer = tFactory.newTransformer(new StreamSource("../pageGenerator.xsl"));
        //        writeTree(c, ve, root, transformer);
    }

    public static void writeTree(VelocityContext c, VelocityEngine ve,
                                 Form f, Transformer t) throws Exception{
        if(f instanceof MultigenitorForm){
            MultigenitorForm multiGen = (MultigenitorForm)f;
            if(!ModelWalker.isSelfReferential(f)){
                Form[] kids = multiGen.getChildren();
                for (int i = 0; i < kids.length; i++) {
                    writeTree(c, ve, kids[i], t);
                }
            }
        }else if(f instanceof NamedElement){
            NamedElement el = (NamedElement)f;
            System.out.println("WRITING " + f);
            c.put("currentElement", f);
            String xPath = null;//el.getXPath();
            System.out.println("XPATH IS " + xPath);
            File xmlFile = new File("xml/" + xPath + ".xml");
            xmlFile.getParentFile().mkdirs();
            FileWriter fw = new FileWriter(xmlFile);
            ve.mergeTemplate("elementPage.vm", c, fw);
            fw.close();
            File htmlFile = new File("html/"+ xPath + ".html");
            System.out.println(htmlFile);
            htmlFile.getParentFile().mkdirs();
            t.transform(new StreamSource(xmlFile), new StreamResult(htmlFile));
            writeTree(c, ve, el.getChild(), t);
        }
    }

}

