package kr.pe.kwonnam.hibernate4extrausertypes

import org.hibernate.HibernateException
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Types


class GenericEnumUserTypeSpec extends Specification {
    GenericEnumUserType genericEnumUserType = new GenericEnumUserType()

    Properties parameters = new Properties()

    def "setParameterValues - parameters=null"() {
        given:
        parameters = null

        when:
        genericEnumUserType.setParameterValues(parameters)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'parameters must not be null.'
    }

    def "setParameterValues - unknown enum class"() {
        given:
        parameters.setProperty(GenericEnumUserType.PARAM_ENUM_CLASS, "unknownpackage.UnknownEnumType")

        when:
        genericEnumUserType.setParameterValues(parameters)

        then:
        HibernateException hex = thrown()
        hex.message == 'Enum class(unknownpackage.UnknownEnumType) not found.'
    }

    def "setParameterValues - unknown identifierMethodName"() {
        given:
        parameters.setProperty(GenericEnumUserType.PARAM_ENUM_CLASS, "kr.pe.kwonnam.hibernate4extrausertypes.Sex")
        parameters.setProperty(GenericEnumUserType.PARAM_IDENTIFIER_METHOD, "unknownmethodname")

        when:
        genericEnumUserType.setParameterValues(parameters)

        then:
        HibernateException hex = thrown()
        hex.message == 'Failed to obtain identifier method(unknownmethodname).'
    }

    def "setParameterValues - unsupported identifier type"() {
        given:
        parameters.setProperty(GenericEnumUserType.PARAM_ENUM_CLASS, "kr.pe.kwonnam.hibernate4extrausertypes.Sex")
        parameters.setProperty(GenericEnumUserType.PARAM_IDENTIFIER_METHOD, "toPerson")

        when:
        genericEnumUserType.setParameterValues(parameters)

        then:
        HibernateException hex = thrown()
        hex.message == 'Unsupported identifier type kr.pe.kwonnam.hibernate4extrausertypes.Person'
    }

    def "setParameterValues - unknown valueOfmethodName"() {
        given:
        parameters.setProperty(GenericEnumUserType.PARAM_ENUM_CLASS, "kr.pe.kwonnam.hibernate4extrausertypes.Sex")
        parameters.setProperty(GenericEnumUserType.PARAM_IDENTIFIER_METHOD, "toInt")
        parameters.setProperty(GenericEnumUserType.PARAM_VALUE_OF_METHOD, "unknown")

        when:
        genericEnumUserType.setParameterValues(parameters)

        then:
        HibernateException hex = thrown()
        hex.message == 'Failed to obtain valueOf method(unknown) with identifierType(int).'
    }

    def "setParameterValues - identifierMethod-valueOfMethod type mismatch"() {
        given:
        parameters.setProperty(GenericEnumUserType.PARAM_ENUM_CLASS, "kr.pe.kwonnam.hibernate4extrausertypes.Sex")
        parameters.setProperty(GenericEnumUserType.PARAM_IDENTIFIER_METHOD, "toInt")
        parameters.setProperty(GenericEnumUserType.PARAM_VALUE_OF_METHOD, "fromShortCode")

        when:
        genericEnumUserType.setParameterValues(parameters)

        then:
        HibernateException hex = thrown()
        hex.message == 'Failed to obtain valueOf method(fromShortCode) with identifierType(int).'
    }

    @Unroll
    def "setParameterValues - identifierMethodName(#identifierMethodName) and valueOfMethodName(#valueOfMethodName)"(identifierMethodName, valueOfMethodName, expectedIdentifierType, expectedHibernateType, expectedSqlType) {
        given:
        parameters.setProperty(GenericEnumUserType.PARAM_ENUM_CLASS, "kr.pe.kwonnam.hibernate4extrausertypes.Sex")
        parameters.setProperty(GenericEnumUserType.PARAM_IDENTIFIER_METHOD, identifierMethodName)
        parameters.setProperty(GenericEnumUserType.PARAM_VALUE_OF_METHOD, valueOfMethodName)

        expect:
        genericEnumUserType.setParameterValues(parameters)
        genericEnumUserType.enumClass == Sex
        genericEnumUserType.identifierMethod.name == identifierMethodName
        genericEnumUserType.valueOfMethod.name == valueOfMethodName
        genericEnumUserType.identifierType == expectedIdentifierType
        genericEnumUserType.type.class == expectedHibernateType
        genericEnumUserType.sqlTypes().length == 1
        genericEnumUserType.sqlTypes()[0] == expectedSqlType

        where:
        identifierMethodName | valueOfMethodName | expectedIdentifierType | expectedHibernateType | expectedSqlType
        'toInt' | 'fromInt' | int.class | org.hibernate.type.IntegerType | Types.INTEGER
        'toShortCode' | 'fromShortCode' | char.class | org.hibernate.type.CharacterType | Types.CHAR
        'name' | 'valueOf' | String.class | org.hibernate.type.StringType | Types.VARCHAR
    }
}
