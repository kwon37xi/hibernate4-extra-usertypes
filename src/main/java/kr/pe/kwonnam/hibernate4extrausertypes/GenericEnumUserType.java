package kr.pe.kwonnam.hibernate4extrausertypes;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.descriptor.JdbcTypeNameMapper;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.jboss.logging.Logger;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

import static java.lang.String.format;

/**
 * GenericEnumUserType is for saving/reading enum data from db column.
 * But column data can be different from enum name.
 *
 * @see <a href="https://developer.jboss.org/wiki/Java5EnumUserType">https://developer.jboss.org/wiki/Java5EnumUserType</a>
 */
public class GenericEnumUserType implements UserType, ParameterizedType {
    private static final CoreMessageLogger LOGGER = Logger.getMessageLogger(CoreMessageLogger.class, GenericEnumUserType.class.getName());
    private static final CoreMessageLogger BINDER_LOGGER = Logger.getMessageLogger(CoreMessageLogger.class, BasicBinder.class.getName());
    private static final CoreMessageLogger EXTRACTOR_LOGGER = Logger.getMessageLogger(CoreMessageLogger.class, BasicExtractor.class.getName());

    /**
     * 대상 enum 클래스 지정 파라미터 이름
     */
    public static final String PARAM_ENUM_CLASS = "enumClass";

    /**
     * Enum을 실제 저장할 문자열로 바꿔주는 메소드를 지정하는 파라미터 이름.
     * 이 메소드의 리턴타입은 PARAM_VALUE_OF_METHOD로 지정된 메소드의 파라미터와 같은 타입이어야 한다.
     */
    public static final String PARAM_IDENTIFIER_METHOD = "identifierMethod";
    private static final String DEFAULT_IDENTIFIER_METHOD_NAME = "name";

    /**
     * 저장된 데이터로부터 Enum을 리턴해주는 메소드를 지정하는 파라미터 이름.
     * 메소드의 파라미터 타입은 PARAM_IDENTIFIER_METHOD로 지정된 메소드의 리턴 타입과 같아야 한다.
     */
    public static final String PARAM_VALUE_OF_METHOD = "valueOfMethod";
    private static final String DEFAULT_VALUE_OF_METHOD_NAME = "valueOf";

    /**
     * enum class
     **/
    private Class<? extends Enum> enumClass;

    /**
     * enum 값 저장 DB Column에 매칭되는 Java Type
     */
    private Class<?> identifierType;

    /**
     * enum 값을 DB Column Type에 맞게 변환해줄 메소드
     */
    private Method identifierMethod;

    /**
     * DB Column에서 읽은 값을 enum 으로 변환해줄 메소드. static 으로 선언돼 있어야 한다.
     */
    private Method valueOfMethod;

    /**
     * enum 값 저장 DB Column에 매칭되는 Hibernate Type
     */
    private AbstractSingleColumnStandardBasicType type;

    /**
     * enum 값 저장 DB Column에 매칭되는 JDBC Type
     */
    private int[] sqlTypes;

    @Override
    public void setParameterValues(Properties parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters must not be null.");
        }

        populateEnumClass(parameters);
        populateIdentifierMethodAndType(parameters);
        populateHibernateType();
        populateValueOfMethod(parameters);

        LOGGER.debugv("GenericEnumUserType for enumClass {0} parameters initialized. identifierMethod : {1}, identifierType : {2}, valueOfMethod : {3}",
            enumClass.getName(), identifierMethod, identifierType.getName(), valueOfMethod);
    }

    private void populateEnumClass(Properties parameters) {
        String enumClassName = parameters.getProperty(PARAM_ENUM_CLASS);

        try {
            enumClass = Class.forName(enumClassName).asSubclass(Enum.class);
        } catch (ClassNotFoundException exception) {
            throw new HibernateException(format("Enum class(%s) not found.", enumClassName), exception);
        }
    }

    private void populateIdentifierMethodAndType(Properties parameters) {
        String identifierMethodName = parameters.getProperty(PARAM_IDENTIFIER_METHOD, DEFAULT_IDENTIFIER_METHOD_NAME);

        try {
            identifierMethod = enumClass.getMethod(identifierMethodName, new Class[0]);
            identifierType = identifierMethod.getReturnType();
        } catch (Exception exception) {
            throw new HibernateException(format("Failed to obtain identifier method(%s).", identifierMethodName), exception);
        }
    }

    private void populateHibernateType() {
        TypeResolver tr = new TypeResolver();
        type = (AbstractSingleColumnStandardBasicType) tr.basic(identifierType.getName());

        if (type == null) {
            throw new HibernateException("Unsupported identifier type " + identifierType.getName());
        }
        sqlTypes = new int[]{type.sqlType()};
    }

    private void populateValueOfMethod(Properties parameters) {
        String valueOfMethodName = parameters.getProperty(PARAM_VALUE_OF_METHOD, DEFAULT_VALUE_OF_METHOD_NAME);

        try {
            valueOfMethod = enumClass.getMethod(valueOfMethodName, new Class[]{identifierType});
        } catch (Exception exception) {
            throw new HibernateException(format("Failed to obtain valueOf method(%s) with identifierType(%s).", valueOfMethodName, identifierType.getName()), exception);
        }
    }

    public Class<? extends Enum> getEnumClass() {
        return enumClass;
    }

    public Class<?> getIdentifierType() {
        return identifierType;
    }

    public Method getIdentifierMethod() {
        return identifierMethod;
    }

    public Method getValueOfMethod() {
        return valueOfMethod;
    }

    public AbstractSingleColumnStandardBasicType getType() {
        return type;
    }

    @Override
    public int[] sqlTypes() {
        return Arrays.copyOf(sqlTypes, sqlTypes.length);
    }

    @Override
    public Class returnedClass() {
        return enumClass;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        Object identifier = type.nullSafeGet(rs, names[0], session);

        if (rs.wasNull()) {
            return null;
        }

        try {
            Object resultValue = valueOfMethod.invoke(enumClass, new Object[]{identifier});
            EXTRACTOR_LOGGER.tracev("Found [{0}] as column [{1}] original value [{2}]", resultValue, names[0], identifier);
            return resultValue;
        } catch (Exception exception) {
            throw new HibernateException(format("Exception while invoking valueOf method '%s' of enumeration class '%s'.", valueOfMethod.getName(), enumClass), exception);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        try {
            if (value == null) {
                st.setNull(index, type.sqlType());
                return;
            }

            Object identifier = identifierMethod.invoke(value, new Object[0]);
            BINDER_LOGGER.tracev("binding parameter [{0}] as [{1}] - [{2}] original value [{3}]",
                index, JdbcTypeNameMapper.getTypeName(sqlTypes[0]), identifier, value);
            type.nullSafeSet(st, identifier, index, session);
        } catch (Exception exception) {
            throw new HibernateException(format("Exception while invoking identifierMethod '%s' of enumeration class '%s'.", identifierMethod.getName(), enumClass), exception);
        }
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
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return Objects.hashCode(x);
    }

    public boolean isMutable() {
        return false;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
