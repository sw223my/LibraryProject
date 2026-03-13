package Objects;

import java.util.Objects;

public class BookTitle {
    public String isbn;
    public String title;
    public String author;
    public int publishYear;

    public BookTitle(String isbn, String title, String author, int publishYear) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publishYear = publishYear;
    }

    @Override
    public String toString() {
        return "BookTitle{" +
                "isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", publishYear=" + publishYear +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BookTitle other)) return false;
        return publishYear == other.publishYear &&
                Objects.equals(isbn, other.isbn) &&
                Objects.equals(title, other.title) &&
                Objects.equals(author, other.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn, title, author, publishYear);
    }
}