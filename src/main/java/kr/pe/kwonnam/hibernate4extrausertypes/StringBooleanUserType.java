package kr.pe.kwonnam.hibernate4extrausertypes;


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
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

import static java.lang.String.format;

public class StringBooleanUserType implements UserType, ParameterizedType {
    private Logger log = LoggerFactory.getLogger(StringBooleanUserType.class);

    public static final String PARAM_TRUE_VALUE = "trueValue";
    public static final String DEFAULT_TRUE_VALUE = "Y";
    public static final String PARAM_FALSE_VALUE = "falseValue";
    public static final String DEFAULT_FALSE_VALUE = "N";

    /**
     * DB상의 데이터가 알 수 없는 값일때(빈 문자열 포함) unknownResult는
     * "true", "false", "null"을 문자열로 기입한다.
     * "null"이 기본값이며 이때는 알 수 없는 값이 들어오면 null을 리턴하고,
     * "true", "false"로 할 경우 해당 Boolean으로 변환한다.
     */
    public static final String PARAM_UNKNOWN_RESULT = "unknownResult";
    public static final String DEFAULT_UNKNOWN_RESULT = "null";

    /**
     * 대소문자 무시할 지 여부를 true/false로 지정한다.
     * 기본값은 true로 대소문자를 무시하고 비교한다.
     */
    public static final String PARAM_IGNORE_CASE = "ignoreCase";
    public static final String DEFAULT_IGNORE_CASE = "true";

    private AbstractSingleColumnStandardBasicType type;
    private int[] sqlTypes = null;

    private String trueValue = null;
    private String falseValue = null;
    private Boolean unknownResult = null;
    private boolean ignoreCase = true;

    @Override
    public void setParameterValues(Properties parameters) {
        if (parameters == null) {
            parameters = new Properties();
        }

        trueValue = parameters.getProperty(PARAM_TRUE_VALUE, DEFAULT_TRUE_VALUE);
        falseValue = parameters.getProperty(PARAM_FALSE_VALUE, DEFAULT_FALSE_VALUE);
        unknownResult = populateUnknownResult(parameters.getProperty(PARAM_UNKNOWN_RESULT, DEFAULT_UNKNOWN_RESULT));
        ignoreCase = Boolean.valueOf(parameters.getProperty(PARAM_IGNORE_CASE, DEFAULT_IGNORE_CASE));

        populateSqlTypes();
    }

    Boolean populateUnknownResult(String unknownResultString) {
        if ("true".equalsIgnoreCase(unknownResultString)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(unknownResultString)) {
            return Boolean.FALSE;
        } else if ("null".equalsIgnoreCase(unknownResultString)) {
            return null;
        }

        throw new IllegalArgumentException(
            format("[%s] is illegal unknownResult value. Only 'true', 'false', 'null' are allowed.",
                unknownResultString));
    }

    private void populateSqlTypes() {
        TypeResolver tr = new TypeResolver();
        String stringClassName = String.class.getName();

        // type은 org.hibernate.type.StringType 으로 사실상 고정값임.
        type = (AbstractSingleColumnStandardBasicType) tr.basic(stringClassName);
        sqlTypes = new int[] { type.sqlType() };
    }

    @Override
    public int[] sqlTypes() {
        return Arrays.copyOf(sqlTypes, sqlTypes.length);
    }

    @Override
    public Class returnedClass() {
        return Boolean.class;
    }

    @Override
    public boolean equals(Object x, Object y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object x) {
        return Objects.hashCode(x);
    }

    @Override
    public Object deepCopy(Object value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) {
        return original;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws SQLException {
        String stringBooleanValue = (String) type.get(rs, names[0], session);

        if (stringBooleanValue == null) {
            log.trace("Found [{}] as column [{}] original value [{}]", null, names[0], stringBooleanValue);
            return null;
        }

        if (stringEqualsWithCaseCheck(trueValue, stringBooleanValue)) {
            log.trace("Found [{}] as column [{}] original value [{}]", true, names[0], stringBooleanValue);
            return true;
        }

        if (stringEqualsWithCaseCheck(falseValue, stringBooleanValue)) {
            log.trace("Found [{}] as column [{}] original value [{}]", false, names[0], stringBooleanValue);
            return false;
        }

        log.trace("Found [{}] as column [{}] original value [{}]", unknownResult, names[0], stringBooleanValue);
        return unknownResult;
    }

    private boolean stringEqualsWithCaseCheck(String value1, String value2) {
        if (ignoreCase) {
//            return StringUtils.equalsIgnoreCase(value1, value2);
        }
//        return StringUtils.equals(value1, value2);
        return false;
    }

    /**
     * 데이터베이스로 값을 저장하기 위해 Boolean을 문자열로 변환.
     */
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws SQLException {
        String columnValue = null;

        if (Boolean.TRUE.equals(value)) {
            columnValue = trueValue;
        } else if (Boolean.FALSE.equals(value)) {
            columnValue = falseValue;
        }
        log.trace("binding parameter [{}] as [{}] - [{}] original value [{}]", index, JdbcTypeNameMapper.getTypeName(sqlTypes[0]), columnValue, value);

        st.setObject(index, columnValue);
    }
}
