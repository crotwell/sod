package edu.sc.seis.sod;

import java.io.StringWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.apache.xml.serialize.XMLSerializer;

import gjb.util.simpleDB.DB;
import gjb.util.simpleDB.Relation;
import gjb.util.simpleDB.Tuple;
import gjb.util.simpleDB.ResultSet;
import gjb.util.simpleDB.InvalidAttributeException;
import gjb.util.simpleDB.InvalidRelationException;
import gjb.util.simpleDB.RelationExistsException;

public class SodCache {

    public SodCache() {
	try {
	    FileReader fileReader = new FileReader("cache.xml");
	    System.out.println("file found");
	    InputSource inputSource = new InputSource(fileReader);
	    db  = new DB(inputSource);
	    System.out.println("db instantiated "+db.toString());
	    relationName = "sodevents";
	} catch(Exception e) {
	    System.out.println("File not found");
	    db = new DB("cache.xml");
	    relationName = "sodevents";
	    System.out.println("db instantiated "+db.toString());
	}
    }

    public void insert(String name, String origin_time) throws Exception{

	Hashtable tuple = new Hashtable();
	tuple.put("name", name);
	tuple.put("origin_time", origin_time);

	db.insert(relationName, tuple);
	System.out.println(db.toString());
	writeToXML();
    }

    public void create(String relationName) throws Exception{

	Vector attr = new Vector();
	attr.addElement("name");
	attr.addElement("origin_time");
	db.createRelation(relationName, attr);
    }

    public void create() throws Exception{
	create("sodevents");

    }

    public boolean get(String name) throws Exception{
	
	if(db == null) System.out.println("the db is null");
	else System.out.println("the db in not null");
	try {
	    ResultSet result = db.select(relationName, " name = '"+name+"'");
	    Tuple tuple = result.getTuple(0);
	    return true;
	} catch(Exception e) {
	    return false;
	}
    }
    public void writeToXML() throws Exception{
	try {
	    // convert the database to its XML representation and print it
	    Document doc = db.toXML();
	    FileWriter writer = new FileWriter("cache.xml");
	    XMLSerializer serializer = new XMLSerializer(writer,null);
	    serializer.serialize(doc);
	    // System.out.println(writer.toString());			
	    writer.close();
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
					
    }
    private DB db;
    private String relationName;
}
