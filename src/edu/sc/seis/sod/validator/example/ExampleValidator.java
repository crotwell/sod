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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.InputSource;
import edu.sc.seis.sod.validator.Validator;
import edu.sc.seis.sod.validator.model.Annotation;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.GenitorForm;
import edu.sc.seis.sod.validator.model.MultigenitorForm;
import edu.sc.seis.sod.validator.model.StAXModelBuilder;

public class ExampleValidator {

    public static void validate(String exampleContainingSchemaLoc)
            throws IOException, XMLStreamException {
        System.out.println("validating " + exampleContainingSchemaLoc);
        StAXModelBuilder modelBuilder = new StAXModelBuilder(exampleContainingSchemaLoc);
        Form root = modelBuilder.getRoot().getForm();
        Annotation[] anns = findUniqueAnnotations(root);
        System.out.println(anns.length + " examples to be validated");
        validate(anns, root, 0, anns.length);
    }

    private static Annotation[] findUniqueAnnotations(Form root) {
        List l = new ArrayList();
        findUniqueAnnotations(root, l, new HashSet());
        return (Annotation[])l.toArray(new Annotation[l.size()]);
    }

    private static void findUniqueAnnotations(Form root,
                                              List annotations,
                                              Set visitedDefs) {
        if(root.isFromDef()) {
            if(visitedDefs.contains(root.getDef().toString())) { return; }
            visitedDefs.add(root.getDef().toString());
        }
        if(root.getAnnotation().hasExampleFromAnnotation()) {
            annotations.add(root.getAnnotation());
        }
        if(root instanceof GenitorForm) {
            findUniqueAnnotations(((GenitorForm)root).getChild(),
                                  annotations,
                                  visitedDefs);
        } else if(root instanceof MultigenitorForm) {
            Form[] kids = ((MultigenitorForm)root).getChildren();
            for(int i = 0; i < kids.length; i++) {
                findUniqueAnnotations(kids[i], annotations, visitedDefs);
            }
        }
    }

    private static void validate(Annotation[] anns,
                                 Form root,
                                 int start,
                                 int end) {
        Validator validator = new Validator(Validator.SOD_SCHEMA_LOC);
        int totalInserted = 0;
        int totalValid = 0;
        File file = new File("tempEx");
        for(int i = start; i < end || i == anns.length - 1; i++) {
            try {
                ExampleBuilder eBuild = new ExampleBuilder(false);
                eBuild.setRequiredExample(anns[i]);
                eBuild.write(root);
                if(eBuild.isExampleInserted()) {
                    totalInserted++;
                }
                String example = eBuild.toString();
                FileWriter fw = new FileWriter(file);
                fw.write(example);
                fw.close();
                InputSource in = new InputSource(new FileInputStream(file));
                String[] examples = new String[] {anns[i].getExample(false),
                                                  example};
                if(!validator.validate(in, true)) {
                    printExamples("NOT VALID", examples);
                } else {
                    totalValid++;
                }
                file.delete();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        int numAnns = end - start;
        System.out.println(getPercentage(numAnns, totalInserted) + "% inserted");
        System.out.println(getPercentage(numAnns, totalValid) + "% valid");
    }

    public static String getPercentage(double total, double actual) {
        if(actual == total) { return "" + 100; }
        double percentage = actual / total * 100;
        return new DecimalFormat("0.00").format(percentage);
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

    public static void main(String[] args) throws IOException,
            XMLStreamException {
        if(args.length >= 1) {
            validate(args[0]);
        } else {
            validate("sod.rng");
        }
    }
}