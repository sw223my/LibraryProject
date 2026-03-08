import java.util.Date;
import java.util.List;

public class LibraryService {

    ILibraryStore store;
    private int nextLoanId = 1;

    public LibraryService(ILibraryStore store) {
        this.store = store;
    }

    public void addBookTitle(String isbn, String title, String author, int year, int copies) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN saknas");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Titel saknas");
        }
        if (copies <= 0) {
            throw new IllegalArgumentException("Antal exemplar måste vara minst 1");
        }

        Book book = new Book(isbn, title, author, year, copies);
        store.addBook(book);

        // logging senare
        // System.out.println("Book added: " + title);
    }

    public void registerMember(String id, String firstName, String lastName, int level) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Medlems-id saknas");
        }
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("Ogiltig medlemsnivå");
        }

        Member member = new Member(id, firstName, lastName, level);
        store.addMember(member);

        // logging senare
        // System.out.println("Member registered: " + member.fullName());
    }

    public boolean lendBook(String isbn, String memberId) {
        Member member = store.getMember(memberId);
        if (member == null) {
            return false;
        }

        Book book = store.getBook(isbn);
        if (book == null) {
            return false;
        }

        Date today = new Date();

        if (member.isSuspended(today)) {
            return false;
        }

        if (!member.canBorrow()) {
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

        long loanPeriodMs = 14L * 24 * 60 * 60 * 1000; // 14 dagar
        Date dueDate = new Date(today.getTime() + loanPeriodMs);

        Loan loan = new Loan(nextLoanId++, memberId, isbn, today, dueDate);
        store.addLoan(loan);

        // logging senare
        // System.out.println("Loan created: " + isbn + " -> " + memberId);

        return true;
    }

    public boolean returnBook(String isbn, String memberId) {
        Member member = store.getMember(memberId);
        if (member == null) {
            return false;
        }

        Book book = store.getBook(isbn);
        if (book == null) {
            return false;
        }

        Loan loan = store.getActiveLoan(memberId, isbn);
        if (loan == null) {
            return false;
        }

        Date today = new Date();
        loan.returnDate = today;

        book.availableCopies++;

        if (member.borrowedCount > 0) {
            member.borrowedCount--;
        }

        if (loan.isLate(today)) {
            member.lateReturnCount++;
            member.suspensionCount++;

            long suspensionMs = 7L * 24 * 60 * 60 * 1000; // 7 dagar
            member.suspendedUntil = new Date(today.getTime() + suspensionMs);
        }

        // logging senare
        // System.out.println("Book returned: " + isbn + " by " + memberId);

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

        // logging senare
        // System.out.println("Member deleted: " + memberId);

        return true;
    }

    // Behåll denna så Main fortfarande fungerar
    public boolean borrow(String isbn, String memberId) {
        return lendBook(isbn, memberId);
    }
}