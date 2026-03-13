package Objects;

import java.util.Objects;

public class Person {
    public String personalNumber;
    public String firstName;
    public String lastName;

    public Person(String personalNumber, String firstName, String lastName) {
        this.personalNumber = personalNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "Person{" +
                "personalNumber='" + personalNumber + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Person other)) return false;
        return Objects.equals(personalNumber, other.personalNumber) &&
                Objects.equals(firstName, other.firstName) &&
                Objects.equals(lastName, other.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personalNumber, firstName, lastName);
    }
}
