package kr.pe.kwonnam.hibernate4extrausertypes;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "people")
public class Person {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firstname", length = 50, nullable = false)
    private String firstName;


    @Column(name = "lastname", length = 50, nullable = false)
    private String lastName;

    @Column(name = "sex_by_int", nullable = true)
    @Type(
        type = "kr.pe.kwonnam.hibernate4extrausertypes.GenericEnumUserType",
        parameters = {
            @Parameter(
                name = GenericEnumUserType.PARAM_ENUM_CLASS,
                value = "kr.pe.kwonnam.hibernate4extrausertypes.Sex"),
            @Parameter(
                name = GenericEnumUserType.PARAM_IDENTIFIER_METHOD,
                value = "toInt"),
            @Parameter(
                name = GenericEnumUserType.PARAM_VALUE_OF_METHOD,
                value = "fromInt")
        }
    )
    private Sex sexByInt;

    @Column(name = "sex_by_shortcode", nullable = true, length = 1)
    @Type(
        type = "kr.pe.kwonnam.hibernate4extrausertypes.GenericEnumUserType",
        parameters = {
            @Parameter(
                name = GenericEnumUserType.PARAM_ENUM_CLASS,
                value = "kr.pe.kwonnam.hibernate4extrausertypes.Sex"),
            @Parameter(
                name = GenericEnumUserType.PARAM_IDENTIFIER_METHOD,
                value = "toShortCode"),
            @Parameter(
                name = GenericEnumUserType.PARAM_VALUE_OF_METHOD,
                value = "fromShortCode")
        }
    )
    private Sex sexByShortCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Sex getSexByInt() {
        return sexByInt;
    }

    public void setSexByInt(Sex sexByInt) {
        this.sexByInt = sexByInt;
    }

    public Sex getSexByShortCode() {
        return sexByShortCode;
    }

    public void setSexByShortCode(Sex sexByShortCode) {
        this.sexByShortCode = sexByShortCode;
    }
}
