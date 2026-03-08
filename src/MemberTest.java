import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

public class MemberTest {

    @Test
    public void testMaxLoansUndergrad() {

        Member m = new Member("4128", "Lisa", "Student", 1);

        int max = m.maxLoans();

        assertEquals(3, max);
    }
    @Test
    public void testMaxLoansTeacher() {

        Member m = new Member("9001", "Eva", "Teacher", 4);

        int max = m.maxLoans();

        assertEquals(10, max);
    }
    @Test
    public void testCanBorrowTrue() {
        Member m = new Member("4128", "Lisa", "Student", 1);
        m.borrowedCount = 1;

        boolean result = m.canBorrow();

        assertTrue(result);
    }

    @Test
    public void testCanBorrowFalse() {
        Member m = new Member("4128", "Lisa", "Student", 1);
        m.borrowedCount = 3;

        boolean result = m.canBorrow();

        assertFalse(result);
    }

    @Test
    public void testIsSuspendedFalse() {
        Member m = new Member("4128", "Lisa", "Student", 1);

        boolean result = m.isSuspended(new Date());

        assertFalse(result);
    }

    @Test
    public void testIsSuspendedTrue() {
        Member m = new Member("4128", "Lisa", "Student", 1);

        Date today = new Date(); //Skapar dagens datum
        Date futureDate = new Date(today.getTime() + 24 * 60 * 60 * 1000); //Skapar ett datum i framtiden (today + 1 dag, alltså imorgon)
        m.suspendedUntil = futureDate; //medlemmen är avstängd tills imorgon

        boolean result = m.isSuspended(today);

        assertTrue(result);
    }

        @Test
        public void testIsSuspendedFalseWhenPastDate() {
            Member m = new Member("4128", "Lisa", "Student", 1);

            Date today = new Date();
            Date pastDate = new Date(today.getTime() - 24 * 60 * 60 * 1000); //igår
            m.suspendedUntil = pastDate; //Suspensionen har gått ut → medlemmen är inte suspended

            boolean result = m.isSuspended(today);

            assertFalse(result);
        }
    }

