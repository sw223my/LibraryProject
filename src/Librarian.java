public class Librarian {
    public int librarianId;
    public String firstName;
    public String lastName;
    public int level = 5; //librarian level = 5

    public Librarian() {}

    public Librarian(int librarianId, String firstName, String lastName) {
        this.librarianId = librarianId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}