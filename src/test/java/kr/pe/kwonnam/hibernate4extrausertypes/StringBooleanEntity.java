package kr.pe.kwonnam.hibernate4extrausertypes;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class StringBooleanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Type(
        type = "kr.pe.kwonnam.hibernate4extrausertypes.StringBooleanUserType",
        parameters = {
            @Parameter(name = StringBooleanUserType.PARAM_TRUE_VALUE, value = "Y"),
            @Parameter(name = StringBooleanUserType.PARAM_FALSE_VALUE, value = "N"),
            @Parameter(name = StringBooleanUserType.PARAM_IGNORE_CASE, value = "false")
        }
    )
    @Column(name = "accessible", columnDefinition = "char(1)")
    private Boolean accessible;

    @Type(
        type = "kr.pe.kwonnam.hibernate4extrausertypes.StringBooleanUserType",
        parameters = {
            @Parameter(name = StringBooleanUserType.PARAM_TRUE_VALUE, value = "right"),
            @Parameter(name = StringBooleanUserType.PARAM_FALSE_VALUE, value = "wrong"),
            @Parameter(name = StringBooleanUserType.PARAM_IGNORE_CASE, value = "true")
        }
    )
    @Column(name = "correct", length = 5)
    private Boolean correct;
}
