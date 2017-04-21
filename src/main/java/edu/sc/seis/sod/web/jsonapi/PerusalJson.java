package edu.sc.seis.sod.web.jsonapi;


public class PerusalJson extends AbstractJsonApiData {

    public PerusalJson(String id, String baseUrl) {
        super(baseUrl);
        this.id = id;
    }

    @Override
    public String getType() {
        return PERUSAL;
    }

    @Override
    public String getId() {
        return id;
    }
    
    String id;
    
    String order;

    public static final String PERUSAL = "perusals";
    
}
