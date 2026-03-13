package Processing;

import Database.ILibraryStore;
import Objects.BookCopy;
import Objects.BookTitle;
import Objects.Loan;
import Objects.MemberType;
import Objects.Membership;
import Objects.Person;
import Objects.Suspension;

import java.util.Date;
import java.util.List;

public class LibraryService {

    private final ILibraryStore store;
    private int nextLoanId = 1;

    public LibraryService(ILibraryStore store) {
        this.store = store;
    }

    public void addBookTitle(String isbn, String title, String author, int publishYear, int copies) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN is required.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (copies <= 0) {
            throw new IllegalArgumentException("Copies must be at least 1.");
        }

        if (store.getBookTitle(isbn) != null) {
            throw new IllegalArgumentException("A book title with this ISBN already exists.");
        }

        BookTitle bookTitle = new BookTitle(isbn, title, author, publishYear);
        store.addBookTitle(bookTitle);
        store.addBookCopies(isbn, copies);
    }

    public boolean deleteBookTitle(String isbn) {
        BookTitle bookTitle = store.getBookTitle(isbn);
        if (bookTitle == null) {
            return false;
        }

        List<Loan> loans = store.getLoansForBook(isbn);
        for (Loan loan : loans) {
            if (loan.isActive()) {
                return false;
            }
        }

        store.removeBookCopiesByIsbn(isbn);
        store.removeBookTitle(isbn);
        return true;
    }

    public String registerMember(String firstName, String lastName, String personalNumber, int memberTypeId) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (personalNumber == null || personalNumber.isBlank()) {
            throw new IllegalArgumentException("Personal number is required.");
        }

        Membership existing = store.getMembershipByPersonalNumber(personalNumber);
        if (existing != null) {
            return String.valueOf(existing.memberId);
        }

        int newId = generateNextMemberId();

        Person person = new Person(personalNumber, firstName, lastName);
        Membership membership = new Membership(
                newId,
                personalNumber,
                memberTypeId,
                null,
                "ACTIVE",
                0,
                0
        );

        store.addPerson(person);
        store.addMembership(membership);

        return String.valueOf(newId);
    }

    public boolean suspendMember(int memberId, int days) {
        Membership membership = store.getMembership(memberId);
        if (membership == null || days <= 0) {
            return false;
        }

        Date today = new Date();
        Date endDate = addDays(today, days);

        membership.suspendedUntil = endDate;
        membership.status = "SUSPENDED";
        membership.suspensionCount++;

        store.updateMembership(membership);
        store.addSuspension(new Suspension(0, memberId, today, endDate));
        return true;
    }

    public boolean deleteMember(int memberId) {
        Membership membership = store.getMembership(memberId);
        if (membership == null) {
            return false;
        }

        List<Loan> loans = store.getLoansForMember(memberId);
        for (Loan loan : loans) {
            if (loan.isActive()) {
                return false;
            }
        }

        store.removeMembership(memberId);
        return true;
    }

    public boolean lendBook(int memberId, String isbn) {
        Membership membership = store.getMembership(memberId);
        if (membership == null) {
            return false;
        }

        BookTitle bookTitle = store.getBookTitle(isbn);
        if (bookTitle == null) {
            return false;
        }

        Date today = new Date();

        if (membership.isSuspended(today)) {
            return false;
        }

        MemberType memberType = store.getMemberType(membership.memberTypeId);
        if (memberType == null) {
            return false;
        }

        List<Loan> currentLoans = store.getLoansForMember(memberId);

        int activeLoans = 0;
        for (Loan loan : currentLoans) {
            if (loan.isActive()) {
                activeLoans++;
            }
        }

        if (activeLoans >= memberType.maxLoans) {
            return false;
        }

        if (store.getActiveLoan(memberId, isbn) != null) {
            return false;
        }

        BookCopy copy = store.getAvailableBookCopy(isbn);
        if (copy == null) {
            return false;
        }

        copy.status = "LOANED";
        store.updateBookCopy(copy);

        Loan loan = new Loan(
                nextLoanId++,
                memberId,
                copy.copyId,
                today,
                addDays(today, 15),
                null
        );

        store.addLoan(loan);
        return true;
    }

    public ReturnResult returnBook(int memberId, String isbn) {
        Membership membership = store.getMembership(memberId);
        if (membership == null) {
            return new ReturnResult(false, false, null, false, "Member not found.");
        }

        BookTitle bookTitle = store.getBookTitle(isbn);
        if (bookTitle == null) {
            return new ReturnResult(false, false, null, false, "Book not found.");
        }

        Loan loan = store.getActiveLoan(memberId, isbn);
        if (loan == null) {
            return new ReturnResult(false, false, null, false, "No active loan found.");
        }

        Date today = new Date();
        loan.returnDate = today;
        store.updateLoan(loan);

        BookCopy returnedCopy = null;
        for (BookCopy copy : store.getBookCopies(isbn)) {
            if (copy.copyId == loan.copyId) {
                returnedCopy = copy;
                break;
            }
        }

        if (returnedCopy != null) {
            returnedCopy.status = "AVAILABLE";
            store.updateBookCopy(returnedCopy);
        }

        boolean wasLate = today.after(loan.dueDate);
        boolean memberDeleted = false;
        Date suspendedUntil = null;

        if (wasLate) {
            membership.lateReturnCount++;

            if (membership.lateReturnCount > 2) {
                suspendedUntil = addDays(today, 15);
                membership.suspendedUntil = suspendedUntil;
                membership.status = "SUSPENDED";
                membership.suspensionCount++;

                store.addSuspension(new Suspension(
                        0, memberId, today, suspendedUntil
                ));

                if (membership.suspensionCount > 2) {
                    memberDeleted = deleteMember(memberId);
                } else {
                    store.updateMembership(membership);
                }
            } else {
                store.updateMembership(membership);
            }
        }

        return new ReturnResult(true, wasLate, suspendedUntil, memberDeleted, "Return completed.");
    }

    public BookTitle getBookTitle(String isbn) {
        return store.getBookTitle(isbn);
    }

    public Membership getMembership(int memberId) {
        return store.getMembership(memberId);
    }

    public List<Loan> getLoansForMember(int memberId) {
        return store.getLoansForMember(memberId);
    }

    private Date addDays(Date date, int days) {
        long ms = days * 24L * 60 * 60 * 1000;
        return new Date(date.getTime() + ms);
    }

    private int generateNextMemberId() {
        for (int i = 1000; i <= 9999; i++) {
            if (store.getMembership(i) == null) {
                return i;
            }
        }
        throw new IllegalStateException("No free member IDs available.");
    }

    public static class ReturnResult {
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
