/**
 * SchemaGrammer.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.util.ExpressionWalker;
import com.sun.msv.reader.util.GrammarLoader;
import com.sun.msv.reader.util.IgnoreController;

public class SchemaGrammar {
    public SchemaGrammar(String[] args) throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        InputSource inputSource = new InputSource(args[0] + "/relax/sod.rng");
        grammar = GrammarLoader.loadSchema(inputSource, new OutMsvController(), saxParserFactory);
        if (grammar == null) {
            throw new Exception("Couldn't load the schema");
        }
        buildSchemaModel();
    }

    void buildSchemaModel() {
        root = new ElementNode("root");
        MsvWalker walker = new MsvWalker();
        grammar.getTopLevel().visit(walker);
    }

    public ElementNode getRoot() {
        return root;
    }

    public ElementNode getNode(String tagName) {
        return (ElementNode)visitedMap.get(tagName);
    }

    ElementNode root;

    static HashMap visitedMap = new HashMap();

    class MsvWalker extends ExpressionWalker {
        MsvWalker() {
            this(root);
        }
        MsvWalker(ElementNode parent) {
            this.parent = parent;
        }

        ElementNode parent;

        public void onElement(com.sun.msv.grammar.ElementExp exp) {
            if (exp.getNameClass() instanceof SimpleNameClass) {
                SimpleNameClass simple = (SimpleNameClass) exp.getNameClass();


                ElementNode child;
                boolean childNotVisited;
                if ( ! visitedMap.keySet().contains(simple.localName)) {
                    child = new ElementNode(simple.localName);
                    visitedMap.put(simple.localName, child);
                    childNotVisited = true;
                } else {
                    child = (ElementNode)visitedMap.get(simple.localName);
                    childNotVisited = false;
                }
                if ( ! parent.containsChild(child)) {
                    // new
                    parent.addChild(child);
                }
                if (childNotVisited) {
                    exp.contentModel.visit(new MsvWalker(child));
                }
            }

            //super.onElement(exp);
        }

        /*

         public void onChoice(com.sun.msv.grammar.ChoiceExp exp) {
         System.out.println(parent+" Choice ");
         super.onChoice(exp);
         System.out.println("END "+parent+" CHOICE");
         }

         public void onSequence(com.sun.msv.grammar.SequenceExp p1) {
         System.out.println(parent+" SEQUENCE ");
         super.onSequence(p1);
         System.out.println(parent+" END SEQUENCE");
         }

         public void onBinExp(com.sun.msv.grammar.BinaryExp p1) {}

         public void onMixed(com.sun.msv.grammar.MixedExp p1) {}

         public void onList(com.sun.msv.grammar.ListExp p1) {

         }

         public void onOneOrMore(com.sun.msv.grammar.OneOrMoreExp p1) {
         System.out.println(parent+" OneOrMore ");
         super.onOneOrMore(p1);
         }

         */
    }


    public class OutMsvController extends IgnoreController {
        public void warning(Locator[] locs, String errorMessage) {
            System.out.println("MSV warning: " + errorMessage);
        }

        public void error(Locator[] locs, String errorMessage, Exception nestedException) {
            System.out.println("MSV error: " + errorMessage);
        }
    }

    public static void main(String[] args) throws Exception{
        new SchemaGrammar(args);
        FileOutputStream fos = new FileOutputStream(args[0] + "/src/"+NODE_JAR_LOC);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(visitedMap);
        oos.close();
    }

    public static final String NODE_JAR_LOC = "edu/sc/seis/sod/data/elementNodes.ser";

    Grammar grammar;
}


