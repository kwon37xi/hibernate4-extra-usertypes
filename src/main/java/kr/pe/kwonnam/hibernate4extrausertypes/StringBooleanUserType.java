package kr.pe.kwonnam.hibernate4extrausertypes;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.jboss.logging.Logger;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import java.util.Properties;

import static java.lang.String.format;

public class StringBooleanUserType implements UserType, ParameterizedType {
    private static final CoreMessageLogger BINDER_LOGGER = Logger.getMessageLogger(CoreMessageLogger.class, BasicBinder.class.getName());
    private static final CoreMessageLogger EXTRACTOR_LOGGER = Logger.getMessageLogger(CoreMessageLogger.class, BasicExtractor.class.getName());

    public static final int SQL_TYPE = Types.VARCHAR;

    /** true 값으로 Column에 저장되는 문자열을 지정하는 파라미터 */
    public static final String PARAM_TRUE_VALUE = "trueValue";
    public static final String DEFAULT_TRUE_VALUE = "Y";

    /** false 값으로 Column에 저장되는 문자열을 지정하는 파라미터 */
    public static final String PARAM_FALSE_VALUE = "falseValue";
    public static final String DEFAULT_FALSE_VALUE = "N";

    /** 알 수 없는 값이 나왔을 때 리턴할 값을 지정하는 파라미터 */
    public static final String PARAM_UNKNOWN_RESULT = "unknownResult";
    public static final String DEFAULT_UNKNOWN_RESULT = "null";

    /** 대소문자를 무시할지 여부를 지정하는 파라미터 */
    public static final String PARAM_IGNORE_CASE = "ignoreCase";
    public static final String DEFAULT_IGNORE_CASE = "false";

    /** true 값을 나타내는 문자열 */
    private String trueValue = null;

    /** false 값을 나타내는 문자열 */
    private String falseValue = null;

    /**
     * DB상의 데이터가 알 수 없는 값일 때(빈 문자열 포함) unknownResult는
     * "true", "false", "null"을 문자열로 기입한다.
     * "null"이 기본값이며 이때는 알 수 없는 값이 들어오면 null을 리턴하고,
     * "true", "false"로 할 경우 해당 Boolean으로 변환한다.
     */
    private Boolean unknownResult = null;

    /**
     * 대소문자 무시할 지 여부를 {@code true}/{@code false}로 지정한다.
     * 기본값은 true로 대소문자를 다른 값으로써 비교한다.
     */
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

    @Override
    public int[] sqlTypes() {
        return new int[]{SQL_TYPE};
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

        String stringBooleanValue = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names[0], session, owner);

        if (stringBooleanValue == null) {
            return null;
        }

        if (stringEqualsWithCaseCheck(trueValue, stringBooleanValue)) {
            EXTRACTOR_LOGGER.tracev("Found [{0}] as column [{1}] original value [{2}]", true, names[0], stringBooleanValue);
            return true;
        }

        if (stringEqualsWithCaseCheck(falseValue, stringBooleanValue)) {
            EXTRACTOR_LOGGER.tracev("Found [{0}] as column [{1}] original value [{2}]", false, names[0], stringBooleanValue);
            return false;
        }

        EXTRACTOR_LOGGER.tracev("Found [{0}] as column [{1}] original value [{2}]", unknownResult, names[0], stringBooleanValue);
        return unknownResult;
    }

    private boolean stringEqualsWithCaseCheck(String value1, String value2) {
        if (ignoreCase) {
            return StringUtils.equalsIgnoreCase(value1, value2);
        }
        return StringUtils.equals(value1, value2);
    }

    /**
     * 데이터베이스로 값을 저장하기 위해 Boolean을 문자열로 변환.
     */
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws SQLException {
        if (value == null) {
            StandardBasicTypes.STRING.nullSafeSet(st, null, index, session);
            return;
        }

        String columnValue = Boolean.TRUE.equals(value) ? trueValue : falseValue;
        StandardBasicTypes.STRING.nullSafeSet(st, columnValue, index, session);
    }
}
