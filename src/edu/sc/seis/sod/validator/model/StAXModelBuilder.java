/**
 * StAXModelBuilder.java
 * 
 * @author Charles Groves
 */
package edu.sc.seis.sod.validator.model;

import java.io.IOException;
import java.util.*;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.xml.sax.InputSource;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.validator.model.datatype.*;

public class StAXModelBuilder implements XMLStreamConstants {

    public StAXModelBuilder(String relaxLoc) throws XMLStreamException,
            IOException {
        if(parsedGrammars.containsKey(relaxLoc)) {
            definedGrammar = (Grammar)parsedGrammars.get(relaxLoc);
        } else {
            if(relaxLoc.endsWith("anyXML.rng")) {
                definedGrammar = new Grammar(relaxLoc);
                NamedElement anyXML = new NamedElement(1, 1, "anyXML");
                anyXML.setChild(new Empty(anyXML));
                Definition start = new Definition("", definedGrammar);
                start.set(anyXML);
                definedGrammar.add(start);
            } else {
                ClassLoader cl = getClass().getClassLoader();
                InputSource relaxSource = Start.createInputSource(cl, relaxLoc);
                reader = XMLUtil.staxInputFactory.createXMLStreamReader(relaxSource.getByteStream());
                definedGrammar = new Grammar(relaxLoc);
                reader.next();//SKIP SPACE
                reader.next();//GET TO GRAMMAR START TAG
                try {
                    handleGrammar();
                } catch(XMLStreamException e) {
                    System.out.println("ERROR ON " + relaxLoc);
                    e.printStackTrace();
                    System.exit(0);
                }
                reader.close();
            }
            parsedGrammars.put(relaxLoc, definedGrammar);
            if(waiters.size() > 0) {
                Iterator it = waiters.entrySet().iterator();
                while(it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    definedGrammar.add((String)entry.getKey(),
                                       definedGrammar.getDef((String)entry.getValue()));
                }
            }
        }
    }

    private void handleGrammar() throws XMLStreamException {
        while(reader.hasNext()) {
            switch(reader.next()){
                case START_ELEMENT:
                    String tag = reader.getLocalName();
                    if(tag.equals("start") || tag.equals("define")) {
                        Definition def = handleDef();
                        if(def != null) {
                            definedGrammar.add(def);
                        }
                    } else if(tag.equals("include")) {
                        handleInclude();
                    } else {
                        System.out.println("I DON'T THINK THIS SHOULD BE HERE");
                        whatIs();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void whatIs() {
        switch(reader.getEventType()){
            case START_ELEMENT:
                System.out.println("START " + reader.getLocalName());
                break;
            case END_ELEMENT:
                System.out.println("END " + reader.getLocalName());
                break;
            case COMMENT:
                System.out.println("COMMENT " + reader.getText());
                break;
            default:
                System.out.println("DUNNO");
                break;
        }
    }

    private void handleInclude() {
        //TODO handle inclusion overrides
        definedGrammar.include(getGrammar(getAbsPath()));
    }

    private Definition handleDef() throws XMLStreamException {
        int combo = Definition.UNDEFINED;
        String name = "";
        for(int i = 0; i < reader.getAttributeCount(); i++) {
            if(reader.getAttributeLocalName(i).equals("combine")) {
                String value = reader.getAttributeValue(i);
                if(value.equals("choice")) {
                    combo = Definition.CHOICE;
                } else if(value.equals("interleave")) {
                    combo = Definition.INTERLEAVE;
                }
            } else if(reader.getAttributeLocalName(i).equals("name")) {
                name = reader.getAttributeValue(i);
            }
        }
        Definition def = new Definition(name, definedGrammar, combo);
        nextTag();
        FormProvider result = handleAll();
        def.set(result);
        if(result instanceof Ref) {
            Ref ref = (Ref)result;
            def = ref.getDef();
            waiters.put(name, ref.getName());
        }
        return def;
    }

    private void nextTag() throws XMLStreamException {
        if(reader.hasNext()) {
            int ev = reader.next();
            while(reader.hasNext() && ev != START_ELEMENT && ev != END_ELEMENT) {
                ev = reader.next();
            }
        }
    }

    private FormProvider handleAll() throws XMLStreamException {
        List kids = new ArrayList();
        while(reader.getEventType() == START_ELEMENT) {
            String tag = reader.getLocalName();
            if(isCardinality(tag)) {
                kids.add(handleCardinality());
            } else if(tag.equals("element")) {
                kids.add(handleElement());
            } else if(tag.equals("attribute")) {
                kids.add(handleAttr());
            } else if(isMulitgen(tag)) {
                kids.add(handleMultigen());
            } else if(tag.equals("ref")) {
                kids.add(handleRef());
            } else if(tag.equals("externalRef")) {
                kids.add(handleExtRef());
            } else if(isData(tag)) {
                kids.add(handleData());
            } else {
                System.out.println("SHIT!!  Unknown tag!" + tag + " "
                        + definedGrammar);
                System.exit(0);
                break;
            }
        }
        if(kids.size() == 0) { return null; }//Hopefully an attribute called
        // handleAll
        if(kids.size() == 1) { return (FormProvider)kids.get(0); }
        Group g = new Group(1, 1);
        Iterator it = kids.iterator();
        while(it.hasNext()) {
            g.add((FormProvider)it.next());
        }
        return g;
    }

    private boolean isAnn(String tag) {
        return tag.equals("annotation");
    }

    private Annotation handleAnn() throws XMLStreamException {
        Annotation note = new Annotation();
        boolean hasExample = false;
        if(isAnn(reader.getLocalName())) {
            while(reader.next() != END_ELEMENT
                    || !reader.getLocalName().equals("annotation")) {
                if(reader.getEventType() == START_ELEMENT) {
                    if(reader.getLocalName().equals("summary")) {
                        reader.next();
                        note.setSummary(reader.getText());
                    } else if(reader.getLocalName().equals("description")) {
                        reader.next();
                        note.setDescription(reader.getText());
                    } else if(reader.getLocalName().equals("include")) {
                        reader.next();
                        note.setInclude(true);
                    } else if(reader.getLocalName().equals("example")) {
                        hasExample = true;
                        reader.next();
                        StringBuffer buf = new StringBuffer();
                        int prevEventType = -1;
                        while(reader.getEventType() != END_ELEMENT
                                || !reader.getLocalName().equals("example")) {
                            int curEventType = reader.getEventType();
                            //this if-else block takes care of empty tags
                            if(prevEventType == START_ELEMENT
                                    && curEventType == END_ELEMENT) {
                                buf.setCharAt(buf.length() - 1, ' ');
                                buf.append("/>");
                            } else {
                                buf.append(XMLUtil.readEvent(reader));
                            }
                            reader.next();
                            prevEventType = curEventType;
                        }
                        String example = buf.toString();
                        example = example.replaceAll("\\t", "        ");
                        int j = 0;
                        for(int i = 0; i < example.length(); i++) {
                            if(example.charAt(i) == '\n') {
                                j = 0;
                            } else if(example.charAt(i) != ' ') {
                                break;
                            } else {
                                j++;
                            }
                        }
                        example = example.trim();
                        note.setExample(example.replaceAll("[ \\t]{" + j + "}",
                                                           ""));
                    }
                }
            }
            reader.nextTag();
            if(hasExample) {
                annotations.add(note);
            }
        }
        return note;
    }

    /**
     * Method handleCardinality assumes that the reader has been advanced to a
     * START_ELEMENT with a local name of one of the cardinality elements:
     * zeroOrMore, optional or oneOrMore It returns a FormProvider representing
     * the internals of that cardinality and advances the reader to the next tag
     * past the END_ELEMENT of the cardinality The parent on the returned
     * FormProvider is not set, so this must be handled by the object calling
     * this.
     */
    private FormProvider handleCardinality() throws XMLStreamException {
        //get cardinality based on the tag name
        int min = 1;
        int max = 1;
        String tag = reader.getLocalName();
        if(tag.equals("zeroOrMore") || tag.equals("optional")) {
            min = 0;
        }
        if(tag.equals("zeroOrMore") || tag.equals("oneOrMore")) {
            max = Integer.MAX_VALUE;
        }
        //make sub structure
        reader.nextTag();
        if(isAnn(reader.getLocalName())) {
            Annotation note = handleAnn();
            throw new RuntimeException("Annotation with summary "
                    + note.getSummary()
                    + " not allowed directly in cardinality tag");
        }
        FormProvider result = handleAll();
        //set cardinality on substructure
        if(min == 0) {
            result.setMin(min);
        }
        if(max == Integer.MAX_VALUE) {
            result.setMax(max);
        }
        //advance past the end of cardinality end element and return
        reader.nextTag();
        return result;
    }

    private boolean isCardinality(String tag) {
        return tag.equals("oneOrMore") || tag.equals("zeroOrMore")
                || tag.equals("optional");
    }

    /**
     * Method handleElement assumes that the reader has been advanced to a
     * START_ELEMENT with a local name of element It returns a NamedElement
     * representing that element and its children and advances the reader to the
     * next tag past the END_ELEMENT of the element handle started The parent on
     * the returned FormProvider is not set, so this must be handled by the
     * object calling this.
     */
    private FormProvider handleElement() throws XMLStreamException {
        String name = reader.getAttributeValue(0);
        nextTag();
        NamedElement result = new NamedElement(1, 1, name);
        result.setAnnotation(handleAnn());
        result.setChild(handleAll());
        nextTag();
        return result;
    }

    private Object handleAttr() throws XMLStreamException {
        String name = reader.getAttributeValue(0);
        nextTag();
        Attribute result = new Attribute(1, 1, name);
        result.setAnnotation(handleAnn());
        FormProvider child = handleAll();
        if(child == null) {
            child = new Text();
        } //An empty attribute has text as a value
        result.setChild(child);
        nextTag();
        return result;
    }

    private FormProvider handleMultigen() throws XMLStreamException {
        String tag = reader.getLocalName();
        AbstractMultigenitorForm parent;
        if(tag.equals("choice")) {
            parent = new Choice(1, 1);
        } else if(tag.equals("group")) {
            parent = new Group(1, 1);
        } else if(tag.equals("list")) {
            parent = new DataList(1, 1);
        } else {
            parent = new Interleave(1, 1);
        }
        nextTag();
        Annotation followingNote = handleAnn();
        FormProvider child = handleAll();
        nextTag();
        if(child instanceof AbstractMultigenitorForm) {
            //If the child is an AbstractMultigenitorForm, there were multiple
            //child FormProviders, so suck all of them out of the child and put
            //them in this one
            FormProvider[] myKids = ((AbstractMultigenitorForm)child).getFormProviders();
            for(int i = 0; i < myKids.length; i++) {
                parent.add(myKids[i].copyWithNewParent(parent));
            }
            parent.setAnnotation(followingNote);
            return parent;
        }
        return child;
    }

    private boolean isMulitgen(String tag) {
        return tag.equals("choice") || tag.equals("group")
                || tag.equals("interleave") || tag.equals("list");
    }

    /**
     * Method handleElement assumes that the reader has been advanced to a
     * START_ELEMENT with a local name of ref It returns a Ref object with the
     * name contained in the name value of the ref element and belonging to the
     * current Grammar. It advances the reader to the next tag past the
     * END_ELEMENT tag of the Ref. The parent is not set, so this must be
     * handled by the caller.
     */
    private Ref handleRef() throws XMLStreamException {
        String name = reader.getAttributeValue(0);
        nextTag();
        Ref ref = new Ref(definedGrammar, name);
        ref.setAnnotation(handleAnn());
        nextTag();
        return ref;
    }

    private Ref handleExtRef() throws XMLStreamException {
        String refGramLoc = getAbsPath();
        nextTag();
        Ref ref = new Ref(getGrammar(refGramLoc));
        ref.setAnnotation(handleAnn());
        nextTag();
        return ref;
    }

    private Form handleData() throws XMLStreamException {
        String tag = reader.getLocalName();
        Form result = null;
        if(tag.equals("notAllowed")) {
            result = new NotAllowed();
        } else if(tag.equals("empty")) {
            result = new Empty();
        } else if(tag.equals("text")) {
            result = new Text();
        } else if(tag.equals("value") || tag.equals("data")) {
            ModelDatatype type = handleType();
            if(tag.equals("data")) {
                result = new Data(1, 1, type);
            } else {
                reader.next();
                result = new Value(1, 1, reader.getText(), type);
            }
        } else if(tag.equals("list")) {
            FormProvider listInternals = handleAll();
            if(listInternals instanceof AbstractMultigenitorForm) {}
        }
        nextTag();
        result.setAnnotation(handleAnn());
        while(reader.getEventType() == START_ELEMENT
                && reader.getLocalName().equals("param")) {
            handleParam((Data)result);
        }
        nextTag();
        return result;
    }

    private void handleParam(Data result) throws XMLStreamException {
        //TODO handle params
        while(reader.getEventType() != END_ELEMENT) {
            reader.next();
        }
        reader.next();
    }

    private boolean isData(String tag) {
        return tag.equals("empty") || tag.equals("data") || tag.equals("value")
                || tag.equals("text") || tag.equals("notAllowed");
    }

    private ModelDatatype handleType() {
        if(reader.getAttributeCount() > 0) {
            String type = reader.getAttributeValue(0);
            if(type.equals("float")) {
                return new FloatDatatype();
            } else if(type.equals("string")) {
                return new AnyText();
            } else if(type.equals("double")) {
                return new DoubleDatatype();
            } else if(type.equals("integer")) {
                return new IntegerDatatype();
            } else if(type.equals("nonNegativeInteger")) { return new NonnegativeIntegerDatatype(); }
            return new Token();
        }
        return null;
    }

    private String getAbsPath() {
        String href = reader.getAttributeValue(0);
        String curLoc = definedGrammar.getLoc();
        return SodUtil.getAbsolutePath(curLoc, href);
    }

    public Form getRoot() {
        return definedGrammar.getRoot();
    }

    Grammar getGrammar(String loc) {
        if(!parsedGrammars.containsKey(loc)) {
            try {
                new StAXModelBuilder(loc);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return (Grammar)parsedGrammars.get(loc);
    }

    public Annotation[] getAnnotations() {
        return (Annotation[])annotations.toArray(new Annotation[0]);
    }

    public static Collection getAllDefinitions() {
        Set defs = new HashSet();
        Iterator it = parsedGrammars.values().iterator();
        while(it.hasNext()) {
            Grammar cur = (Grammar)it.next();
            defs.addAll(cur.getDefs());
        }
        return defs;
    }

    private Map waiters = new HashMap();

    private XMLStreamReader reader;

    private Grammar definedGrammar;

    private static Map parsedGrammars = new HashMap();

    private static List annotations = new ArrayList();
}