/**
 * SchemaDocumenter.java
 * 
 * @author Charles Groves
 */
package edu.sc.seis.sod.validator.documenter;

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
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.validator.ModelWalker;
import edu.sc.seis.sod.validator.model.Definition;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.StAXModelBuilder;
import edu.sc.seis.sod.validator.tour.DepthAwareGuide;
import edu.sc.seis.sod.validator.tour.HTMLOutlineTourist;

public class SchemaDocumenter {

    public static void main(String[] args) throws Exception {
        StAXModelBuilder handler = new StAXModelBuilder("../../relax/sod.rng");
        relaxBase = "../../relax/";
        //Setup velocity
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        VelocityContext c = new VelocityContext();
        c.put("root", handler.getRoot());
        c.put("walker", new ModelWalker());
        c.put("util", new SodUtil());
        c.put("helper", new VelocityModelHelper());
        c.put("doc", new SchemaDocumenter());
        //Setup xsl transforms
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(new StreamSource("../pageGenerator.xsl"));
        //Run velocity then xsl on all definitions
        Collection defs = StAXModelBuilder.getAllDefinitions();
        Iterator it = defs.iterator();
        while(it.hasNext()) {
            Definition cur = (Definition)it.next();
            //if(makePath(cur).startsWith("waveform/requestSubsetter/breqFastRequest")) {
                render(c, ve, cur, transformer);
                System.out.print(".");
            //}
        }
    }

    static Set renderLocs = new HashSet();

    public static void render(VelocityContext c,
                              VelocityEngine ve,
                              Definition def,
                              Transformer t) throws Exception {
        String path = makePath(def);
        File xmlFile = new File("xml/" + path + ".xml");
        xmlFile.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(xmlFile);
        c.put("def", def);
        HTMLOutlineTourist tourist = new HTMLOutlineTourist(makePath(def));
        DepthAwareGuide guide = new DepthAwareGuide(def.getForm());
        guide.lead(tourist);
        c.put("contained", tourist.getResult());
        ve.mergeTemplate("elementPage.vm", new VelocityContext(c), fw);
        fw.close();
        String outputLoc = "../generatedSite/tagDocs/" + path + ".html";
        File htmlFile = new File(outputLoc);
        htmlFile.getParentFile().mkdirs();
        String relPath = SodUtil.getRelativePath(outputLoc,
                                                 "../generatedSite/",
                                                 "/");
        relPath = relPath.substring(0, relPath.length()
                - "../generatedSite".length());
        t.setParameter("base", relPath);
        t.setParameter("menu", "Reference");
        t.setParameter("page", "tagDocs/" + path + ".html");
        t.transform(new StreamSource("xml/" + path + ".xml"),
                    new StreamResult(htmlFile));
    }

    public static Definition getNearestDef(Form f) {
        if(f.isFromDef()) { return f.getDef(); }
        return getNearestDef(f.getParent());
    }

    public static String makePath(Form f) {
        return makePath(getNearestDef(f));
    }

    public static String makePath(Definition def) {
        String rngLoc = def.getGrammar().getLoc();
        String path = rngLoc.substring(relaxBase.length(), rngLoc.length() - 4);
        path += "/" + def.getName();
        if(def.getName().equals("")) {
            path += "start";
        }
        return path;
    }

    static String relaxBase = "";

    static Set writtenFiles = new HashSet();

    static File base = new File("html/");
}