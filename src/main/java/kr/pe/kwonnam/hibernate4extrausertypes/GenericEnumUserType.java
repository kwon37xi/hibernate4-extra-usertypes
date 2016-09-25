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
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class GenericEnumUserType implements UserType, ParameterizedType {
    private Logger log = LoggerFactory.getLogger(GenericEnumUserType.class);

    /**
     * 대상 enum 클래스를 문자열로 지정한다
     */
    public static final String PARAM_ENUM_CLASS = "enumClass";

    /**
     * Enum을 실제 저장할 문자열로 바꿔주는 메소드. 이 메소드의 리턴타입은 PARAM_VALUE_OF_METHOD로 지정된 메소드의 파라미터와 같은 타입이어야 한다.
     */
    public static final String PARAM_IDENTIFIER_METHOD = "identifierMethod";
    private static final String DEFAULT_IDENTIFIER_METHOD_NAME = "name";

    /**
     * 저장된 데이터로부터 Enum을 리턴해주는 메소드. 이 메소드의 파라미터 타입은 PARAM_IDENTIFIER_METHOD로 지정된 메소드의 리턴 타입과 같아야 한다.
     */
    public static final String PARAM_VALUE_OF_METHOD = "valueOfMethod";
    private static final String DEFAULT_VALUE_OF_METHOD_NAME = "valueOf";

    private Class<? extends Enum> enumClass;
    private Class<?> identifierType;
    private Method identifierMethod;
    private Method valueOfMethod;
    private AbstractSingleColumnStandardBasicType type;
    private int[] sqlTypes;

    @Override
    public void setParameterValues(Properties parameters) {
        String enumClassName = parameters.getProperty(PARAM_ENUM_CLASS);

        try {
            enumClass = Class.forName(enumClassName).asSubclass(Enum.class);
        } catch (ClassNotFoundException exception) {
            throw new HibernateException("Enum class not found", exception);
        }

        String identifierMethodName = parameters.getProperty(PARAM_IDENTIFIER_METHOD, DEFAULT_IDENTIFIER_METHOD_NAME);

        try {
            identifierMethod = enumClass.getMethod(identifierMethodName, new Class[0]);
            identifierType = identifierMethod.getReturnType();
        } catch (Exception exception) {
            throw new HibernateException("Failed to optain identifier method", exception);
        }

        TypeResolver tr = new TypeResolver();
        type = (AbstractSingleColumnStandardBasicType) tr.basic(identifierType.getName());

        if (type == null) {
            throw new HibernateException("Unsupported identifier type " + identifierType.getName());
        }
        sqlTypes = new int[]{type.sqlType()};

        String valueOfMethodName = parameters.getProperty(PARAM_VALUE_OF_METHOD, DEFAULT_VALUE_OF_METHOD_NAME);

        try {
            valueOfMethod = enumClass.getMethod(valueOfMethodName, new Class[]{identifierType});
        } catch (Exception exception) {
            throw new HibernateException("Failed to optain valueOf method",
                exception);
        }
    }

    @Override
    public Class returnedClass() {
        return enumClass;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        Object identifier = type.get(rs, names[0], session);
        if (rs.wasNull()) {
            return null;
        }

        try {
            Object resultValue = valueOfMethod.invoke(enumClass, new Object[]{identifier});
            log.trace("Found [{}] as column [{}] original value [{}]", resultValue, names[0], identifier);
            return resultValue;
        } catch (Exception exception) {
            throw new HibernateException("Exception while invoking valueOfMethod of enumeration class: ",
                exception);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        try {
            if (value == null) {
                st.setNull(index, type.sqlType());
            } else {
                Object identifier = value != null ? identifierMethod.invoke(value, new Object[0]) : null;
                log.trace("binding parameter [{}] as [{}] - [{}] original value [{}]", index, JdbcTypeNameMapper.getTypeName(sqlTypes[0]), identifier, value);
                type.set(st, identifier, index, session);
            }
        } catch (Exception exception) {
            throw new HibernateException("Exception while invoking identifierMethod of enumeration class: ",
                exception);
        }
    }

    @Override
    public int[] sqlTypes() {
        return sqlTypes;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public boolean isMutable() {
        return false;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
