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

public class ExampleValidator {

    public static void validate(String exampleContainingSchemaLoc) throws IOException, XMLStreamException, SAXException{
        Validator validator = new Validator(Validator.SOD_SCHEMA_LOC);
        StAXModelBuilder modelBuilder = new StAXModelBuilder(exampleContainingSchemaLoc);
        Annotation[] anns = modelBuilder.getAnnotations();
        ExampleBuilder eBuild;
        File file = new File("tempEx");
        for (int i = 0; i < anns.length; i++) {
            eBuild = new ExampleBuilder(false);
            eBuild.setRequiredExample(anns[i]);
            eBuild.write(modelBuilder.getRoot().getForm());
            String example = eBuild.toString();
            FileWriter fw = new FileWriter(file);
            fw.write(example);
            fw.close();
            InputSource in = new InputSource(new FileInputStream(file));
            if (!validator.validate(in, true)){
                System.out.println("NOT VALID: \n"
                                       + "inserted example: \n "
                                       + anns[i].getExample(false)
                                       + "\n---------------\n"
                                       + example
                                       + "\n---------------\n");
            } else {
                //System.out.println("VALID!!!: \n" + anns[i].getExample(false));
            }
            file.delete();
        }
    }

    public static void main(String[] args) throws SAXException, IOException, XMLStreamException{
        validate("sod.rng");
    }

}

