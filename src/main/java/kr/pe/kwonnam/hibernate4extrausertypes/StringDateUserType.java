package kr.pe.kwonnam.hibernate4extrausertypes;


import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class StringDateUserType implements UserType, ParameterizedType {
    public static final int SQL_TYPE = Types.VARCHAR;

    public static final String PARAM_FORMAT = "format";
    public static final String DEFAULT_FORMAT = "yyyyMMddHHmmss";

    private String format;

    String getFormat() {
        return format;
    }

    @Override
    public void setParameterValues(Properties parameters) {
        format = parameters.getProperty(PARAM_FORMAT, DEFAULT_FORMAT);
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{SQL_TYPE};
    }

    @Override
    public Class returnedClass() {
        return Date.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return Objects.hashCode(x);
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return dateDeepCopy((Date) value);
    }

    @Override
    public boolean isMutable() {
        return true; // java.util.Date is mutable
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        // TODO : Date는 mutable이라서 deep copy가 맞는듯. 확인 필요.
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        // TODO : Date는 mutable이라서 deep copy가 맞는듯. 확인 필요.
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return dateDeepCopy((Date) original);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        String stringDate = StandardBasicTypes.STRING.nullSafeGet(rs, names[0], session);
        if (stringDate == null) { // 데이터에 null이 아닌 empty가 있다면 empty 체크도 해야함.
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(stringDate);
        } catch (Exception ex) {
            throw new HibernateException("Failed to parse [" + stringDate + "] with [" + format + "].", ex);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            StandardBasicTypes.STRING.nullSafeSet(st, null, index, session);
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);

            final String formattedDate = sdf.format((Date) value);
            StandardBasicTypes.STRING.nullSafeSet(st, formattedDate, index, session);
        } catch (Exception ex) {
            throw new HibernateException("Failed to format date object to string.", ex);
        }
    }

    public static Object dateDeepCopy(Date date) {
        if (date == null) {
            return null;
        }
        return new Date(date.getTime());
    }
}
