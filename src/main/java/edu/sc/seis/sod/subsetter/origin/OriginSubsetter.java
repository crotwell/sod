package edu.sc.seis.sod.subsetter.origin;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * OriginSubsetter.java
 *
 *
 * Created: Tue Apr  2 13:32:13 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public interface OriginSubsetter extends Subsetter{
    public StringTree accept(CacheEvent eventAccess, EventAttrImpl eventAttr, OriginImpl preferred_origin)
        throws Exception;
}// OriginSubsetter
