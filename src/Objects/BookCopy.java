package Objects;

import java.util.Objects;

public class BookCopy {
    public int copyId;
    public String isbn;
    public String status;

    public BookCopy(int copyId, String isbn, String status) {
        this.copyId = copyId;
        this.isbn = isbn;
        this.status = status;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "BookCopy{" +
                "copyId=" + copyId +
                ", isbn='" + isbn + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BookCopy other)) return false;
        return copyId == other.copyId &&
                Objects.equals(isbn, other.isbn) &&
                Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(copyId, isbn, status);
    }
}
