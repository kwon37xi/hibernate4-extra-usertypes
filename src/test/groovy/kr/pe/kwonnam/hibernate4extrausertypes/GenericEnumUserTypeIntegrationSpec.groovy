package kr.pe.kwonnam.hibernate4extrausertypes

import groovy.sql.Sql
import org.hibernate.cfg.Configuration
import org.hibernate.jdbc.Work

import java.sql.Connection

class GenericEnumUserTypeIntegrationSpec extends AbstractUserTypeIntegrationSpec {
    @Override
    void addAnnotatedClass(Configuration configuration) {
        configuration.addAnnotatedClass(Person)
    }

    def "save and get"() {
        given:
        Person person = new Person(firstName: "GenericEnumUserType", lastName: "Hibernate", sexByInt: Sex.MALE, sexByShortCode: Sex.FEMALE)

        when:
        Long id = session.save(person)
        session.evict(person)

        Person readFromDb = session.get(Person, 1L)

        then:
        id == 1L
        session.doWork({ Connection con ->
            Sql sql = new Sql(con)
            def row = sql.firstRow("select sex_by_int, sex_by_shortcode from people where id = 1")
            assert row.sex_by_int == 10001
            assert row.sex_by_shortcode == 'F'
        } as Work)

        readFromDb.sexByInt == Sex.MALE
        readFromDb.sexByShortCode == Sex.FEMALE
        readFromDb.firstName == 'GenericEnumUserType'
        readFromDb.lastName == 'Hibernate'
    }

    def "save and get null values"() {
        given:
        Person person = new Person(firstName: "GenericEnumUserType", lastName: "Hibernate", sexByInt: null, sexByShortCode: null)

        when:
        Long id = session.save(person)
        session.evict(person)

        Person readFromDb = session.get(Person, 1L)

        then:
        id == 1L
        session.doWork({ Connection con ->
            Sql sql = new Sql(con)
            def row = sql.firstRow("select sex_by_int, sex_by_shortcode from people where id = 1")
            assert row.sex_by_int == null
            assert row.sex_by_shortcode == null
        } as Work)

        readFromDb.sexByInt == null
        readFromDb.sexByShortCode == null
        readFromDb.firstName == 'GenericEnumUserType'
        readFromDb.lastName == 'Hibernate'
    }
}
