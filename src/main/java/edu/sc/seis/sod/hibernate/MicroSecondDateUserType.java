package edu.sc.seis.sod.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.TimestampType;
import org.hibernate.usertype.UserType;
import org.hsqldb.types.Types;

import edu.sc.seis.sod.model.common.MicroSecondDate;


public class MicroSecondDateUserType implements UserType {

    public static final MicroSecondDateUserType INSTANCE = new MicroSecondDateUserType();

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MicroSecondDateUserType.class);

    @Override
    public int[] sqlTypes() {
        return new int[] {TimestampType.INSTANCE.sqlType()};
    }

    @Override
    public Class returnedClass() {
        return MicroSecondDate.class;
    }

    @Override
    public boolean equals(Object x, Object y)
            throws HibernateException {
        return Objects.equals( x, y );
    }

    @Override
    public int hashCode(Object x)
            throws HibernateException {
        return Objects.hashCode( x );
    }

    @Override
    public Object nullSafeGet(
            ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {
        String columnName = names[0];
        Timestamp ts = rs.getTimestamp(columnName);
        if (ts == null) return null;
        else return new MicroSecondDate(ts);
    }

    @Override
    public void nullSafeSet(
            PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        if ( value == null ) {
            st.setNull( index, Types.TIMESTAMP_WITH_TIMEZONE );
        }
        else {
            Timestamp ts = ((MicroSecondDate)value).getTimestamp();
            st.setTimestamp( index, ts );
        }
    }

    @Override
    public Object deepCopy(Object value)
            throws HibernateException {
        return value == null ? null : new MicroSecondDate((MicroSecondDate)value);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value)
            throws HibernateException {
        return (MicroSecondDate) deepCopy( value );
    }

    @Override
    public Object assemble(Serializable cached, Object owner)
            throws HibernateException {
        return deepCopy( cached );
    }

    @Override
    public Object replace(Object original, Object target, Object owner)
            throws HibernateException {
        return deepCopy( original );
    }
}
