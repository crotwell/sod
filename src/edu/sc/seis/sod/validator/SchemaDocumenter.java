/**
 * SchemaDocumenter.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.validator.ModelWalker;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.MultigenitorForm;
import edu.sc.seis.sod.validator.model.NamedElement;
import edu.sc.seis.sod.validator.model.StAXModelBuilder;
import java.io.File;
import java.io.FileWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class SchemaDocumenter {
    public static void main(String[] args) throws Exception{

        StAXModelBuilder handler = new StAXModelBuilder("../../exampleSchema/library.rng");
        //StAXModelBuilder handler = new StAXModelBuilder("../../relax/sod.rng");
        //handler.getRoot().accept(new FormPrinter(4));
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        VelocityContext c = new VelocityContext();
        System.out.println(handler.getRoot());
        c.put("root", handler.getRoot());
        c.put("walker", new ModelWalker());
        c.put("util", new SodUtil());
        c.put("helper", new VelocityModelHelper());
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(new StreamSource("../pageGenerator.xsl"));
        writeTree(c, ve, handler.getRoot(), transformer);
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
            if(!ModelWalker.isSelfReferential(f)){
                NamedElement el = (NamedElement)f;
                System.out.println("WRITING " + f);
                c.put("curEl", f);
                String xPath = el.getXPath();
                System.out.println("XPATH IS " + xPath);
                File xmlFile = new File("xml/" + xPath + ".xml");
                xmlFile.getParentFile().mkdirs();
                FileWriter fw = new FileWriter(xmlFile);
                ve.mergeTemplate("elementPage.vm", new VelocityContext(c), fw);
                fw.close();

                String outputLoc = "../generatedSite/tagDocs/"+ xPath + ".html";
                File htmlFile = new File(outputLoc);
                System.out.println(htmlFile);
                htmlFile.getParentFile().mkdirs();
                String relPath = SodUtil.getRelativePath(outputLoc,
                                                         "../generatedSite/",
                                                         "/");
                relPath = relPath.substring(0, relPath.length() - "../generatedSite".length());
                t.setParameter("base", relPath);
                System.out.println("set base to " + relPath);
                t.transform(new StreamSource(xmlFile), new StreamResult(htmlFile));
                writeTree(c, ve, el.getChild(), t);
            }
        }
    }


    static  File base = new File("html/");
}

