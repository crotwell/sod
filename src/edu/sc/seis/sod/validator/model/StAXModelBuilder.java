/**
 * StAXModelBuilder.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import java.util.*;

import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.validator.model.datatype.AnyText;
import edu.sc.seis.sod.validator.model.datatype.DoubleDatatype;
import edu.sc.seis.sod.validator.model.datatype.FloatDatatype;
import edu.sc.seis.sod.validator.model.datatype.Token;
import java.io.IOException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.xml.sax.InputSource;

public class StAXModelBuilder implements XMLStreamConstants{
    public StAXModelBuilder(String relaxLoc) throws XMLStreamException, IOException{
        if(parsedGrammars.containsKey(relaxLoc)){
            definedGrammar = (Grammar)parsedGrammars.get(relaxLoc);
        }else{
            if(relaxLoc.endsWith("anyXML.rng")){
                definedGrammar = new Grammar(relaxLoc);
                NamedElement anyXML = new NamedElement(1, 1, "anyXML");
                anyXML.setChild(new Empty(anyXML));
                Definition start = new Definition("", definedGrammar);
                start.set(anyXML);
                definedGrammar.addStart(start);
            }else{
                ClassLoader cl = getClass().getClassLoader();
                InputSource relaxSource = Start.createInputSource(cl, relaxLoc);
                reader = XMLUtil.staxInputFactory.createXMLStreamReader(relaxSource.getByteStream());
                definedGrammar = new Grammar(relaxLoc);
                reader.next();//SKIP SPACE
                reader.next();//GET TO GRAMMAR START TAG
                try {
                    handleGrammar();
                } catch (XMLStreamException e) {
                    System.out.println("ERROR ON " + relaxLoc);
                    e.printStackTrace();
                    System.exit(0);
                }
                reader.close();
            }
            parsedGrammars.put(relaxLoc, definedGrammar);
        }
    }

    private void handleGrammar() throws XMLStreamException{
        while(reader.hasNext()){
            switch (reader.next()) {
                case START_ELEMENT:
                    String tag = reader.getLocalName();
                    if(tag.equals("start")){
                        definedGrammar.addStart(handleDef());
                    }else if(tag.equals("define")){
                        definedGrammar.add(handleDef());
                    }else if(tag.equals("include")){ handleInclude(); }
                    else{
                        System.out.println("I DON'T THINK THIS SHOULD BE HERE");
                        whatIs();
                    }
                    break;
                default :
                    break;
            }
        }
    }

    public void whatIs(){
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

    private Definition handleDef() throws XMLStreamException{
        int combo = Definition.UNDEFINED;
        String name = "";
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            if(reader.getAttributeLocalName(i).equals("combine")){
                String value = reader.getAttributeValue(i);
                if(value.equals("choice")){ combo = Definition.CHOICE; }
                else if(value.equals("interleave")){ combo = Definition.INTERLEAVE; }
            }else if(reader.getAttributeLocalName(i).equals("name")){
                name = reader.getAttributeValue(i);
            }
        }
        Definition def = new Definition(name, definedGrammar, combo);
        nextTag();
        def.set(handleAll());
        return def;
    }

    private void nextTag() throws XMLStreamException {
        if(reader.hasNext()){
            int ev = reader.next();
            while(reader.hasNext() && ev != START_ELEMENT && ev != END_ELEMENT){
                ev = reader.next();
            }

        }
    }

    private FormProvider handleAll() throws XMLStreamException{
        List kids = new ArrayList();
        Annotation note = null;
        while(reader.getEventType() == START_ELEMENT){
            String tag = reader.getLocalName();
            if(isCardinality(tag)){ kids.add(handleCardinality()); }
            else if(tag.equals("element")){ kids.add(handleElement()); }
            else if(tag.equals("attribute")){ kids.add(handleAttr()); }
            else if(isMulitgen(tag)){ kids.add(handleMultigen()); }
            else if(tag.equals("ref")){ kids.add(handleRef()); }
            else if(tag.equals("externalRef")){ kids.add(handleExtRef()); }
            else if(isData(tag)){ kids.add(handleData()); }
            else if(tag.equals("annotation")){
                note = handleAnn();
                continue;
            }
            else{
                System.out.println("SHIT!!  Unknown tag!" + tag + " " + definedGrammar);
                System.exit(0);

                break;
            }
            if(note != null){
                ((FormProvider)kids.get(kids.size() - 1)).setAnnotation(note);
                note = null;
            }
        }
        if(kids.size() == 1){ return (FormProvider)kids.get(0); }
        Group g = new Group(1, 1);
        Iterator it = kids.iterator();
        while(it.hasNext()){
            g.add((FormProvider)it.next());
        }
        return g;
    }

    private Annotation handleAnn() throws XMLStreamException {
        Annotation note = new Annotation();
        while(reader.next() != END_ELEMENT ||
              ! reader.getLocalName().equals("annotation")){
            if(reader.getEventType() == START_ELEMENT){
                if(reader.getLocalName().equals("summary")){
                    reader.next();
                    note.setSummary(reader.getText());
                }else if(reader.getLocalName().equals("description")){
                    reader.next();
                    note.setDescription(reader.getText());
                }
            }
        }
        reader.nextTag();
        return note;
    }

    /**
     * Method handleCardinality assumes that the reader has been advanced to
     * a START_ELEMENT with a local name of one of the cardinality elements:
     * zeroOrMore, optional or oneOrMore
     *
     * It returns a FormProvider representing the internals of that cardinality
     * and advances the reader to the next tag past the END_ELEMENT of the
     * cardinality handle started
     *
     * The parent on the returned FormProvider is not set, so this must be
     * handled by the object calling this.
     */
    private FormProvider handleCardinality() throws XMLStreamException{
        //get cardinality based on the tag name
        int min = 1;
        int max = 1;
        String tag = reader.getLocalName();
        if(tag.equals("zeroOrMore") || tag.equals("optional")){ min = 0; }
        if(tag.equals("zeroOrMore") || tag.equals("oneOrMore")){
            max = Integer.MAX_VALUE;
        }

        //make sub structure
        reader.nextTag();
        FormProvider result = handleAll();

        //set cardinality on substructure
        if(min == 0){ result.setMin(min); }
        if(max == Integer.MAX_VALUE){ result.setMax(max); }

        //advance past the end of cardinality end element and return
        reader.nextTag();
        return result;
    }

    private boolean isCardinality(String tag){
        return tag.equals("oneOrMore") || tag.equals("zeroOrMore") ||
            tag.equals("optional");
    }

    /**
     * Method handleElement assumes that the reader has been advanced to
     * a START_ELEMENT with a local name of element
     *
     * It returns a NamedElement representing that element and its children
     * and advances the reader to the next tag past the END_ELEMENT of the
     * element handle started
     *
     * The parent on the returned FormProvider is not set, so this must be
     * handled by the object calling this.
     */
    private FormProvider handleElement() throws XMLStreamException{
        String name = reader.getAttributeValue(0);
        nextTag();
        NamedElement result = new NamedElement(1, 1, name);
        result.setChild(handleAll());
        nextTag();
        return result;
    }

    private Object handleAttr() throws XMLStreamException {
        String name = reader.getAttributeValue(0);
        nextTag();
        Attribute result = new Attribute(1, 1, name);
        result.setChild(handleAll());
        nextTag();
        return result;
    }

    private FormProvider handleMultigen() throws XMLStreamException{
        String tag = reader.getLocalName();
        AbstractMultigenitorForm parent;
        if(tag.equals("choice")){ parent =  new Choice(1, 1); }
        else if(tag.equals("group")){ parent = new Group(1, 1); }
        else if(tag.equals("list")){ parent = new DataList(1, 1); }
        else{ parent = new Interleave(1, 1); }
        nextTag();
        FormProvider child = handleAll();
        nextTag();
        if(child instanceof AbstractMultigenitorForm){
            //If the child is an AbstractMultigenitorForm, there were multiple
            //child FormProviders, so suck all of them out of the child and put
            //them in this one
            FormProvider[] myKids = ((AbstractMultigenitorForm)child).getFormProviders();
            for (int i = 0; i < myKids.length; i++) {
                parent.add(myKids[i].copyWithNewParent(parent));
            }
            return parent;
        }else{
            return child;
        }
    }

    private boolean isMulitgen(String tag){
        return tag.equals("choice") || tag.equals("group") ||
            tag.equals("interleave") || tag.equals("list");
    }

    /**
     * Method handleElement assumes that the reader has been advanced to
     * a START_ELEMENT with a local name of ref
     *
     * It returns a Ref object with the name contained in the name value of the
     * ref element and belonging to the current Grammar.  It advances the reader
     * to the next tag past the END_ELEMENT tag of the Ref.  The parent is not
     * set, so this must be handled by the caller.
     */
    private Ref handleRef() throws XMLStreamException{
        String name = reader.getAttributeValue(0);
        nextTag();
        nextTag();
        return new Ref(definedGrammar, name);
    }


    private Ref handleExtRef() throws XMLStreamException{
        String refGramLoc = getAbsPath();
        nextTag();
        nextTag();
        return new Ref(getGrammar(refGramLoc));
    }

    private Object handleData() throws XMLStreamException {
        String tag = reader.getLocalName();
        Form result = null;
        if(tag.equals("notAllowed")){ result = new NotAllowed(); }
        else if(tag.equals("empty")){ result = new Empty(); }
        else if(tag.equals("text")){ result = new Text(); }
        else if(tag.equals("value") || tag.equals("data")){
            ModelDatatype type = handleType();
            if(tag.equals("data")){ result = new Data(1, 1, type); }
            else{
                reader.next();
                result = new Value(1, 1, reader.getText(), type);
            }
        }else if(tag.equals("list")){
            FormProvider listInternals = handleAll();
            if(listInternals instanceof AbstractMultigenitorForm){

            }
        }
        nextTag();
        while(reader.getEventType() == START_ELEMENT &&
              reader.getLocalName().equals("param")){
            handleParam((Data)result);
        }
        nextTag();
        return result;
    }

    private void handleParam(Data result) throws XMLStreamException {
        //TODO handle params
        while(reader.getEventType() != END_ELEMENT){ reader.next(); }
        reader.next();
    }

    private boolean isData(String tag) {
        return tag.equals("empty") || tag.equals("data") || tag.equals("value") ||
            tag.equals("text") || tag.equals("notAllowed");
    }

    private ModelDatatype handleType(){
        if(reader.getAttributeCount() > 0){
            String type = reader.getAttributeValue(0);
            if(type.equals("float")){
                return new FloatDatatype();
            }else if(type.equals("string")){ return new AnyText(); }
            else if(type.equals("double")){ return new DoubleDatatype(); }
            return new Token();
        }
        return null;
    }

    private String getAbsPath(){
        String href = reader.getAttributeValue(0);
        String curLoc = definedGrammar.getLoc();
        return SodUtil.getAbsolutePath(curLoc, href);
    }

    public Form getRoot(){ return definedGrammar.getRoot(); }

    Grammar getGrammar(String loc) {
        if(!parsedGrammars.containsKey(loc)){
            try {
                new StAXModelBuilder(loc);
            } catch(Exception e) { e.printStackTrace();}
        }
        return (Grammar)parsedGrammars.get(loc);
    }

    public static Collection getAllDefinitions(){
        Set defs = new HashSet();
        Iterator it = parsedGrammars.values().iterator();
        while(it.hasNext()){
            Grammar cur = (Grammar)it.next();
            defs.addAll(cur.getDefs());
        }
        return defs;
    }

    private XMLStreamReader reader;
    private Grammar definedGrammar;
    private static Map parsedGrammars = new HashMap();
}
