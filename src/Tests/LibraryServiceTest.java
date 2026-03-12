package Tests;

import Processing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LibraryServiceTest {

    private ILibraryStore store;
    private LibraryService service;

    @BeforeEach
    void setUp() {
        store = mock(ILibraryStore.class);
        service = new LibraryService(store);
    }

    @Test
    void addBookTitle_shouldThrow_whenIsbnIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addBookTitle(" ", "Title", "Author", 2020, 1)
        );

        assertEquals("ISBN is required.", ex.getMessage());
        verify(store, never()).addBook(any());
    }

    @Test
    void addBookTitle_shouldThrow_whenTitleIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addBookTitle("123", " ", "Author", 2020, 1)
        );

        assertEquals("Title is required.", ex.getMessage());
        verify(store, never()).addBook(any());
    }

    @Test
    void addBookTitle_shouldThrow_whenCopiesIsInvalid() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addBookTitle("123", "Title", "Author", 2020, 0)
        );

        assertEquals("Copies must be at least 1.", ex.getMessage());
        verify(store, never()).addBook(any());
    }

    @Test
    void addBookTitle_shouldThrow_whenBookAlreadyExists() {
        when(store.getBook("123")).thenReturn(new Book("123", "Old", "Author", 2020, 1));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addBookTitle("123", "New", "Author", 2024, 2)
        );

        assertEquals("A book with this ISBN already exists.", ex.getMessage());
        verify(store, never()).addBook(any());
    }

    @Test
    void addBookTitle_shouldAddBook_whenValid() {
        when(store.getBook("123")).thenReturn(null);

        service.addBookTitle("123", "Clean Code", "Robert Martin", 2008, 3);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(store).addBook(captor.capture());

        Book added = captor.getValue();
        assertEquals("123", added.ISBN);
        assertEquals("Clean Code", added.title);
        assertEquals("Robert Martin", added.author);
        assertEquals(2008, added.year);
        assertEquals(3, added.totalCopies);
        assertEquals(3, added.availableCopies);
    }

    @Test
    void deleteBook_shouldReturnFalse_whenBookDoesNotExist() {
        when(store.getBook("123")).thenReturn(null);

        boolean result = service.deleteBook("123");

        assertFalse(result);
        verify(store, never()).removeBook(anyString());
    }

    @Test
    void deleteBook_shouldReturnFalse_whenActiveLoanExists() {
        Book book = new Book("123", "Java", "Author", 2020, 2);
        Loan activeLoan = mock(Loan.class);

        when(store.getBook("123")).thenReturn(book);
        when(store.getLoansForBook("123")).thenReturn(List.of(activeLoan));
        when(activeLoan.isActive()).thenReturn(true);

        boolean result = service.deleteBook("123");

        assertFalse(result);
        verify(store, never()).removeBook("123");
    }

    @Test
    void deleteBook_shouldRemoveBook_whenNoActiveLoansExist() {
        Book book = new Book("123", "Java", "Author", 2020, 2);
        Loan loan1 = mock(Loan.class);
        Loan loan2 = mock(Loan.class);

        when(store.getBook("123")).thenReturn(book);
        when(store.getLoansForBook("123")).thenReturn(Arrays.asList(loan1, loan2));
        when(loan1.isActive()).thenReturn(false);
        when(loan2.isActive()).thenReturn(false);

        boolean result = service.deleteBook("123");

        assertTrue(result);
        verify(store).removeBook("123");
    }

    @Test
    void registerMember_shouldThrow_whenFirstNameBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registerMember(" ", "Doe", "19900101-1234", 1)
        );

        assertEquals("First name is required.", ex.getMessage());
    }

    @Test
    void registerMember_shouldThrow_whenLastNameBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registerMember("John", " ", "19900101-1234", 1)
        );

        assertEquals("Last name is required.", ex.getMessage());
    }

    @Test
    void registerMember_shouldThrow_whenPersonalNumberBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registerMember("John", "Doe", " ", 1)
        );

        assertEquals("Personal number is required.", ex.getMessage());
    }

    @Test
    void registerMember_shouldThrow_whenLevelInvalid() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registerMember("John", "Doe", "19900101-1234", 5)
        );

        assertEquals("Level must be 1-4.", ex.getMessage());
    }

    @Test
    void registerMember_shouldReturnExistingId_whenAlreadyRegistered() {
        Member existing = new Member("1001", "John", "Doe", "19900101-1234", 1);
        when(store.getMemberByPersonalNumber("19900101-1234")).thenReturn(existing);

        String result = service.registerMember("John", "Doe", "19900101-1234", 1);

        assertEquals("1001", result);
        verify(store, never()).addMember(any());
    }

    @Test
    void registerMember_shouldCreateNewMember_whenNotExisting() {
        when(store.getMemberByPersonalNumber("19900101-1234")).thenReturn(null);
        when(store.getMember("1000")).thenReturn(null);

        String result = service.registerMember("John", "Doe", "19900101-1234", 2);

        assertEquals("1000", result);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(store).addMember(captor.capture());

        Member added = captor.getValue();
        assertEquals("1000", added.id);
        assertEquals("John", added.firstName);
        assertEquals("Doe", added.lastName);
        assertEquals("19900101-1234", added.personalNumber);
        assertEquals(2, added.level);
    }

    @Test
    void suspendMember_shouldReturnFalse_whenMemberDoesNotExist() {
        when(store.getMember("1000")).thenReturn(null);

        boolean result = service.suspendMember("1000", 10);

        assertFalse(result);
    }

    @Test
    void suspendMember_shouldReturnFalse_whenDaysInvalid() {
        Member member = new Member("1000", "John", "Doe", "19900101-1234", 1);
        when(store.getMember("1000")).thenReturn(member);

        boolean result = service.suspendMember("1000", 0);

        assertFalse(result);
    }

    @Test
    void suspendMember_shouldSetSuspendedUntil_whenValid() {
        Member member = new Member("1000", "John", "Doe", "19900101-1234", 1);
        when(store.getMember("1000")).thenReturn(member);

        boolean result = service.suspendMember("1000", 10);

        assertTrue(result);
        assertNotNull(member.suspendedUntil);
        assertTrue(member.suspendedUntil.after(new Date(System.currentTimeMillis() + 8L * 24 * 60 * 60 * 1000)));
    }

    @Test
    void deleteMember_shouldReturnFalse_whenMemberDoesNotExist() {
        when(store.getMember("1000")).thenReturn(null);

        boolean result = service.deleteMember("1000");

        assertFalse(result);
        verify(store, never()).removeMember(anyString());
    }

    @Test
    void deleteMember_shouldReturnFalse_whenActiveLoanExists() {
        Member member = new Member("1000", "John", "Doe", "19900101-1234", 1);
        Loan activeLoan = mock(Loan.class);

        when(store.getMember("1000")).thenReturn(member);
        when(store.getLoansForMember("1000")).thenReturn(List.of(activeLoan));
        when(activeLoan.isActive()).thenReturn(true);

        boolean result = service.deleteMember("1000");

        assertFalse(result);
        verify(store, never()).removeMember("1000");
    }

    @Test
    void deleteMember_shouldRemoveMember_whenNoActiveLoans() {
        Member member = new Member("1000", "John", "Doe", "19900101-1234", 1);
        Loan loan = mock(Loan.class);

        when(store.getMember("1000")).thenReturn(member);
        when(store.getLoansForMember("1000")).thenReturn(List.of(loan));
        when(loan.isActive()).thenReturn(false);

        boolean result = service.deleteMember("1000");

        assertTrue(result);
        verify(store).removeMember("1000");
    }

    @Test
    void lendBook_shouldReturnFalse_whenMemberNotFound() {
        when(store.getMember("1000")).thenReturn(null);

        boolean result = service.lendBook("1000", "123");

        assertFalse(result);
        verify(store, never()).addLoan(any());
    }

    @Test
    void lendBook_shouldReturnFalse_whenBookNotFound() {
        when(store.getMember("1000")).thenReturn(new Member("1000", "John", "Doe", "19900101-1234", 1));
        when(store.getBook("123")).thenReturn(null);

        boolean result = service.lendBook("1000", "123");

        assertFalse(result);
        verify(store, never()).addLoan(any());
    }

    @Test
    void lendBook_shouldReturnFalse_whenMemberCannotBorrow() {
        Member member = mock(Member.class);
        Book book = new Book("123", "Java", "Author", 2020, 2);

        when(store.getMember("1000")).thenReturn(member);
        when(store.getBook("123")).thenReturn(book);
        when(member.canBorrow(any(Date.class))).thenReturn(false);

        boolean result = service.lendBook("1000", "123");

        assertFalse(result);
        verify(store, never()).addLoan(any());
    }

    @Test
    void lendBook_shouldReturnFalse_whenBookNotAvailable() {
        Member member = mock(Member.class);
        Book book = mock(Book.class);

        when(store.getMember("1000")).thenReturn(member);
        when(store.getBook("123")).thenReturn(book);
        when(member.canBorrow(any(Date.class))).thenReturn(true);
        when(book.isAvailable()).thenReturn(false);

        boolean result = service.lendBook("1000", "123");

        assertFalse(result);
        verify(store, never()).addLoan(any());
    }

    @Test
    void lendBook_shouldReturnFalse_whenActiveLoanAlreadyExists() {
        Member member = mock(Member.class);
        Book book = new Book("123", "Java", "Author", 2020, 2);
        Loan existingLoan = mock(Loan.class);

        when(store.getMember("1000")).thenReturn(member);
        when(store.getBook("123")).thenReturn(book);
        when(member.canBorrow(any(Date.class))).thenReturn(true);
        when(store.getActiveLoan("1000", "123")).thenReturn(existingLoan);

        boolean result = service.lendBook("1000", "123");

        assertFalse(result);
        verify(store, never()).addLoan(any());
    }

    @Test
    void lendBook_shouldCreateLoan_whenValid() {
        Member member = new Member("1000", "John", "Doe", "19900101-1234", 1);
        Book book = new Book("123", "Java", "Author", 2020, 2);

        when(store.getMember("1000")).thenReturn(member);
        when(store.getBook("123")).thenReturn(book);
        when(store.getActiveLoan("1000", "123")).thenReturn(null);

        boolean result = service.lendBook("1000", "123");

        assertTrue(result);
        assertEquals(1, member.borrowedCount);
        assertEquals(1, book.availableCopies);

        ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
        verify(store).addLoan(captor.capture());

        Loan loan = captor.getValue();
        assertEquals("1000", loan.memberId);
        assertEquals("123", loan.isbn);
        assertNotNull(loan.loanDate);
        assertNotNull(loan.dueDate);
    }

    @Test
    void returnBook_shouldFail_whenMemberNotFound() {
        when(store.getMember("1000")).thenReturn(null);

        LibraryService.ReturnResult result = service.returnBook("1000", "123");

        assertFalse(result.success);
        assertEquals("Member not found.", result.message);
    }

    @Test
    void returnBook_shouldFail_whenBookNotFound() {
        when(store.getMember("1000")).thenReturn(new Member("1000", "John", "Doe", "19900101-1234", 1));
        when(store.getBook("123")).thenReturn(null);

        LibraryService.ReturnResult result = service.returnBook("1000", "123");

        assertFalse(result.success);
        assertEquals("Book not found.", result.message);
    }

    @Test
    void returnBook_shouldFail_whenNoActiveLoan() {
        Member member = new Member("1000", "John", "Doe", "19900101-1234", 1);
        Book book = new Book("123", "Java", "Author", 2020, 1);

        when(store.getMember("1000")).thenReturn(member);
        when(store.getBook("123")).thenReturn(book);
        when(store.getActiveLoan("1000", "123")).thenReturn(null);

        LibraryService.ReturnResult result = service.returnBook("1000", "123");

        assertFalse(result.success);
        assertEquals("No active loan found.", result.message);
    }

    @Test
    void returnBook_shouldCompleteNormalReturn_whenNotLate() {
        Member member = new Member("1000", "John", "Doe", "19900101-1234", 1);
        member.borrowedCount = 1;

        Book book = new Book("123", "Java", "Author", 2020, 1);
        book.availableCopies = 0;

        Loan loan = mock(Loan.class);
        when(loan.isLate(any(Date.class))).thenReturn(false);

        when(store.getMember("1000")).thenReturn(member);
        when(store.getBook("123")).thenReturn(book);
        when(store.getActiveLoan("1000", "123")).thenReturn(loan);

        LibraryService.ReturnResult result = service.returnBook("1000", "123");

        assertTrue(result.success);
        assertFalse(result.late);
        assertEquals("Return completed.", result.message);
        assertEquals(0, member.borrowedCount);
        assertEquals(1, book.availableCopies);
        verify(loan).isLate(any(Date.class));
    }

    @Test
    void returnBook_shouldSuspendMember_afterThirdLateReturn() {
        Member member = new Member("1000", "John", "Doe", "19900101-1234", 1);
        member.borrowedCount = 1;
        member.lateReturnCount = 2;

        Book book = new Book("123", "Java", "Author", 2020, 1);
        book.availableCopies = 0;

        Loan loan = mock(Loan.class);
        when(loan.isLate(any(Date.class))).thenReturn(true);

        when(store.getMember("1000")).thenReturn(member);
        when(store.getBook("123")).thenReturn(book);
        when(store.getActiveLoan("1000", "123")).thenReturn(loan);

        LibraryService.ReturnResult result = service.returnBook("1000", "123");

        assertTrue(result.success);
        assertTrue(result.late);
        assertNotNull(member.suspendedUntil);
        assertEquals(1, member.suspensionCount);
        assertFalse(result.memberDeleted);
    }

    @Test
    void returnBook_shouldDeleteMember_whenSuspensionCountBecomesMoreThanTwo() {
        Member member = new Member("1000", "John", "Doe", "19900101-1234", 1);
        member.borrowedCount = 1;
        member.lateReturnCount = 2;
        member.suspensionCount = 2;

        Book book = new Book("123", "Java", "Author", 2020, 1);
        book.availableCopies = 0;

        Loan loan = mock(Loan.class);
        when(loan.isLate(any(Date.class))).thenReturn(true);

        when(store.getMember("1000")).thenReturn(member);
        when(store.getBook("123")).thenReturn(book);
        when(store.getActiveLoan("1000", "123")).thenReturn(loan);
        when(store.getLoansForMember("1000")).thenReturn(Collections.emptyList());

        LibraryService.ReturnResult result = service.returnBook("1000", "123");

        assertTrue(result.success);
        assertTrue(result.late);
        assertTrue(result.memberDeleted);
        verify(store).removeMember("1000");
    }

    @Test
    void getLoansForMember_shouldDelegateToStore() {
        List<Loan> loans = List.of(mock(Loan.class));
        when(store.getLoansForMember("1000")).thenReturn(loans);

        List<Loan> result = service.getLoansForMember("1000");

        assertEquals(loans, result);
        verify(store).getLoansForMember("1000");
    }

    @Test
    void getBook_shouldDelegateToStore() {
        Book book = new Book("123", "Java", "Author", 2020, 1);
        when(store.getBook("123")).thenReturn(book);

        Book result = service.getBook("123");

        assertEquals(book, result);
        verify(store).getBook("123");
    }

    @Test
    void getMember_shouldDelegateToStore() {
        Member member = new Member("1000", "John", "Doe", "19900101-1234", 1);
        when(store.getMember("1000")).thenReturn(member);

        Member result = service.getMember("1000");

        assertEquals(member, result);
        verify(store).getMember("1000");
    }
}