
public class Book {
    public String author;
    public String ISBN;
    public String title;
    public int year;

    // New fields: handle multiple copies of same title
    public int totalCopies;
    public int availableCopies;

    public Book() {}

    public Book(String ISBN, String title, String author, int year, int copies) {
        this.ISBN = ISBN;
        this.title = title;
        this.author = author;
        this.year = year;
        this.totalCopies = copies;
        this.availableCopies = copies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }
}
