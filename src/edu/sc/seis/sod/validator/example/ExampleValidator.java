/**
 * ExampleValidator.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.validator.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import edu.sc.seis.sod.validator.Validator;
import edu.sc.seis.sod.validator.model.Annotation;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.ModelUtil;
import edu.sc.seis.sod.validator.model.StAXModelBuilder;
import edu.sc.seis.sod.validator.tour.FormPrinter;
import edu.sc.seis.sod.validator.tour.MaximalVisitGuide;
import edu.sc.seis.sod.validator.tour.MinimalVisitGuide;
import edu.sc.seis.sod.validator.tour.TourGuide;

public class ExampleValidator {

    public static void validate(String exampleContainingSchemaLoc)
            throws IOException, XMLStreamException, SAXException {
        StAXModelBuilder modelBuilder = new StAXModelBuilder(exampleContainingSchemaLoc);
        Annotation[] anns = modelBuilder.getAnnotations();
        Form root = modelBuilder.getRoot().getForm();
        //validate(anns, root, 0, 20);
        validate(anns, root, 0, anns.length);
    }

    private static void validate(Annotation[] anns,
                                 Form root,
                                 int start,
                                 int end) {
        Validator validator = new Validator(Validator.SOD_SCHEMA_LOC);
        //Validator validator = new Validator("/Users/oliverpa/Code/seis/sod/relax/event/origin.rng");
        ExampleBuilder eBuild;
        int totalInserted = 0;
        File file = new File("tempEx");
        for(int i = start; i < end || i == anns.length - 1; i++) {
            try {
                System.out.println("-----------------------------------");
                System.out.println("ann #" + i);
                eBuild = new ExampleBuilder(false);
                eBuild.setRequiredExample(anns[i]);
                eBuild.write(root);
                if(eBuild.isExampleInserted()) {
                    totalInserted++;
                }
                String example = eBuild.toString();
                //printExamples("CURRENT EXAMPLE", new String[]{example});
                FileWriter fw = new FileWriter(file);
                fw.write(example);
                fw.close();
                InputSource in = new InputSource(new FileInputStream(file));
                String[] examples = new String[] {anns[i].getExample(false),
                                                  example};
                //printExamples("Example " + i, examples);
                System.out.println("lineage: "
                        + ModelUtil.getLineageString(anns[i].getFormProvider()
                                .getForm()));
                if(!validator.validate(in, true)) {
//                    System.out.println("lineage: "
//                            + ModelUtil.getLineageString(anns[i].getFormProvider()
//                                    .getForm()));
                    //System.out.println("NOT VALID");
                    printExamples("NOT VALID", examples);
                } else {
                    //if(anns[i].getExample(false).startsWith("<networkOwner>"))
                    // {
                    //System.out.println("VALID!!!");
                    printExamples("VALID!!!", examples);
                    //}
                }
                file.delete();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        int numAnns = end - start;
        System.out.println("number of examples: " + numAnns);
        System.out.println("number inserted: " + totalInserted);
        double hitRatio = (double)totalInserted / (double)numAnns;
        System.out.println("hit ratio: " + hitRatio);
    }

    public static void printExamples(String prefix, String[] examples) {
        StringBuffer buf = new StringBuffer();
        buf.append(prefix + ": inserted example: \n");
        for(int i = 0; i < examples.length; i++) {
            buf.append(insertLineNumbers(examples[i]) + "\n---------------\n");
        }
        System.out.println(buf.toString());
    }

    public static String insertLineNumbers(String input) {
        StringBuffer buf = new StringBuffer();
        StringTokenizer tok = new StringTokenizer(input, "\n");
        for(int i = 1; tok.hasMoreTokens(); i++) {
            buf.append(i + ": " + tok.nextToken() + '\n');
        }
        return buf.toString();
    }

    public static void main(String[] args) throws SAXException, IOException,
            XMLStreamException {
        validate("/Users/oliverpa/Code/seis/sod/relax/sod.rng");
        //validate("/Users/oliverpa/Code/seis/sod/relax/event/origin.rng");
    }
}