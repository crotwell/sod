/**
 * SchemaDocumenter.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.documenter;


import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.validator.ModelWalker;
import edu.sc.seis.sod.validator.model.Definition;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.MultigenitorForm;
import edu.sc.seis.sod.validator.model.NamedElement;
import edu.sc.seis.sod.validator.model.StAXModelBuilder;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class SchemaDocumenter {
    public static void main(String[] args) throws Exception{

        //StAXModelBuilder handler = new StAXModelBuilder("../../exampleSchema/library.rng");
        StAXModelBuilder handler = new StAXModelBuilder("../../relax/sod.rng");
        relaxBase = "../../relax/";
        //handler.getRoot().accept(new FormPrinter(4));
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        VelocityContext c = new VelocityContext();
        c.put("root", handler.getRoot());
        c.put("walker", new ModelWalker());
        c.put("util", new SodUtil());
        c.put("helper", new VelocityModelHelper());
        c.put("doc", new SchemaDocumenter());
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(new StreamSource("../pageGenerator.xsl"));
        Collection defs = StAXModelBuilder.getAllDefinitions();
        Iterator it = defs.iterator();
        while(it.hasNext()){
            Definition cur = (Definition)it.next();
            //if(makePath(cur).equals("properties/start")){
                render(c, ve, cur, transformer);
            //}
        }
        //writeTree(c, ve, handler.getRoot(), transformer);
    }

    static Set renderLocs = new HashSet();

    public static void render(VelocityContext c, VelocityEngine ve,
                              Definition def, Transformer t) throws Exception{
        String path = makePath(def);
        File xmlFile = new File("xml/" + path + ".xml");
        if(!renderLocs.add(path)){
            System.out.println("FUCKED UP");
            System.exit(0);
        }
        xmlFile.getParentFile().mkdirs();
        System.out.println("writin " + xmlFile);
        FileWriter fw = new FileWriter(xmlFile);
        c.put("def", def);
        ve.mergeTemplate("elementPage.vm", new VelocityContext(c), fw);
        fw.close();

        String outputLoc = "../generatedSite/tagDocs/"+ path + ".html";
        File htmlFile = new File(outputLoc);
        htmlFile.getParentFile().mkdirs();
        String relPath = SodUtil.getRelativePath(outputLoc,
                                                 "../generatedSite/",
                                                 "/");
        relPath = relPath.substring(0, relPath.length() - "../generatedSite".length());
        t.setParameter("base", relPath);
        t.transform(new StreamSource("xml/"+path+".xml"), new StreamResult(htmlFile));
    }

    public static Definition getNearestDef(Form f){
        if(f.isFromDef()){ return f.getDef(); }
        else return getNearestDef(f.getParent());
    }

    public static String makePath(Form f){ return makePath(getNearestDef(f)); }

    public static String makePath(Definition def){
        String rngLoc = def.getGrammar().getLoc();
        String path = rngLoc.substring(relaxBase.length(), rngLoc.length() - 4);
        path += "/" + def.getName();
        if(def.getName().equals("")){ path += "start"; }
        return path;
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
            String path;
            c.put("curEl", f);
            if(f.isFromDef()){
                if(!ModelWalker.isSelfReferential(f)){
                    path = "";
                }else{
                    return;
                }
            }else{
                path = el.getXPath();
                File xmlFile = new File("xml/" + path + ".xml");
                if(!writtenFiles.add(xmlFile.getAbsolutePath())){
                    System.out.println("WROTE IN AN ALREADY EXISTING LOCATION");
                }
                xmlFile.getParentFile().mkdirs();
                FileWriter fw = new FileWriter(xmlFile);
                ve.mergeTemplate("elementPage.vm", new VelocityContext(c), fw);
                fw.close();
            }

            String outputLoc = "../generatedSite/tagDocs/"+ path + ".html";
            File htmlFile = new File(outputLoc);
            htmlFile.getParentFile().mkdirs();
            String relPath = SodUtil.getRelativePath(outputLoc,
                                                     "../generatedSite/",
                                                     "/");
            relPath = relPath.substring(0, relPath.length() - "../generatedSite".length());
            t.setParameter("base", relPath);
            t.transform(new StreamSource("xml/"+path+".xml"), new StreamResult(htmlFile));
            writeTree(c, ve, el.getChild(), t);
        }
    }

    static String relaxBase = "";
    static Set writtenFiles = new HashSet();
    static  File base = new File("html/");
}

