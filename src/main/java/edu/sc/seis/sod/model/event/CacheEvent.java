package edu.sc.seis.sod.model.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CacheEvent.java Created: Mon Jan 8 16:33:52 2001
 * 
 * @author Philip Crotwell
 * @version
 */
public class CacheEvent  {

    /** for use by hibernate */
    protected CacheEvent() {}

    
    /**
     * Initializes the origins array to be just the single prefferred origin.
     */
    public CacheEvent(EventAttrImpl attr, OriginImpl preferred) {
        this(attr, new OriginImpl[] {preferred}, preferred);
    }

    public CacheEvent(EventAttrImpl attr, OriginImpl[] origins, OriginImpl preferred) {
        if(attr == null) { throw new IllegalArgumentException("EventAttr cannot be null"); }
        if(origins == null) { throw new IllegalArgumentException("origins cannot be null"); }
        this.attr = attr;
        this.origins = origins;
        this.preferred = preferred;
    }

    protected CacheEvent event;
    @Deprecated
    public CacheEvent(CacheEvent event) {
        if(event == null) { throw new IllegalArgumentException("EventAccess cannot be null"); }
        this.event = event;
        get_attributes();
        try {
            getPreferred();
        } catch (NoPreferredOrigin e) {
            // oh well...
        }
    }

    public EventAttrImpl get_attributes() {
        if(attr == null && event != null) {
            this.attr = event.get_attributes();
        }
        if(attr == null) {
            // remote doesn't implement
            attr = EventAttrImpl.createEmpty();
        }
        return attr;
    }

    public EventAttrImpl getAttributes() {
        return (EventAttrImpl)get_attributes();
    }

    public OriginImpl[] get_origins() {
        if(origins == null && event != null) {
            origins = event.get_origins();
        }
        if (origins == null) {
            try {
                origins = new OriginImpl[] {getPreferred()};
            } catch(NoPreferredOrigin e) {
                // shouldn't happen
                origins = new OriginImpl[0];
            }
        }
        return origins;
    }
    
    public OriginImpl[] getOrigins() {
        return get_origins();
    }
    
    /** for use by hibernate */
    protected void setOrigins(OriginImpl[] origins) {
        this.origins = origins;
    }

    public OriginImpl get_origin(String the_origin) throws OriginNotFound {
        if(event != null) {
            return event.get_origin(the_origin);
        } else {
            for(int i = 0; i < origins.length; i++) {
                if(origins[i].get_id().equals(the_origin)) { return origins[i]; }
            }
        }
        throw new OriginNotFound();
    }

    public OriginImpl get_preferred_origin() throws NoPreferredOrigin {
        return getPreferred();
    }

    public OriginImpl getPreferred() throws NoPreferredOrigin {
        if(preferred == null) {
            if(event != null) {
                preferred = (OriginImpl)event.get_preferred_origin();
            } else {
                throw new NoPreferredOrigin();
            }
        }
        return preferred;
    }
    
    protected void setPreferred(OriginImpl o) {
        this.preferred = o;
    }

    /**
     * @return true if both the attributes and the preferred origin are cached
     */
    public boolean isLoaded() {
        return attr != null && preferred != null;
    }

    public boolean hasDbid(){
        return dbid > -1;
    }
    
    public int getDbid() {
        return dbid;
    }

    public void setDbid(int id) {
        dbid = id;
    }

    /**
     * This gets around the NoPreferredOrigin exception
     */
    @Deprecated
    public OriginImpl extractOrigin() {
        try {
            return (OriginImpl)get_preferred_origin();
        } catch(NoPreferredOrigin e) {
            logger.info("No preferred origin in event.  Trying get_origins instead");
            OriginImpl[] oArray = get_origins();
            if(oArray.length > 0) {
                return (OriginImpl)oArray[0];
            }
            throw new RuntimeException("No preferred origin", e);
        }
    }
    
    private int dbid;

    protected EventAttrImpl attr;

    protected OriginImpl[] origins;

    protected OriginImpl preferred;

    private static final Logger logger = LoggerFactory.getLogger(CacheEvent.class);
    
    @Deprecated
    public OriginImpl getOrigin() {
        return extractOrigin();
    }
    
    public String toString() {
        try {
            return getPreferred().getOriginTime()+" "+getPreferred().getMagnitudeList().get(0);
        } catch (NoPreferredOrigin e) {
            return "Event "+getAttributes().getName()+" no pref origin";
        }
    }
} // CacheEvent
