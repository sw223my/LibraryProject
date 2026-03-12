package Tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import Processing.*;

public class MemberTest {

    @Test
    public void testMaxLoansUndergrad() {

        // Skapar en undergraduate student (level 1)
        Member m = new Member("4128", "Lisa", "Student", "19990101-1234", 1);

        int max = m.maxLoans();

        // En undergraduate får max låna 3 böcker
        assertEquals(3, max);
    }

    @Test
    public void testMaxLoansTeacher() {

        // Skapar en teacher (level 4)
        Member m = new Member("9001", "Eva", "Teacher", "19700101-0000", 4);

        int max = m.maxLoans();

        // En teacher får låna upp till 10 böcker
        assertEquals(10, max);
    }

    @Test
    public void testCanBorrowTrue() {

        // Skapar en medlem som inte nått maxgränsen
        Member m = new Member("4128", "Lisa", "Student", "19990101-1234", 1);
        m.borrowedCount = 1;

        // Skapar dagens datum
        Date today = new Date();

        boolean result = m.canBorrow(today);

        // Medlemmen ska kunna låna eftersom den inte är suspended och inte har nått max antal lån
        assertTrue(result);
    }

    @Test
    public void testCanBorrowFalse() {

        // Skapar en medlem som redan har max lån
        Member m = new Member("4128", "Lisa", "Student", "19990101-1234", 1);
        m.borrowedCount = 3;

        Date today = new Date();

        boolean result = m.canBorrow(today);

        // Medlemmen ska inte kunna låna fler böcker
        assertFalse(result);
    }

    @Test
    public void testIsSuspendedFalse() {

        // Medlemmen har ingen suspension
        Member m = new Member("4128", "Lisa", "Student", "19990101-1234", 1);

        boolean result = m.isSuspended(new Date());

        // Om suspendedUntil är null ska resultatet vara false
        assertFalse(result);
    }

    @Test
    public void testIsSuspendedTrue() {

        Member m = new Member("4128", "Lisa", "Student", "19990101-1234", 1);

        Date today = new Date(); // Skapar dagens datum

        Date futureDate = new Date(today.getTime() + 24 * 60 * 60 * 1000); // Skapar ett datum i framtiden (today + 1 dag, alltså imorgon)

        m.suspendedUntil = futureDate; // Medlemmen är avstängd tills imorgon

        boolean result = m.isSuspended(today);

        // Medlemmen ska fortfarande vara suspended
        assertTrue(result);
    }

    @Test
    public void testIsSuspendedFalseWhenPastDate() {

        Member m = new Member("4128", "Lisa", "Student", "19990101-1234", 1);

        Date today = new Date();

        Date pastDate = new Date(today.getTime() - 24 * 60 * 60 * 1000); // Skapar ett datum igår

        m.suspendedUntil = pastDate; // Suspensionen har gått ut → medlemmen är inte suspended

        boolean result = m.isSuspended(today);

        assertFalse(result);
    }
}

