package edu.sc.seis.sod.validator;

import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.TemplateFileLoader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class RelaxHandler extends DefaultHandler{
    public RelaxHandler(String baseRelaxFile) throws Exception{
        this.file = baseRelaxFile;
        XMLReader xr = XMLReaderFactory.createXMLReader();
        xr.setContentHandler(this);
        URL baseURL = TemplateFileLoader.getUrl(getClass().getClassLoader(),
                                                baseRelaxFile);
        xr.parse(new InputSource(baseURL.openStream()));
        fileToGrammar.put(file, grammar);
        grammar.dereference();
    }

    public Pattern getRoot(){ return grammar.getStart().getChild(); }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes){
        indentedOut("Starting " + localName + " at " + stackPos);
        if(structuralElementNames.contains(localName)){
            createStructuralElement(localName, attributes);
        }else if(localName.equals("start")){ createStart(attributes); }
        else if(localName.equals("define")){ createDefinition(attributes); }
        else if(localName.equals("grammar")){ createGrammar(); }
        else if(localName.equals("ref")){ createRef(attributes); }
        else if(localName.equals("externalRef")){
            createExternalRef(attributes);
        }else if(localName.equals("include")){ includeGrammar(attributes);
        }else{stackPos++;}//unmatched, unloved element just moves the stack
    }

    public void endElement(String uri, String localName, String qName){
        indentedOut("Exiting " + localName + " at " +  --stackPos);
        if(structuralElementNames.contains(localName)){
            currentPattern = currentPattern.getParent();
        }else if(localName.equals("start")){
            grammar.addStart((Start)currentPattern);
        }
    }

    private void createDefinition(Attributes attributes) {
        Definition def = new Definition(grammar, getName(attributes));
        currentPattern = def;
        grammar.addDefinition(def);
        stack[++stackPos] = PATTERN;
    }

    private void createStart(Attributes attributes) {
        currentPattern = Start.createStart(grammar, attributes);
        stack[++stackPos] = PATTERN;
    }

    private void createStructuralElement(String localName, Attributes attributes) {
        Pattern newEl = null;
        if(localName.equals("element")){
            newEl = new NamedElement(grammar, currentPattern, getName(attributes));
        }else if(localName.equals("choice")){
            newEl = new Choice(grammar, currentPattern);
        }else if(localName.equals("zeroOrMore")){
            newEl = new ZeroOrMore(grammar, currentPattern);
        }else if(localName.equals("oneOrMore")){
            newEl = new OneOrMore(grammar, currentPattern);
        }else if(localName.equals("optional")){
            newEl = new Optional(grammar, currentPattern);
        }else if(localName.equals("interleave")){
            newEl = new Interleave(grammar, currentPattern);
        }
        currentPattern.addChild(newEl);
        currentPattern = newEl;
        stack[++stackPos] = PATTERN;
    }

    private void createGrammar() {
        grammar = new GrammarFile(file);
        fileToGrammar.put(file, grammar);
        stack[++stackPos] = GRAMMAR;
    }

    private void createRef(Attributes attributes) {
        Definition def =  grammar.handleReference(getName(attributes));
        currentPattern.addChild(def);
        stackPos++;
    }

    private void createExternalRef(Attributes attributes){
        try {
            Grammar referencedGrammar = getGrammar(attributes.getValue("href"));
            Pattern refEl = referencedGrammar.getStart().getChild();
            currentPattern.addChild(refEl);
            ++stackPos;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void includeGrammar(Attributes attributes) {
        try {
            grammar.include(getGrammar(attributes.getValue("href")));
            ++stackPos;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Grammar getGrammar(String relaxFile)throws Exception{
        relaxFile = SodUtil.getAbsolutePath(file, relaxFile);
        if(!fileToGrammar.containsKey(relaxFile)){ new RelaxHandler(relaxFile);}
        return (Grammar)fileToGrammar.get(relaxFile);
    }

    private boolean inPattern(){ return stack[stackPos] == PATTERN; }

    private boolean inGrammar(){ return stack[stackPos] == GRAMMAR; }

    private void indentedOut(String out){
        for (int i = stackPos; i >= 0; i--) { System.out.print("  "); }
        System.out.println(out);
    }

    private static Set structuralElementNames = new HashSet();
    static {
        structuralElementNames.add("element");
        structuralElementNames.add("choice");
        structuralElementNames.add("zeroOrMore");
        structuralElementNames.add("oneOrMore");
        structuralElementNames.add("optional");
    }

    private static String getName(Attributes attr){
        return attr.getValue("name");
    }

    private static final int GRAMMAR = 0, PATTERN = 1;
    private int[] stack = new int[128];
    private int stackPos = -1;
    private Pattern currentPattern;
    private Grammar grammar;
    private String file;
    private Start rootPattern;
    private static Map fileToGrammar = new HashMap();
    public static String RELAX_NS = "http://relaxng.org/ns/structure/1.0";
}
