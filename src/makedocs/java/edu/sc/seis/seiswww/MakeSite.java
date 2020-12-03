package edu.sc.seis.seiswww;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * @author groves Created on Nov 12, 2004
 */
public class MakeSite {

    public static class TemplateExistenceChecker {

        public boolean exists(String name) {
            try {
                return getEngine().templateExists(templatize(name));
            } catch(Exception e) {
                throw new RuntimeException("Got this exception getting the engine, but this should only be called from in a template, so huh?",
                                           e);
            }
        }

        public String include(String name) {
            StringWriter writer = new StringWriter();
            try {
                getEngine().evaluate(new VelocityContext(),
                                     writer,
                                     "includer",
                                     "#include('" + templatize(name) + "')");
            } catch(ResourceNotFoundException re) {
                System.err.println(re.getMessage());
            } catch(Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Got this using the engine, but this should only be a simple text file, so huh?",
                                           e);
            }
            return writer.getBuffer().toString();
        }

        private String templatize(String name) {
            return velocityDir
                    + workingContext.get("outputType")
                    + "/"
                    + workingContext.get((String)workingContext.get("variableName"))
                    + "/" + name;
        }
    }

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure(new NullAppender());
        loadProperties(args);
        velocityDir = props.getProperty("velocityDir");
        baseOutputDir = props.getProperty("outputDir");
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("--run-once")) {
                runOnce = true;
            } else if(args[i].equals("--output-dir")) {
                baseOutputDir = args[++i];
            } else if(args[i].equals("--velocity-resource-loader-path")) {
                velocityDir = new File(args[++i]).getAbsolutePath();
                if(!velocityDir.endsWith("/")) {
                    velocityDir += "/";
                }
                props.setProperty("velocimacro.library", velocityDir
                        + "VM_global_library.vm");
            }
        }
        if(!baseOutputDir.endsWith("/")) {
            baseOutputDir += "/";
        }
        if(!new File(baseOutputDir).exists()) {
            new File(baseOutputDir).mkdirs();
        }
        String site = (String)props.getProperty("site", "");
        if(site.equals("sod")) {
            setupSODExtras();
        }
        defaultContext.put("includer", new TemplateExistenceChecker());
        String staticContent = props.getProperty("staticContentDir");
        log("Starting");
        log("Loading velocity from " + velocityDir + " content from "
                + staticContent + " and outputting to " + baseOutputDir);
        do {
            recursiveCopy(staticContent, baseOutputDir);
            makeDirs(velocityDir, baseOutputDir);
            findLibs(velocityDir, baseOutputDir);
            writeVelocity(velocityDir, baseOutputDir);
            if(site.equals("root")) {
                writeRootExtras(velocityDir);
                recursiveCopy(velocityDir, baseOutputDir);
            }
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {}
        } while(!runOnce);
        recursiveCopy(staticContent, baseOutputDir);
        System.exit(0);
    }

    private static List makeEntryPerDir(File baseDir) throws IOException {
        String[] dirs = baseDir.list();
        List toFill = new ArrayList(dirs.length - 1);
        for(int i = 0; i < dirs.length; i++) {
            File entryDir = new File(baseDir, dirs[i]);
            if(!entryDir.isDirectory() || dirs[i].equals(".svn")
                    || dirs[i].equals(".DS_Store")) {
                continue;
            }
            toFill.add(dirs[i]);
        }
        return toFill;
    }

    private static void writeRootExtras(String siteDirName) throws Exception {
        File siteDir = new File(siteDirName);
        //writeIndividualPages(siteDir, "people", "person");
        writeIndividualPages(siteDir, "projects", "project");
    }

    private static void writeIndividualPages(File baseDir,
                                             String outputType,
                                             String variableName)
            throws Exception {
        Iterator it = makeEntryPerDir(new File(baseDir, outputType)).iterator();
        while(it.hasNext()) {
            Object value = it.next();
            workingContext = new VelocityContext(defaultContext);
            workingContext.put(variableName, value);
            writeVelocity(velocityDir + outputType + "/" + variableName
                    + "_detail_template.vm", new File(baseOutputDir, outputType
                    + "/" + value + "/index.html"));
            workingContext = null;
        }
    }

    private static VelocityContext workingContext;

    private static void setupSODExtras() {
        String soddir = props.getProperty("sodDir", "../");
        String sodScripts = soddir + "scripts/";
        String demoSodScripts = soddir + "src/main/resources/edu/sc/seis/sod/data/configFiles/";
        defaultContext.put("demoSnippetizer",
                           new Snippetizer("xslt/toSnippet.xslt",
                                           demoSodScripts,
                                           "velocity/sod/documentation/",
                                           "txt"));
        defaultContext.put("snippetizer",
                           new Snippetizer("xslt/toSnippet.xslt",
                                           sodScripts,
                                           "velocity/sod/documentation/",
                                           "txt"));
        defaultContext.put("htmlizer",
                           new Snippetizer("xslt/toHTML.xslt",
                                           sodScripts,
                                           baseOutputDir+"/documentation/tutorials/",
                                           "html"));
        defaultContext.put("demoHtmlizer",
                           new Snippetizer("xslt/toHTML.xslt",
                                           demoSodScripts,
                                           baseOutputDir+"/documentation/tutorials/",
                                           "html"));
        defaultContext.put("booleanLogic",
                           new Snippetizer("xslt/booleanLogic.xslt",
                                           sodScripts,
                                           "velocity/sod/documentation/",
                                           "txt"));
        defaultContext.put("apfSnippet",
                           new Snippetizer("xslt/apfSnippet.xslt",
                                           sodScripts,
                                           "velocity/sod/documentation/",
                                           "txt"));
        insertExternalArray("originExternals", ExternalInfoImpls.origin);
        insertExternalArray("networkExternals", ExternalInfoImpls.networks);
        insertExternalArray("waveformExternals", ExternalInfoImpls.waveforms);
        insertExternalArray("vectorExternals", ExternalInfoImpls.vector);
        insertExternalArray("originJython", JythonInfoImpls.origin);
        insertExternalArray("networkJython", JythonInfoImpls.networks);
        insertExternalArray("waveformJython", JythonInfoImpls.waveforms);
        insertExternalArray("vectorJython", JythonInfoImpls.vector);
    }

    private static void insertExternalArray(String name, ExternalInfo[] items) {
        defaultContext.put(name, items);
        for(int i = 0; i < items.length; i++) {
            defaultContext.put(items[i].getName() + "Info", items[i]);
        }
    }

    public static void makeDirs(String base, String output) throws IOException {
        File f = new File(base);
        if ( ! f.isDirectory()) {
            throw new RuntimeException("Can't mkdirs on a non-directry: "+f);
        }
        File[] contents = f.listFiles();
        for(int i = 0; i < contents.length; i++) {
            String filename = contents[i].getName();
            if(contents[i].isDirectory()) {
                String newbase = base + filename + '/';
                String newout = output + filename + '/';
                File newoutFile = new File(newout);
                if(!newoutFile.exists()) {
                    newoutFile.mkdir();
                } else if(!newoutFile.isDirectory()) {
                    // Can't write a file in a nondirectory
                    throw new IOException("Trying to write to nondirectory "
                            + newout);
                }
                makeDirs(newbase, newout);
            }
        }
    }

    public static void findLibs(String base, String output) {
        File f = new File(base);
        File[] contents = f.listFiles();
        for(int i = 0; i < contents.length; i++) {
            String filename = contents[i].getName();
            if(contents[i].isDirectory()) {
                String newbase = base + filename + '/';
                String newout = output + filename + '/';
                findLibs(newbase, newout);
            } else if(filename.startsWith("VM")) {
                libs.add(new File(base + filename));
            }
        }
    }

    public static void writeVelocity(String base, String output)
            throws Exception {
        File f = new File(base);
        File[] contents = f.listFiles();
        for(int i = 0; i < contents.length; i++) {
            String filename = contents[i].getName();
            if(contents[i].isDirectory()
                    && !contents[i].getName().endsWith(".svn")) {
                String newbase = base + filename + '/';
                String newout = output + filename + '/';
                writeVelocity(newbase, newout);
            } else if(filename.endsWith(".vm")) {
                if(!filename.startsWith("VM")) {
                    File outputFile = new File(output
                            + filename.replaceAll("vm", "html"));
                    writeVelocity(base + filename, outputFile);
                }
            }
        }
    }

    private static void writeVelocity(String velFile, File outputFile)
            throws IOException, ResourceNotFoundException,
            MethodInvocationException, Exception {
        writeVelocity(velFile, outputFile, runOnce);
    }

    private static void writeVelocity(String velFile,
                                      File outputFile,
                                      boolean force) throws IOException,
            ResourceNotFoundException, MethodInvocationException, Exception {
        if(force || velocityNeedsUpdating(outputFile, new File(velFile))) {
            Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),
                                                                 "UTF-8"));
            log("Velocitizing " + velFile + " to " + outputFile);
            try {
                if(workingContext == null) {
                    workingContext = new VelocityContext(defaultContext);
                }
                getEngine().mergeTemplate(velFile, "UTF-8", workingContext, w);
                workingContext = null;
            } catch(ParseErrorException e) {
                System.out.println(e.getMessage());
            }
            w.close();
        }
    }

    public static VelocityEngine getEngine() throws Exception {
        if(ve == null) {
            ve = new VelocityEngine();
            props.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                              "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            props.setProperty("runtime.log.logsystem.log4j.category",
                              logger.getName());
            ve.init(props);
        }
        return ve;
    }

    public static void recursiveCopy(String baseDir, String outputDir)
            throws IOException {
        File[] files = new File(baseDir).listFiles();
        for(int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()) {
                if(!files[i].getName().endsWith(".svn")) {
                    recursiveCopy(baseDir + files[i].getName() + "/", outputDir
                            + files[i].getName() + "/");
                }
            } else if(!files[i].getName().equals("Thumbs.db")) {
                copyFile(files[i], outputDir + files[i].getName());
            }
        }
    }

    public static void copyFile(File inputFile, String outputFile)
            throws IOException {
        File f = new File(outputFile);
        if(needsUpdating(f, inputFile)) {
            log("Copying " + inputFile + " to " + outputFile);
            f.getParentFile().mkdirs();
            InputStream src = new BufferedInputStream(new FileInputStream(inputFile));
            OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
            int curChar;
            while((curChar = src.read()) != -1) {
                os.write(curChar);
            }
            os.close();
            src.close();
        }
    }

    public static void log(String msg) {
        if(!runOnce) {
            System.out.println(df.format(new Date()) + " - " + msg);
        }
    }

    public static boolean velocityNeedsUpdating(File dest, File src) {
        Iterator it = libs.iterator();
        while(it.hasNext()) {
            if(((File)it.next()).lastModified() > dest.lastModified()) {
                return true;
            }
        }
        return needsUpdating(dest, src);
    }

    public static boolean needsUpdating(File dest, File src) {
        return !dest.exists() || dest.lastModified() < src.lastModified();
    }

    public static void loadProperties(String[] args) {
        String propFilename = null;
        props = System.getProperties();
        for(int i = 0; i < args.length - 1; i++) {
            if(args[i].equals("-p")) {
                propFilename = args[i + 1];
                try {
                    FileInputStream in = new FileInputStream(propFilename);
                    props.load(in);
                    in.close();
                } catch(FileNotFoundException f) {
                    System.err.println(" file missing " + f + " using defaults");
                } catch(IOException f) {
                    System.err.println(f.toString() + " using defaults");
                }
            }
        }
        if(propFilename == null) {
            System.err.println("-p <propfile> must be passed in");
            System.exit(0);
        }
    }

    private static VelocityContext defaultContext = new VelocityContext();

    private static DateFormat df = new SimpleDateFormat("HH:mm:ss");

    private static Set libs = new HashSet();

    private static VelocityEngine ve;

    private static Properties props;

    private static String baseOutputDir, velocityDir;

    private static boolean runOnce = false;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MakeSite.class);
}
