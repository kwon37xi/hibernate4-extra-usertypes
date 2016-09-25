# Hibernate4 Extra UserTypes
Hibernate 4 extra user types for enum(when not using name or ordinal), boolean for string columns, Date for string columns.

## Requirements
* Hibernate 4 or later
* Java 7

## Setup
Just copy the source files into your project and use like the following examples.

## Usage

### [GenericEnumUserType](https://github.com/kwon37xi/hibernate4-extra-usertypes/blob/master/src/main/java/kr/pe/kwonnam/hibernate4extrausertypes/GenericEnumUserType.java)
`GenericEnumUserType` is for enum but not using enum's name or ordinal.
See the test entity mapping [Person.java](https://github.com/kwon37xi/hibernate4-extra-usertypes/blob/master/src/test/java/kr/pe/kwonnam/hibernate4extrausertypes/Person.java).

```java
// When you have enum like the following,
// You can save enum using shortCode or intValue.

public enum Sex {

    MALE(10001, 'M'),
    FEMALE(20002, 'F');

    private int intValue;
    private char shortCode;

    Sex(int intValue, char shortCode) {
        this.intValue = intValue;
        this.shortCode = shortCode;
    }

    /** for unsupported identifier type test */
    public Person toPerson() {
        return new Person();
    }

    /**
     * int identifier method
     */
    public int toInt() {
        return intValue;
    }

    /**
     * int valueOf method
     */
    public static Sex fromInt(int value) {
        for (Sex sex : Sex.values()) {
            if (sex.intValue == value) {
                return sex;
            }
        }
        return null;
    }

    /**
     * String shortCode identifier method
     */
    public char toShortCode() {
        return shortCode;
    }

    /**
     * String shortCode valueOf method
     */
    public static Sex fromShortCode(char shortCode) {
        for (Sex sex : Sex.values()) {
            if (sex.shortCode == shortCode) {
                return sex;
            }
        }
        return null;
    }
}

// Entity field mapping like the following
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
private Sex sexByInt; // integer column. save 10001 or 20002.

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
private Sex sexByShortCode; // char column. save 'M' or 'F'.
```
