/**
 * SchemaDocumenter.java
 * 
 * @author Charles Groves
 */
package edu.sc.seis.sod.validator.documenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
        base = args[0];
        outputdir = args[1];
        StAXModelBuilder handler = new StAXModelBuilder(base + "relax/sod.rng");
        //Setup velocity
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("file.resource.loader.path", base + "site");
        ve.init();
        VelocityContext c = new VelocityContext();
        c.put("root", handler.getRoot());
        c.put("walker", new ModelWalker(handler.getRoot()));
        c.put("util", new SodUtil());
        c.put("helper", new VelocityModelHelper());
        c.put("doc", new SchemaDocumenter());
        Collection defs = StAXModelBuilder.getAllDefinitions();
        Iterator it = defs.iterator();
        while(it.hasNext()) {
            render(c, ve, (Definition)it.next());
            System.out.print('.');
        }
    }

    static Set renderLocs = new HashSet();

    public static void render(VelocityContext c,
                              VelocityEngine ve,
                              Definition def) throws Exception {
        String path = makePath(def);
        File velFile = new File(outputdir + path + ".vm");
        velFile.getParentFile().mkdirs();
        Writer w = new BufferedWriter(new FileWriter(velFile));
        System.out.println(velFile);
        c.put("def", def);
        HTMLOutlineTourist tourist = new HTMLOutlineTourist(makePath(def));
        DepthAwareGuide guide = new DepthAwareGuide(def.getForm());
        guide.lead(tourist);
        c.put("contained", tourist.getResult());
        ve.mergeTemplate("elementPage.vm", new VelocityContext(c), w);
        w.close();
    }

    public static Definition getNearestDef(Form f) {
        if(f.isFromDef()) {
            return f.getDef();
        }
        return getNearestDef(f.getParent());
    }

    public static String makePath(Form f) {
        return makePath(getNearestDef(f));
    }

    public static String makePath(Definition def) {
        String rngLoc = def.getGrammar().getLoc();
        String path = rngLoc.substring(rngLoc.indexOf("relax") + 6,
                                       rngLoc.length() - 4);
        path += "/" + def.getName();
        if(def.getName().equals("")) {
            path += "start";
        }
        return path;
    }

    static String base, outputdir;
}