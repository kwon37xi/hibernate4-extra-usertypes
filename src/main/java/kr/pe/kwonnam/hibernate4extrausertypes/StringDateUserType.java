package kr.pe.kwonnam.hibernate4extrausertypes;


import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.descriptor.JdbcTypeNameMapper;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class StringDateUserType implements UserType, ParameterizedType {
    private Logger log = LoggerFactory.getLogger(StringDateUserType.class);

    public static final String PARAM_FORMAT = "format";
    public static final String DEFAULT_FORMAT = "yyyyMMddHHmmss";

    private AbstractSingleColumnStandardBasicType type;
    private int[] sqlTypes = null;
    private String format;

    @Override
    public void setParameterValues(Properties parameters) {
        format = parameters.getProperty(PARAM_FORMAT, DEFAULT_FORMAT);
        populateSqlTypes();
    }

    private void populateSqlTypes() {
        TypeResolver tr = new TypeResolver();
        String stringClassName = String.class.getName();

        // type은 org.hibernate.type.StringType 으로 사실상 고정값임.
        type = (AbstractSingleColumnStandardBasicType) tr.basic(stringClassName);
        sqlTypes = new int[]{type.sqlType()};
    }

    @Override
    public int[] sqlTypes() {
        return sqlTypes;
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
        String stringDate = (String) type.get(rs, names[0], session);
        if (stringDate == null) { // 데이터에 null이 아닌 empty가 있다면 empty 체크도 해야함.
            log.trace("Found [{}] as column [{}] original value [{}]", null, names[0], stringDate);
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date parsedDate = sdf.parse(stringDate);
            log.trace("Found [{}] as column [{}] original value [{}]", parsedDate, names[0], stringDate);
            return parsedDate;
        } catch (Exception ex) {
            throw new HibernateException("Failed to parse [" + stringDate + "] with [" + format + "].", ex);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        try {
            String formattedDate = null;
            if (value != null) {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                formattedDate = sdf.format((Date) value);
            }
            log.trace("binding parameter [{}] as [{}] - [{}] original value [{}]", index, JdbcTypeNameMapper.getTypeName(sqlTypes[0]), formattedDate, value);
            st.setObject(index, formattedDate);
        } catch (Exception ex) {
            throw new HibernateException("Failed to format date object to string.", ex);
        }
    }

    String getFormat() {
        return format;
    }

    public static Object dateDeepCopy(Date date) {
        if (date == null) {
            return null;
        }
        return new Date(date.getTime());
    }
}

