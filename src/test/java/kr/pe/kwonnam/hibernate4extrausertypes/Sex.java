package kr.pe.kwonnam.hibernate4extrausertypes;

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
