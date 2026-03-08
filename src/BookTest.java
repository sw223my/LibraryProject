import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    @Test
    public void testIsAvailableTrue() {
        Book b = new Book("123456", "Test Book", "Test Author", 2020, 2);

        boolean result = b.isAvailable();

        assertTrue(result);
    }

    @Test
    public void testIsAvailableFalse() {
        Book b = new Book("123456", "Test Book", "Test Author", 2020, 1);
        b.availableCopies = 0;

        boolean result = b.isAvailable();

        assertFalse(result);
    }
}