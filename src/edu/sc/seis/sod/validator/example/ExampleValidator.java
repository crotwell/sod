/**
 * ExampleValidator.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.validator.example;

import edu.sc.seis.sod.validator.Validator;
import edu.sc.seis.sod.validator.model.Annotation;
import edu.sc.seis.sod.validator.model.StAXModelBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ExampleValidator {

    public static void validate(String exampleContainingSchemaLoc)
            throws IOException, XMLStreamException, SAXException {
        Validator validator = new Validator(Validator.SOD_SCHEMA_LOC);
        StAXModelBuilder modelBuilder = new StAXModelBuilder(exampleContainingSchemaLoc);
        Annotation[] anns = modelBuilder.getAnnotations();
        ExampleBuilder eBuild;
        File file = new File("tempEx");
        for(int i = 0; i < anns.length; i++) {
        //for(int i = 10; i < 20; i++) {
            eBuild = new ExampleBuilder(false);
            eBuild.setRequiredExample(anns[i]);
            eBuild.write(modelBuilder.getRoot().getForm());
            String example = eBuild.toString();
            //printExamples("CURRENT EXAMPLE", new String[]{example});
            FileWriter fw = new FileWriter(file);
            fw.write(example);
            fw.close();
            InputSource in = new InputSource(new FileInputStream(file));
            String[] examples = new String[] {anns[i].getExample(false),
                                              example};
            if(!validator.validate(in, true)) {
                printExamples("NOT VALID", examples);
            } else {
                //if
                // (anns[i].getExample(false).startsWith("<networkOwner>")){
                //printExamples("VALID!!!", examples);
            }
            file.delete();
        }
    }

    public static void printExamples(String prefix, String[] examples) {
        StringBuffer buf = new StringBuffer();
        buf.append(prefix + ": inserted example: \n");
        for(int i = 0; i < examples.length; i++) {
            buf.append(examples[i] + "\n---------------\n");
        }
        System.out.println(buf.toString());
    }

    public static void main(String[] args) throws SAXException, IOException,
            XMLStreamException {
        validate("/Users/oliverpa/Code/seis/sod/relax/sod.rng");
    }
}