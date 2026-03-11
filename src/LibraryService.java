import java.util.Date;
import java.util.List;

public class LibraryService implements ILibraryService {

    private final ILibraryStore store;
    private int nextLoanId = 1;

    public LibraryService(ILibraryStore store) {
        this.store = store;
    }

    public void addBookTitle(String isbn, String title, String author, int year, int copies) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN is required.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (copies <= 0) {
            throw new IllegalArgumentException("Copies must be at least 1.");
        }

        Book existing = store.getBook(isbn);
        if (existing != null) {
            throw new IllegalArgumentException("A book with this ISBN already exists.");
        }

        Book book = new Book(isbn, title, author, year, copies);
        store.addBook(book);
    }

    public boolean deleteBook(String isbn) {
        Book book = store.getBook(isbn);
        if (book == null) {
            return false;
        }

        List<Loan> loans = store.getLoansForBook(isbn);
        for (Loan loan : loans) {
            if (loan.isActive()) {
                return false;
            }
        }

        store.removeBook(isbn);
        return true;
    }

    public String registerMember(String firstName, String lastName, String personalNumber, int level) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (personalNumber == null || personalNumber.isBlank()) {
            throw new IllegalArgumentException("Personal number is required.");
        }
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("Level must be 1-4.");
        }

        Member existing = store.getMemberByPersonalNumber(personalNumber);
        if (existing != null) {
            return existing.id; // already registered
        }

        String newId = generateNextMemberId();
        Member member = new Member(newId, firstName, lastName, personalNumber, level);
        store.addMember(member);
        return newId;
    }

    public boolean suspendMember(String memberId, int days) {
        Member member = store.getMember(memberId);
        if (member == null) {
            return false;
        }

        if (days <= 0) {
            return false;
        }

        Date today = new Date();
        member.suspendedUntil = addDays(today, days);
        return true;
    }

    public boolean deleteMember(String memberId) {
        Member member = store.getMember(memberId);
        if (member == null) {
            return false;
        }

        List<Loan> loans = store.getLoansForMember(memberId);
        for (Loan loan : loans) {
            if (loan.isActive()) {
                return false;
            }
        }

        store.removeMember(memberId);
        return true;
    }

    public boolean lendBook(String memberId, String isbn) {
        Member member = store.getMember(memberId);
        if (member == null) {
            return false;
        }

        Book book = store.getBook(isbn);
        if (book == null) {
            return false;
        }

        Date today = new Date();

        if (!member.canBorrow(today)) {
            return false;
        }

        if (!book.isAvailable()) {
            return false;
        }

        if (store.getActiveLoan(memberId, isbn) != null) {
            return false;
        }

        book.availableCopies--;
        member.borrowedCount++;

        Date dueDate = addDays(today, 15);
        Loan loan = new Loan(nextLoanId++, memberId, isbn, today, dueDate);
        store.addLoan(loan);

        return true;
    }

    public ReturnResult returnBook(String memberId, String isbn) {
        Member member = store.getMember(memberId);
        if (member == null) {
            return new ReturnResult(false, false, null, false, "Member not found.");
        }

        Book book = store.getBook(isbn);
        if (book == null) {
            return new ReturnResult(false, false, null, false, "Book not found.");
        }

        Loan loan = store.getActiveLoan(memberId, isbn);
        if (loan == null) {
            return new ReturnResult(false, false, null, false, "No active loan found.");
        }

        Date today = new Date();
        loan.returnDate = today;

        book.availableCopies++;

        if (member.borrowedCount > 0) {
            member.borrowedCount--;
        }

        boolean wasLate = loan.isLate(today);
        boolean deleted = false;
        Date suspendedUntil = null;

        if (wasLate) {
            member.lateReturnCount++;

            if (member.lateReturnCount > 2) {
                member.suspendedUntil = addDays(today, 15);
                member.suspensionCount++;
                suspendedUntil = member.suspendedUntil;

                if (member.suspensionCount > 2) {
                    deleted = deleteMember(memberId);
                }
            }
        }

        return new ReturnResult(true, wasLate, suspendedUntil, deleted, "Return completed.");
    }

    public List<Loan> getLoansForMember(String memberId) {
        return store.getLoansForMember(memberId);
    }

    public Book getBook(String isbn) {
        return store.getBook(isbn);
    }

    public Member getMember(String memberId) {
        return store.getMember(memberId);
    }

    private Date addDays(Date date, int days) {
        long ms = days * 24L * 60 * 60 * 1000;
        return new Date(date.getTime() + ms);
    }

    private String generateNextMemberId() {
        for (int i = 1000; i <= 9999; i++) {
            String id = String.valueOf(i);
            if (store.getMember(id) == null) {
                return id;
            }
        }
        throw new IllegalStateException("No free member IDs available.");
    }

    public class ReturnResult {
        public final boolean success;
        public final boolean late;
        public final Date suspendedUntil;
        public final boolean memberDeleted;
        public final String message;

        public ReturnResult(boolean success, boolean late, Date suspendedUntil, boolean memberDeleted, String message) {
            this.success = success;
            this.late = late;
            this.suspendedUntil = suspendedUntil;
            this.memberDeleted = memberDeleted;
            this.message = message;
        }
    }
}