package edu.sc.seis.sod.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

import edu.iris.Fissures.model.TimeUtils;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;


public class FTimeUserType implements CompositeUserType {

    private static final int[] TYPES = { Types.TIMESTAMP, Types.INTEGER, Types.INTEGER };

    public int[] sqlTypes() {
        return TYPES;
    }

    public Class returnedClass() {
        return Time.class;
    }

    public boolean equals(Object x, Object y) {
        if (x==y) return true;
        if (x==null || y==null) return false;
        return TimeUtils.areEqual((Time)x, (Time)y);
    }

    public int hashCode(Object x) throws HibernateException {
        Time a = (Time) x;
        return new MicroSecondDate(a).hashCode() + 31 * a.leap_seconds_version; 
    }

    public Object deepCopy(Object x) {
        if (x==null) return null;
        Time xt = (Time)x;
        Time result = new Time();
        result.date_time = xt.date_time;
        result.leap_seconds_version = xt.leap_seconds_version;
        return result;
    }

    public boolean isMutable() { return true; }

    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
    throws HibernateException, SQLException { 

        Timestamp first = (Timestamp) StandardBasicTypes.TIMESTAMP.nullSafeGet(rs, names[0], session);
        if ( first==null ) {System.out.println("WARNING: timestamp in FTimeUserType is null!");return null ;}
        int second = ((Integer)StandardBasicTypes.INTEGER.nullSafeGet(rs, names[1], session)).intValue();
        first.setNanos(second);
        int third = ((Integer)StandardBasicTypes.INTEGER.nullSafeGet(rs, names[2], session)).intValue();
        MicroSecondDate out = new MicroSecondDate(first, third);
        return  out.getFissuresTime();
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
    throws HibernateException, SQLException {
        if (value == null) {System.out.println("WARNING: value in FTimeUserType.nullSafeSet is null!");
        StandardBasicTypes.TIMESTAMP.nullSafeSet(st, null, index, session);
        StandardBasicTypes.INTEGER.nullSafeSet(st, null, index+1, session);
        StandardBasicTypes.INTEGER.nullSafeSet(st, null, index+2, session);
        } else {
            StandardBasicTypes.TIMESTAMP.nullSafeSet(st, new MicroSecondDate((Time)value).getTimestamp(), index, session);
            StandardBasicTypes.INTEGER.nullSafeSet(st, new Integer(new MicroSecondDate((Time)value).getTimestamp().getNanos()), index+1, session);
            StandardBasicTypes.INTEGER.nullSafeSet(st, new Integer(((Time)value).leap_seconds_version), index+2, session);
        }
    }

    public String[] getPropertyNames() {
        return new String[] { "time", "nanos", "leaps" };
    }

    public Type[] getPropertyTypes() {
        return new Type[] { StandardBasicTypes.TIMESTAMP, StandardBasicTypes.INTEGER, StandardBasicTypes.INTEGER };
    }

    public Object getPropertyValue(Object component, int property) {
        switch(property) {
            case 0:
                return new MicroSecondDate((Time)component).getTimestamp();
            case 1:
                return new Integer(new MicroSecondDate((Time)component).getTimestamp().getNanos());
            case 2:
                return new Integer(((Time)component).leap_seconds_version);
                
        }
        return null;
    }

    public void setPropertyValue(
        Object component,
        int property,
        Object value) {
        switch(property) {
            case 0:
                ( (Time) component ).date_time = new MicroSecondDate((Timestamp) value).getFissuresTime().date_time;
                break;
            case 1:
                ( (Time) component ).date_time = new MicroSecondDate(( (Time) component )).add(new TimeInterval(((Integer)value).intValue(), UnitImpl.NANOSECOND)).getFissuresTime().date_time;
                break;
            case 2:
                ( (Time) component ).leap_seconds_version = ((Integer)value).intValue();
                break;
        }
    }

    public Object assemble(
        Serializable cached,
        SessionImplementor session,
        Object owner) {

        return deepCopy(cached);
    }

    public Serializable disassemble(Object value, SessionImplementor session) {
        return (Serializable) deepCopy(value);
    }
    
    public Object replace(Object original, Object target, SessionImplementor session, Object owner) 
    throws HibernateException {
        return original;
    }
}
