import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

public class LoanTest {

    @Test
    public void testIsActiveTrue() {
        Loan loan = new Loan();

        boolean result = loan.isActive();

        assertTrue(result);
    }

    @Test
    public void testIsActiveFalse() {
        Loan loan = new Loan();
        loan.returnDate = new Date();

        boolean result = loan.isActive();

        assertFalse(result);
    }

    @Test
    public void testIsLateTrue() {
        Loan loan = new Loan();

        Date today = new Date();
        Date yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);

        loan.dueDate = yesterday;
        loan.returnDate = null;

        boolean result = loan.isLate(today);

        assertTrue(result);
    }

    @Test
    public void testIsLateFalse() {
        Loan loan = new Loan();

        Date today = new Date();
        Date tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000);

        loan.dueDate = tomorrow;
        loan.returnDate = null;

        boolean result = loan.isLate(today);

        assertFalse(result);
    }
}