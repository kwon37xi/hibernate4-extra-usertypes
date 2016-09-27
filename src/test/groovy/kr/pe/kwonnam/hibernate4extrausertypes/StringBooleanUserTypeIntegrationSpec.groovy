package kr.pe.kwonnam.hibernate4extrausertypes

import org.hibernate.cfg.Configuration

class StringBooleanUserTypeIntegrationSpec extends AbstractUserTypeIntegrationSpec {
    @Override
    void addAnnotatedClass(Configuration configuration) {
        configuration.addAnnotatedClass(StringBooleanEntity)
    }

}
