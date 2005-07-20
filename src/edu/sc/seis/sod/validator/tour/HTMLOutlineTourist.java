/*
 * Created on Jul 14, 2004
 */
package edu.sc.seis.sod.validator.tour;

import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.validator.documenter.SchemaDocumenter;
import edu.sc.seis.sod.validator.model.*;

/**
 * @author Charlie Groves
 */
public class HTMLOutlineTourist implements Tourist {

    public HTMLOutlineTourist(String curLoc) {
        this.curLoc = curLoc.replace('\\', '/');
    }

    public void visit(Attribute attr) {
        genericVisit(attr);
        result.append("An attribute named " + attr.getName()
                + " with a value of ");
    }

    public void leave(Attribute attr) {
        genericLeave(attr);
        result.append("\n");
    }

    public void visit(Choice choice) {
        genericVisit(choice);
        result.append(getDefLink(choice) + " " + getCardinality(choice));
        appendIfChildren = " " + getChoiceLink() + "\n<div id=\"choice\">\n";
    }

    public void leave(Choice choice) {
        appendIfChildren = "</div>end " + getDefLink(choice) + " "
                + getCardinality(choice) + getChoiceLink() + "\n<br />";
        appendIfNoChildren = "<br />\n";
        genericLeave(choice);
    }

    private String getChoiceLink() {
        return "<i><a class=\"choice\" href=\"" + getTagDocHelpHREF()
                + "#choice\">choice</a></i> ";
    }

    public void visit(Data d) {
        genericVisit(d);
        result.append("<b>");
        if(d.isFromDef()) {
            result.append(getDefLink(d));
        } else {
            result.append("<a href=\"" + getDatatypeHREF(d) + "\">"
                    + d.getDatatype() + "</a>");
        }
        result.append("</b>");
    }

    public void visit(Value v) {
        genericVisit(v);
        String output = "<b>" + v.getValue() + "</b>";
        if(v.getParent() instanceof MultigenitorForm) {
            output = "<div>" + output + "</div>\n";
        }
        result.append(output);
    }

    public void visit(Empty e) {}

    public void visit(Group g) {
        genericVisit(g);
        String cardinality = getCardinality(g);
        if(!cardinality.equals("")) {
            result.append(getDefLink(g) + getGroupLink() + getCardinality(g)
                    + "\n");
        }
        appendIfChildren = "<div id=\"group\">\n";
    }

    public void leave(Group g) {
        appendIfNoChildren = getDefLink(g) + getCardinality(g) + "<br />\n";
        appendIfChildren = "</div>\n";
        genericLeave(g);
    }

    private String getGroupLink() {
        return "<i><a href=\"" + getTagDocHelpHREF()
                + "#group\">group</a> </i>";
    }

    public void visit(Interleave i) {
        genericVisit(i);
        result.append(getDefLink(i) + getCardinality(i));
        appendIfChildren = getInterLink() + "\n<div id=\"inter\">\n";
    }

    public void leave(Interleave i) {
        appendIfNoChildren = "<br />\n";
        appendIfChildren = "</div>end " + getDefLink(i) + getCardinality(i)
                + getInterLink() + "\n";
        genericLeave(i);
    }

    private String getInterLink() {
        return "<i><a href=\"" + getTagDocHelpHREF()
                + "#interleave\">interleave</a></i>";
    }

    public void visit(NamedElement ne) {
        genericVisit(ne);
        if(ne.getChild() instanceof Empty) {
            result.append("&lt;" + getName(ne) + "/&gt;");
        } else {
            result.append("&lt;" + getName(ne) + "&gt;");
            if(!isData(ne.getChild())) {
                appendIfChildren = " " + getCardinality(ne) + "<div>\n";
            }
        }
    }

    public void leave(NamedElement ne) {
        appendIfNoChildren = " " + getCardinality(ne) + "<br />";
        if(!(ne.getChild() instanceof Empty)) {
            if(!isData(ne.getChild())) {
                appendIfChildren = "</div>\n&lt;/" + getName(ne)
                        + "&gt;<br />\n";
            } else {
                appendIfChildren = "&lt;/" + getName(ne) + "&gt; "
                        + getCardinality(ne) + "<br />\n";
            }
        }
        genericLeave(ne);
    }

    private String getName(NamedElement ne) {
        if(ne.getDef() != null) {
            return getDefLink(ne, ne.getName());
        } else {
            return ne.getName();
        }
    }

    public boolean isData(Form f) {
        return f instanceof Data || f instanceof Value || f instanceof Text;
    }

    public void visit(Text t) {
        genericVisit(t);
        result.append("<b><a href=\"" + getDatatypeHREF(t)
                + "\">Any Text</a></b>");
    }

    public void visit(NotAllowed na) {}

    public String getResult() {
        return result.toString();
    }

    private String getTagDocHelpHREF() {
        return getBasePath() + "ingredients/abstractStructure.html";
    }

    private String getBasePath() {
        String baseString = "../";
        for(int i = 0; i < curLoc.length(); i++) {
            if(curLoc.charAt(i) == '/') {
                baseString += "../";
            }
        }
        return baseString;
    }

    private String getDatatypeHREF(Form d) {
        Class c = d.getClass();
        //add one to get the final package period
        if(d instanceof Data) {
            c = ((Data)d).getDatatype().getClass();
        }
        String classname = c.getName().substring(c.getPackage()
                .getName()
                .length() + 1);
        return getBasePath() + "ingredients/datatypes/" + classname + ".html";
    }

    private String getDefLink(Form f) {
        if(f.getDef() != null) { return getDefLink(f, f.getDef().getName()); }
        return "";
    }

    private String getDefLink(Form f, String name) {
        String path = SchemaDocumenter.makePath(f.getDef()) + ".html";
        String href = SodUtil.getRelativePath(curLoc, path, "/");
        String title = "";
        if(f.getAnnotation().hasSummary()) {
            title = "title=\"" + f.getAnnotation().getSummary() + "\"";
        }
        return "<a href=\"" + href + "\" " + title + ">" + name + "</a>";
    }

    private String getCardinality(Form f) {
        String baseString = getTagDocHelpHREF();
        if(f.getMin() == 0) {
            if(f.getMax() == 1) { return "<i><a href=\"" + baseString
                    + "#optional\">optional</a> </i>"; }
            return "<i><a href=\"" + baseString
                    + "#Any number of times\">Any number of times</a> </i>";
        } else if(f.getMax() > 1) {
            return "<i><a href=\"" + baseString
                    + "#At least once\">At least once</a> </i>";
        } else {
            return "";
        }
    }

    private void genericVisit(Form f) {
        result.append(appendIfChildren);
        appendIfChildren = "";
        appendIfNoChildren = "";
        lastForm = f;
    }

    private void genericLeave(Form f) {
        if(f.equals(lastForm)) {
            result.append(appendIfNoChildren);
        } else {
            result.append(appendIfChildren);
        }
        appendIfNoChildren = "";
        appendIfChildren = "";
    }

    private Form lastForm;

    private String appendIfChildren = "";

    private String appendIfNoChildren = "";

    private String curLoc;

    private StringBuffer result = new StringBuffer();
}