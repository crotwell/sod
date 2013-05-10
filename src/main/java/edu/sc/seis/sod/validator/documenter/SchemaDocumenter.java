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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
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
        BasicConfigurator.configure();
        if (args.length != 3) {
            System.err.println("Usage: schemaDocumenter rng basedir outputdir");
            System.err.print("       ");
            for (int i = 0; i < args.length; i++) {
                System.err.print(args[i]+" ");
            }
            System.err.println();
            return;
        }
        String sod_rng = args[0];
        base = args[1];
        if (base.length() != 0 && ! base.endsWith("/")) { base += "/"; }
        outputdir = args[2];
        if (outputdir.length() != 0 && ! outputdir.endsWith("/")) { outputdir += "/"; }

        StAXModelBuilder handler = new StAXModelBuilder(sod_rng);
        //Setup velocity
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("file.resource.loader.path", base + "site");
        ve.setProperty("runtime.log.logsystem.log4j.logger", "schemaDocumenter");
        ve.init();
        VelocityContext c = new VelocityContext();
        walker = new ModelWalker(handler.getRoot());
        c.put("root", handler.getRoot());
        c.put("walker", walker);
        c.put("util", new SodUtil());
        c.put("helper", new VelocityModelHelper());
        c.put("doc", new SchemaDocumenter());
        Collection<Definition> defs = StAXModelBuilder.getAllDefinitions();
        List<Definition> sortedDefs = new ArrayList<Definition>();
        sortedDefs.addAll(defs);
        Collections.sort(sortedDefs, new Comparator<Definition>() {
            @Override
            public int compare(Definition o1, Definition o2) {
                return o1.getName().compareTo(o2.getName());
            }
            
        });
        Iterator<Definition> it = sortedDefs.iterator();
        while(it.hasNext()) {
            Definition def = it.next();
//            if(!def.getName().equals("eventArea")){
//                continue;
//            }
            render(c, ve, def);
            System.out.print('.');
        }
        System.out.println();
        System.out.println("Finish successfully");
    }

    public static void render(VelocityContext c,
                              VelocityEngine ve,
                              Definition def) throws Exception {
        String path = makePath(def);
        File velFile = new File(outputdir + path + ".vm");
        velFile.getParentFile().mkdirs();
        Writer w = new BufferedWriter(new FileWriter(velFile));
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
       // System.out.println("SchemaDocumenter.makePath: "+rngLoc+"  "+def.getForm().getXPath());
        String path = rngLoc.substring(rngLoc.indexOf("relax") + 6,
                                       rngLoc.length() - 4);
        //path += "/" + def.getName();
        path = "/" + def.getName();
        if(def.getName().equals("")) {
            path += "start";
        }
        return path;
    }

    static ModelWalker walker;
    static String base, outputdir;
}
