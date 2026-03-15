package Tests;

import Database.ILibraryStore;
import Objects.BookCopy;
import Objects.BookTitle;
import Objects.Loan;
import Objects.MemberType;
import Objects.Membership;
import Objects.Person;
import Objects.Suspension;
import Processing.LibraryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LibraryServiceTest {

    @Mock
    private ILibraryStore store;

    @InjectMocks
    private LibraryService service;

    // ---------- Common fixtures ----------

    private static final String PERSONAL_NUMBER = "20050713-1234";
    private static final String ISBN            = "123456";
    private static final int    MEMBER_ID       = 3333;

    private Membership activeMembership;
    private BookTitle  bookTitle;
    private MemberType memberType;

    @BeforeEach
    void setUp() {
        activeMembership = new Membership(MEMBER_ID, PERSONAL_NUMBER, 1, null, "ACTIVE", 0, 0);
        bookTitle        = new BookTitle(ISBN, "The Hobbit", "J. R. R. Tolkien", 1937);
        memberType       = new MemberType(1, "Undergraduate", 3);
    }

    // ---------- addBookTitle ----------

    @Test
    void addBookTitle_shouldThrow_whenIsbnIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addBookTitle(" ", "Title", "Author", 2020, 1)
        );

        assertEquals("ISBN is required.", ex.getMessage());
        verify(store, never()).addBookTitle(any());
        verify(store, never()).addBookCopies(anyString(), anyInt());
    }

    @Test
    void addBookTitle_shouldThrow_whenIsbnNotSixDigits() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addBookTitle("12345", "Title", "Author", 2020, 1)
        );

        assertEquals("ISBN must be exactly 6 digits.", ex.getMessage());
        verify(store, never()).getBookTitle(anyString());
        verify(store, never()).addBookTitle(any());
        verify(store, never()).addBookCopies(anyString(), anyInt());
    }

    @Test
    void addBookTitle_shouldThrow_whenIsbnContainsLetters() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addBookTitle("12A456", "Title", "Author", 2020, 1)
        );

        assertEquals("ISBN must be exactly 6 digits.", ex.getMessage());
        verify(store, never()).getBookTitle(anyString());
        verify(store, never()).addBookTitle(any());
        verify(store, never()).addBookCopies(anyString(), anyInt());
    }

    @Test
    void addBookTitle_shouldThrow_whenTitleIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addBookTitle("123456", " ", "Author", 2020, 1)
        );

        assertEquals("Title is required.", ex.getMessage());
        verify(store, never()).addBookTitle(any());
        verify(store, never()).addBookCopies(anyString(), anyInt());
    }

    @Test
    void addBookTitle_shouldThrow_whenCopiesInvalid() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addBookTitle("123456", "Title", "Author", 2020, 0)
        );

        assertEquals("Copies must be at least 1.", ex.getMessage());
        verify(store, never()).addBookTitle(any());
        verify(store, never()).addBookCopies(anyString(), anyInt());
    }

    @Test
    void addBookTitle_shouldThrow_whenBookTitleAlreadyExists() {
        when(store.getBookTitle("123456"))
                .thenReturn(new BookTitle("123456", "Old", "Author", 2020));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addBookTitle("123456", "New", "Author", 2024, 2)
        );

        assertEquals("A book title with this ISBN already exists.", ex.getMessage());
        verify(store, never()).addBookTitle(any());
        verify(store, never()).addBookCopies(anyString(), anyInt());
    }

    @Test
    void addBookTitle_shouldAddBookTitleAndCopies_whenValid() {
        when(store.getBookTitle("123456")).thenReturn(null);

        service.addBookTitle("123456", "Clean Code", "Robert Martin", 2008, 3);

        ArgumentCaptor<BookTitle> captor = ArgumentCaptor.forClass(BookTitle.class);
        verify(store).addBookTitle(captor.capture());
        verify(store).addBookCopies("123456", 3);

        BookTitle added = captor.getValue();
        assertEquals("123456", added.isbn);
        assertEquals("Clean Code", added.title);
        assertEquals("Robert Martin", added.author);
        assertEquals(2008, added.publishYear);
    }

    // ---------- deleteBookTitle ----------

    @Test
    void deleteBookTitle_shouldThrow_whenIsbnNotSixDigits() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteBookTitle("12345")
        );

        assertEquals("ISBN must be exactly 6 digits.", ex.getMessage());
        verify(store, never()).getBookTitle(anyString());
        verify(store, never()).removeBookCopiesByIsbn(anyString());
        verify(store, never()).removeBookTitle(anyString());
    }

    @Test
    void deleteBookTitle_shouldReturnFalse_whenBookDoesNotExist() {
        when(store.getBookTitle("123456")).thenReturn(null);

        boolean result = service.deleteBookTitle("123456");

        assertFalse(result);
        verify(store, never()).removeBookCopiesByIsbn(anyString());
        verify(store, never()).removeBookTitle(anyString());
    }

    @Test
    void deleteBookTitle_shouldReturnFalse_whenActiveLoanExists() {
        BookTitle title = bookTitle;
        Loan activeLoan = mock(Loan.class);

        when(store.getBookTitle("123456")).thenReturn(title);
        when(store.getLoansForBook("123456")).thenReturn(List.of(activeLoan));
        when(activeLoan.isActive()).thenReturn(true);

        boolean result = service.deleteBookTitle("123456");

        assertFalse(result);
        verify(store, never()).removeBookCopiesByIsbn("123456");
        verify(store, never()).removeBookTitle("123456");
    }

    @Test
    void deleteBookTitle_shouldRemoveBookTitleAndCopies_whenNoActiveLoansExist() {
        BookTitle title = bookTitle;
        Loan loan1 = mock(Loan.class);
        Loan loan2 = mock(Loan.class);

        when(store.getBookTitle("123456")).thenReturn(title);
        when(store.getLoansForBook("123456")).thenReturn(List.of(loan1, loan2));
        when(loan1.isActive()).thenReturn(false);
        when(loan2.isActive()).thenReturn(false);

        boolean result = service.deleteBookTitle("123456");

        assertTrue(result);
        verify(store).removeBookCopiesByIsbn("123456");
        verify(store).removeBookTitle("123456");
    }

    // ---------- registerMember ----------

    @Test
    void registerMember_shouldThrow_whenFirstNameBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registerMember(" ", "Doe", "19900101-1234", 1)
        );

        assertEquals("First name is required.", ex.getMessage());
        verify(store, never()).getMemberType(anyInt());
        verify(store, never()).getMembershipByPersonalNumber(anyString());
        verify(store, never()).getPerson(anyString());
        verify(store, never()).generateMemberId();
        verify(store, never()).addPerson(any());
        verify(store, never()).addMembership(any());
    }

    @Test
    void registerMember_shouldThrow_whenLastNameBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registerMember("John", " ", "19900101-1234", 1)
        );

        assertEquals("Last name is required.", ex.getMessage());
        verify(store, never()).getMemberType(anyInt());
        verify(store, never()).getMembershipByPersonalNumber(anyString());
        verify(store, never()).getPerson(anyString());
        verify(store, never()).generateMemberId();
        verify(store, never()).addPerson(any());
        verify(store, never()).addMembership(any());
    }

    @Test
    void registerMember_shouldThrow_whenPersonalNumberBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registerMember("John", "Doe", " ", 1)
        );

        assertEquals("Personal number is required.", ex.getMessage());
        verify(store, never()).getMemberType(anyInt());
        verify(store, never()).getMembershipByPersonalNumber(anyString());
        verify(store, never()).getPerson(anyString());
        verify(store, never()).generateMemberId();
        verify(store, never()).addPerson(any());
        verify(store, never()).addMembership(any());
    }

    @Test
    void registerMember_shouldThrow_whenMemberTypeDoesNotExist() {
        when(store.getMemberType(99)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registerMember("John", "Doe", "19900101-1234", 99)
        );

        assertEquals("Invalid member type.", ex.getMessage());
        verify(store).getMemberType(99);
        verify(store, never()).getMembershipByPersonalNumber(anyString());
        verify(store, never()).getPerson(anyString());
        verify(store, never()).generateMemberId();
        verify(store, never()).addPerson(any());
        verify(store, never()).addMembership(any());
    }

    @Test
    void registerMember_shouldReturnExistingId_whenAlreadyRegistered() {
        when(store.getMemberType(1)).thenReturn(memberType);
        when(store.getPerson("19900101-1234"))
                .thenReturn(new Person("19900101-1234", "John", "Doe", false));

        Membership existing = new Membership(1001, "19900101-1234", 1, null, "ACTIVE", 0, 0);
        when(store.getMembershipByPersonalNumber("19900101-1234")).thenReturn(existing);

        String result = service.registerMember("John", "Doe", "19900101-1234", 1);

        assertEquals("1001", result);
        verify(store).getMemberType(1);
        verify(store).getPerson("19900101-1234");
        verify(store).getMembershipByPersonalNumber("19900101-1234");
        verify(store, never()).generateMemberId();
        verify(store, never()).addPerson(any());
        verify(store, never()).addMembership(any());
    }

    @Test
    void registerMember_shouldCreatePersonAndMembership_whenPersonAndMembershipDoNotExist() {
        when(store.getMemberType(2)).thenReturn(new MemberType(2, "Postgraduate", 5));
        when(store.getMembershipByPersonalNumber("19900101-1234")).thenReturn(null);
        when(store.getPerson("19900101-1234")).thenReturn(null);
        when(store.generateMemberId()).thenReturn(1000);

        String result = service.registerMember("John", "Doe", "19900101-1234", 2);

        assertEquals("1000", result);

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        ArgumentCaptor<Membership> membershipCaptor = ArgumentCaptor.forClass(Membership.class);

        verify(store).getMemberType(2);
        verify(store).getMembershipByPersonalNumber("19900101-1234");
        verify(store).getPerson("19900101-1234");
        verify(store).generateMemberId();
        verify(store).addPerson(personCaptor.capture());
        verify(store).addMembership(membershipCaptor.capture());

        Person person = personCaptor.getValue();
        assertEquals("19900101-1234", person.personalNumber);
        assertEquals("John", person.firstName);
        assertEquals("Doe", person.lastName);
        assertFalse(person.blocked);

        Membership membership = membershipCaptor.getValue();
        assertEquals(1000, membership.memberId);
        assertEquals("19900101-1234", membership.personalNumber);
        assertEquals(2, membership.memberTypeId);
        assertEquals("ACTIVE", membership.status);
        assertEquals(0, membership.lateReturnCount);
        assertEquals(0, membership.suspensionCount);
        assertNull(membership.suspendedUntil);
    }

    @Test
    void registerMember_shouldNotCreatePersonAgain_whenPersonExistsButMembershipDoesNot() {
        when(store.getMemberType(1)).thenReturn(memberType);
        when(store.getMembershipByPersonalNumber("19900101-1234")).thenReturn(null);
        when(store.getPerson("19900101-1234"))
                .thenReturn(new Person("19900101-1234", "John", "Doe", false));
        when(store.generateMemberId()).thenReturn(1002);

        String result = service.registerMember("John", "Doe", "19900101-1234", 1);

        assertEquals("1002", result);

        ArgumentCaptor<Membership> membershipCaptor = ArgumentCaptor.forClass(Membership.class);

        verify(store).getMemberType(1);
        verify(store).getMembershipByPersonalNumber("19900101-1234");
        verify(store).getPerson("19900101-1234");
        verify(store).generateMemberId();
        verify(store, never()).addPerson(any());
        verify(store).addMembership(membershipCaptor.capture());

        Membership membership = membershipCaptor.getValue();
        assertEquals(1002, membership.memberId);
        assertEquals("19900101-1234", membership.personalNumber);
        assertEquals(1, membership.memberTypeId);
        assertEquals("ACTIVE", membership.status);
        assertEquals(0, membership.lateReturnCount);
        assertEquals(0, membership.suspensionCount);
        assertNull(membership.suspendedUntil);
    }

    @Test
    void registerMember_shouldThrow_whenPersonIsBlocked() {
        when(store.getMemberType(1)).thenReturn(memberType);
        when(store.getPerson("19900101-1234"))
                .thenReturn(new Person("19900101-1234", "John", "Doe", true));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registerMember("John", "Doe", "19900101-1234", 1)
        );

        assertEquals("Registration not allowed due to previous violations.", ex.getMessage());
        verify(store).getMemberType(1);
        verify(store).getPerson("19900101-1234");
        verify(store, never()).getMembershipByPersonalNumber(anyString());
        verify(store, never()).generateMemberId();
        verify(store, never()).addPerson(any());
        verify(store, never()).addMembership(any());
    }

    @Test
    void registerMember_shouldThrow_whenGeneratedMemberIdIsNotFourDigits() {
        when(store.getMemberType(1)).thenReturn(memberType);
        when(store.getMembershipByPersonalNumber("19900101-1234")).thenReturn(null);
        when(store.getPerson("19900101-1234")).thenReturn(null);
        when(store.generateMemberId()).thenReturn(999);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerMember("John", "Doe", "19900101-1234", 1)
        );

        assertEquals("Generated member ID must be a 4-digit number.", ex.getMessage());
        verify(store).generateMemberId();
        verify(store, never()).addMembership(any());
    }

    // ---------- suspendMember ----------

    @Test
    void suspendMember_shouldThrow_whenMemberIdIsNotFourDigits() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.suspendMember(999, 10)
        );

        assertEquals("Member ID must be a 4-digit number.", ex.getMessage());
        verify(store, never()).getMembership(anyInt());
        verify(store, never()).updateMembership(any());
        verify(store, never()).addSuspension(any());
    }

    @Test
    void suspendMember_shouldReturnFalse_whenMemberDoesNotExist() {
        when(store.getMembership(1000)).thenReturn(null);

        boolean result = service.suspendMember(1000, 10);

        assertFalse(result);
        verify(store, never()).updateMembership(any());
        verify(store, never()).addSuspension(any());
    }

    @Test
    void suspendMember_shouldReturnFalse_whenDaysInvalid() {
        Membership membership = activeMembership;
        when(store.getMembership(1000)).thenReturn(membership);

        boolean result = service.suspendMember(1000, 0);

        assertFalse(result);
        verify(store, never()).updateMembership(any());
        verify(store, never()).addSuspension(any());
    }

    @Test
    void suspendMember_shouldUpdateMembershipAndAddSuspension_whenValid() {
        Membership membership = activeMembership;
        when(store.getMembership(1000)).thenReturn(membership);

        boolean result = service.suspendMember(1000, 10);

        assertTrue(result);
        assertNotNull(membership.suspendedUntil);
        assertEquals("SUSPENDED", membership.status);
        assertEquals(1, membership.suspensionCount);

        verify(store).updateMembership(membership);

        ArgumentCaptor<Suspension> captor = ArgumentCaptor.forClass(Suspension.class);
        verify(store).addSuspension(captor.capture());

        Suspension suspension = captor.getValue();
        assertEquals(1000, suspension.memberId);
        assertNotNull(suspension.startDate);
        assertNotNull(suspension.endDate);
    }

    // ---------- deleteMember ----------

    @Test
    void deleteMember_shouldThrow_whenMemberIdIsNotFourDigits() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteMember(10000)
        );

        assertEquals("Member ID must be a 4-digit number.", ex.getMessage());
        verify(store, never()).getMembership(anyInt());
    }

    @Test
    void deleteMember_shouldReturnFalse_whenMemberDoesNotExist() {
        when(store.getMembership(1000)).thenReturn(null);

        boolean result = service.deleteMember(1000);

        assertFalse(result);
        verify(store, never()).removeMembership(anyInt());
    }

    @Test
    void deleteMember_shouldReturnFalse_whenActiveLoanExists() {
        Membership membership = activeMembership;
        Loan activeLoan = mock(Loan.class);

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getLoansForMember(1000)).thenReturn(List.of(activeLoan));
        when(activeLoan.isActive()).thenReturn(true);

        boolean result = service.deleteMember(1000);

        assertFalse(result);
        verify(store, never()).removeMembership(1000);
    }

    @Test
    void deleteMember_shouldBlockPersonAndRemoveMembership_whenNoActiveLoans() {
        Membership membership = activeMembership;
        Loan loan = mock(Loan.class);

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getLoansForMember(1000)).thenReturn(List.of(loan));
        when(loan.isActive()).thenReturn(false);

        boolean result = service.deleteMember(1000);

        assertTrue(result);
        verify(store).blockPerson("19900101-1234");
        verify(store).removeMembership(1000);
    }

    // ---------- lendBook ----------

    @Test
    void lendBook_shouldThrow_whenMemberIdIsNotFourDigits() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.lendBook(999, "123456")
        );

        assertEquals("Member ID must be a 4-digit number.", ex.getMessage());
        verify(store, never()).getMembership(anyInt());
    }

    @Test
    void lendBook_shouldThrow_whenIsbnIsNotSixDigits() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.lendBook(1000, "12345")
        );

        assertEquals("ISBN must be exactly 6 digits.", ex.getMessage());
        verify(store, never()).getMembership(anyInt());
    }

    @Test
    void lendBook_shouldReturnFalse_whenMemberNotFound() {
        when(store.getMembership(1000)).thenReturn(null);

        boolean result = service.lendBook(1000, "123456");

        assertFalse(result);
        verify(store, never()).addLoan(any());
    }

    @Test
    void lendBook_shouldReturnFalse_whenBookTitleNotFound() {
        Membership membership = activeMembership;

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getBookTitle("123456")).thenReturn(null);

        boolean result = service.lendBook(1000, "123456");

        assertFalse(result);
        verify(store, never()).addLoan(any());
    }

    @Test
    void lendBook_shouldReturnFalse_whenMemberIsSuspended() {
        Date tomorrow = new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000);
        Membership membership = new Membership(1000, "19900101-1234", 1, tomorrow, "SUSPENDED", 0, 1);
        BookTitle title = bookTitle;

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getBookTitle("123456")).thenReturn(title);

        boolean result = service.lendBook(1000, "123456");

        assertFalse(result);
        verify(store, never()).addLoan(any());
    }

    @Test
    void lendBook_shouldReturnFalse_whenLoanLimitReached() {
        Membership membership = activeMembership;
        BookTitle title = bookTitle;
        MemberType type = new MemberType(1, "Student", 1);
        Loan activeLoan = mock(Loan.class);

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getBookTitle("123456")).thenReturn(title);
        when(store.getMemberType(1)).thenReturn(type);
        when(store.getLoansForMember(1000)).thenReturn(List.of(activeLoan));
        when(activeLoan.isActive()).thenReturn(true);

        boolean result = service.lendBook(1000, "123456");

        assertFalse(result);
        verify(store, never()).addLoan(any());
    }

    @Test
    void lendBook_shouldReturnFalse_whenActiveLoanAlreadyExists() {
        Membership membership = activeMembership;
        BookTitle title = bookTitle;
        MemberType type = new MemberType(1, "Student", 5);
        Loan existingLoan = mock(Loan.class);

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getBookTitle("123456")).thenReturn(title);
        when(store.getMemberType(1)).thenReturn(type);
        when(store.getLoansForMember(1000)).thenReturn(Collections.emptyList());
        when(store.getActiveLoan(1000, "123456")).thenReturn(existingLoan);

        boolean result = service.lendBook(1000, "123456");

        assertFalse(result);
        verify(store, never()).addLoan(any());
    }

    @Test
    void lendBook_shouldReturnFalse_whenNoAvailableCopy() {
        Membership membership = activeMembership;
        BookTitle title = bookTitle;
        MemberType type = new MemberType(1, "Student", 5);

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getBookTitle("123456")).thenReturn(title);
        when(store.getMemberType(1)).thenReturn(type);
        when(store.getLoansForMember(1000)).thenReturn(Collections.emptyList());
        when(store.getActiveLoan(1000, "123456")).thenReturn(null);
        when(store.getAvailableBookCopy("123456")).thenReturn(null);

        boolean result = service.lendBook(1000, "123456");

        assertFalse(result);
        verify(store, never()).addLoan(any());
    }

    @Test
    void lendBook_shouldCreateLoanAndUpdateCopy_whenValid() {
        Membership membership = activeMembership;
        BookTitle title = bookTitle;
        MemberType type = new MemberType(1, "Student", 5);
        BookCopy copy = new BookCopy(7, "123456", "AVAILABLE");

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getBookTitle("123456")).thenReturn(title);
        when(store.getMemberType(1)).thenReturn(type);
        when(store.getLoansForMember(1000)).thenReturn(Collections.emptyList());
        when(store.getActiveLoan(1000, "123456")).thenReturn(null);
        when(store.getAvailableBookCopy("123456")).thenReturn(copy);

        boolean result = service.lendBook(1000, "123456");

        assertTrue(result);
        assertEquals("LOANED", copy.status);
        verify(store).updateBookCopy(copy);

        ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
        verify(store).addLoan(captor.capture());

        Loan loan = captor.getValue();
        assertEquals(0, loan.loanId);
        assertEquals(1000, loan.memberId);
        assertEquals(7, loan.copyId);
        assertNotNull(loan.loanDate);
        assertNotNull(loan.dueDate);
        assertNull(loan.returnDate);
    }

    @Test
    void lendBook_shouldReactivateMember_whenSuspensionHasExpired() {
        Date yesterday = new Date(System.currentTimeMillis() - 24L * 60 * 60 * 1000);
        Membership membership = new Membership(1000, "19900101-1234", 1, yesterday, "SUSPENDED", 0, 1);
        BookTitle title = bookTitle;
        MemberType type = new MemberType(1, "Student", 5);
        BookCopy copy = new BookCopy(7, "123456", "AVAILABLE");

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getBookTitle("123456")).thenReturn(title);
        when(store.getMemberType(1)).thenReturn(type);
        when(store.getLoansForMember(1000)).thenReturn(Collections.emptyList());
        when(store.getActiveLoan(1000, "123456")).thenReturn(null);
        when(store.getAvailableBookCopy("123456")).thenReturn(copy);

        boolean result = service.lendBook(1000, "123456");

        assertTrue(result);
        assertEquals("ACTIVE", membership.status);
        assertNull(membership.suspendedUntil);
        verify(store).updateMembership(membership);
    }

    // ---------- returnBook ----------

    @Test
    void returnBook_shouldThrow_whenMemberIdIsNotFourDigits() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.returnBook(999, "123456")
        );

        assertEquals("Member ID must be a 4-digit number.", ex.getMessage());
        verify(store, never()).getMembership(anyInt());
    }

    @Test
    void returnBook_shouldThrow_whenIsbnIsNotSixDigits() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.returnBook(1000, "12345")
        );

        assertEquals("ISBN must be exactly 6 digits.", ex.getMessage());
        verify(store, never()).getMembership(anyInt());
    }

    @Test
    void returnBook_shouldFail_whenMemberNotFound() {
        when(store.getMembership(1000)).thenReturn(null);

        LibraryService.ReturnResult result = service.returnBook(1000, "123456");

        assertFalse(result.success);
        assertEquals("Member not found.", result.message);
    }

    @Test
    void returnBook_shouldFail_whenBookNotFound() {
        Membership membership = activeMembership;

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getBookTitle("123456")).thenReturn(null);

        LibraryService.ReturnResult result = service.returnBook(1000, "123456");

        assertFalse(result.success);
        assertEquals("Book not found.", result.message);
    }

    @Test
    void returnBook_shouldFail_whenNoActiveLoanFound() {
        Membership membership = activeMembership;
        BookTitle title = bookTitle;

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getBookTitle("123456")).thenReturn(title);
        when(store.getActiveLoan(1000, "123456")).thenReturn(null);

        LibraryService.ReturnResult result = service.returnBook(1000, "123456");

        assertFalse(result.success);
        assertEquals("No active loan found.", result.message);
    }

    @Test
    void returnBook_shouldCompleteNormalReturn_whenNotLate() {
        Membership membership = activeMembership;
        Date today = new Date();
        Date futureDue = new Date(today.getTime() + 24L * 60 * 60 * 1000);

        BookTitle title = bookTitle;
        Loan loan = new Loan(1, 1000, 7, today, futureDue, null);
        BookCopy copy = new BookCopy(7, "123456", "LOANED");

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getBookTitle("123456")).thenReturn(title);
        when(store.getActiveLoan(1000, "123456")).thenReturn(loan);
        when(store.getBookCopies("123456")).thenReturn(List.of(copy));

        LibraryService.ReturnResult result = service.returnBook(1000, "123456");

        assertTrue(result.success);
        assertFalse(result.late);
        assertEquals("Return completed.", result.message);
        assertNotNull(loan.returnDate);
        assertEquals("AVAILABLE", copy.status);

        verify(store).updateLoan(loan);
        verify(store).updateBookCopy(copy);
        verify(store, never()).addSuspension(any());
    }

    @Test
    void returnBook_shouldSuspendMember_afterThirdLateReturn() {
        Membership membership = new Membership(1000, "19900101-1234", 1, null, "ACTIVE", 2, 0);
        Date oldDate = new Date(System.currentTimeMillis() - 10L * 24 * 60 * 60 * 1000);
        Date pastDue = new Date(System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000);

        BookTitle title = bookTitle;
        Loan loan = new Loan(1, 1000, 7, oldDate, pastDue, null);
        BookCopy copy = new BookCopy(7, "123456", "LOANED");

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getBookTitle("123456")).thenReturn(title);
        when(store.getActiveLoan(1000, "123456")).thenReturn(loan);
        when(store.getBookCopies("123456")).thenReturn(List.of(copy));

        LibraryService.ReturnResult result = service.returnBook(1000, "123456");

        assertTrue(result.success);
        assertTrue(result.late);
        assertNotNull(result.suspendedUntil);
        assertFalse(result.memberDeleted);
        assertEquals(3, membership.lateReturnCount);
        assertEquals(1, membership.suspensionCount);
        assertEquals("SUSPENDED", membership.status);

        verify(store).addSuspension(any(Suspension.class));
        verify(store).updateMembership(membership);
    }

    @Test
    void returnBook_shouldBlockPersonAndRemoveMembership_whenSuspensionCountExceedsTwo() {
        Membership membership = new Membership(1000, "19900101-1234", 1, null, "ACTIVE", 2, 2);
        Date oldDate = new Date(System.currentTimeMillis() - 10L * 24 * 60 * 60 * 1000);
        Date pastDue = new Date(System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000);

        BookTitle title = bookTitle;
        Loan loan = new Loan(1, 1000, 7, oldDate, pastDue, null);
        BookCopy copy = new BookCopy(7, "123456", "LOANED");

        when(store.getMembership(1000)).thenReturn(membership);
        when(store.getBookTitle("123456")).thenReturn(title);
        when(store.getActiveLoan(1000, "123456")).thenReturn(loan);
        when(store.getBookCopies("123456")).thenReturn(List.of(copy));

        LibraryService.ReturnResult result = service.returnBook(1000, "123456");

        assertTrue(result.success);
        assertTrue(result.late);
        assertTrue(result.memberDeleted);
        assertNotNull(result.suspendedUntil);

        assertEquals(3, membership.lateReturnCount);
        assertEquals(3, membership.suspensionCount);
        assertEquals("SUSPENDED", membership.status);
        assertEquals("AVAILABLE", copy.status);
        assertNotNull(loan.returnDate);

        verify(store).updateLoan(loan);
        verify(store).updateBookCopy(copy);
        verify(store).addSuspension(any(Suspension.class));
        verify(store).blockPerson("19900101-1234");
        verify(store).removeMembership(1000);
        verify(store, never()).updateMembership(any());
    }

    // ---------- getters ----------

    @Test
    void getBookTitle_shouldThrow_whenIsbnIsNotSixDigits() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getBookTitle("12345")
        );

        assertEquals("ISBN must be exactly 6 digits.", ex.getMessage());
        verify(store, never()).getBookTitle(anyString());
    }

    @Test
    void getBookTitle_shouldDelegateToStore() {
        BookTitle title = bookTitle;
        when(store.getBookTitle("123456")).thenReturn(title);

        BookTitle result = service.getBookTitle("123456");

        assertEquals(title, result);
        verify(store).getBookTitle("123456");
    }

    @Test
    void getMembership_shouldThrow_whenMemberIdIsNotFourDigits() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getMembership(999)
        );

        assertEquals("Member ID must be a 4-digit number.", ex.getMessage());
        verify(store, never()).getMembership(anyInt());
    }

    @Test
    void getMembership_shouldReturnNull_whenMemberDoesNotExist() {
        when(store.getMembership(1000)).thenReturn(null);

        Membership result = service.getMembership(1000);

        assertNull(result);
        verify(store).getMembership(1000);
        verify(store, never()).updateMembership(any());
    }

    @Test
    void getMembership_shouldDelegateToStore() {
        Membership membership = activeMembership;
        when(store.getMembership(1000)).thenReturn(membership);

        Membership result = service.getMembership(1000);

        assertEquals(membership, result);
        verify(store).getMembership(1000);
    }

    @Test
    void getLoansForMember_shouldThrow_whenMemberIdIsNotFourDigits() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getLoansForMember(999)
        );

        assertEquals("Member ID must be a 4-digit number.", ex.getMessage());
        verify(store, never()).getLoansForMember(anyInt());
    }

    @Test
    void getLoansForMember_shouldDelegateToStore() {
        List<Loan> loans = List.of(new Loan(1, 1000, 7, new Date(), new Date(), null));
        when(store.getLoansForMember(1000)).thenReturn(loans);

        List<Loan> result = service.getLoansForMember(1000);

        assertEquals(loans, result);
        verify(store).getLoansForMember(1000);
    }

    @Test
    void getMembership_shouldReactivateMember_whenSuspensionHasExpired() {
        Date yesterday = new Date(System.currentTimeMillis() - 24L * 60 * 60 * 1000);
        Membership membership = new Membership(1000, "19900101-1234", 1, yesterday, "SUSPENDED", 0, 1);

        when(store.getMembership(1000)).thenReturn(membership);

        Membership result = service.getMembership(1000);

        assertEquals("ACTIVE", result.status);
        assertNull(result.suspendedUntil);
        verify(store).updateMembership(membership);
    }

    @Test
    void getMembership_shouldKeepSuspendedStatus_whenSuspensionIsStillActive() {
        Date tomorrow = new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000);
        Membership membership = new Membership(1000, "19900101-1234", 1, tomorrow, "ACTIVE", 0, 1);

        when(store.getMembership(1000)).thenReturn(membership);

        Membership result = service.getMembership(1000);

        assertEquals("SUSPENDED", result.status);
        assertEquals(tomorrow, result.suspendedUntil);
        verify(store).updateMembership(membership);
    }
}