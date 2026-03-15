package Processing;

import Database.ILibraryStore;
import Objects.BookCopy;
import Objects.BookTitle;
import Objects.Loan;
import Objects.MemberType;
import Objects.Membership;
import Objects.Person;
import Objects.Suspension;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;

public class LibraryService implements ILibraryService {

    private static final Logger logger = LogManager.getLogger(LibraryService.class);
    private final ILibraryStore store;

    public LibraryService(ILibraryStore store) {
        this.store = store;
    }

    public void addBookTitle(String isbn, String title, String author, int publishYear, int copies) {
        logger.info("Add book title requested. isbn={}, title={}, copies={}", isbn, title, copies);

        validateIsbn(isbn);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (copies <= 0) {
            throw new IllegalArgumentException("Copies must be at least 1.");
        }

        if (store.getBookTitle(isbn) != null) {
            logger.warn("Add book title denied: ISBN already exists. isbn={}", isbn);
            throw new IllegalArgumentException("A book title with this ISBN already exists.");
        }

        BookTitle bookTitle = new BookTitle(isbn, title, author, publishYear);
        store.addBookTitle(bookTitle);
        store.addBookCopies(isbn, copies);
        logger.info("Book title added successfully. isbn={}, copies={}", isbn, copies);
    }

    public boolean deleteBookTitle(String isbn) {
        logger.info("Delete book title requested. isbn={}", isbn);
        validateIsbn(isbn);

        BookTitle bookTitle = store.getBookTitle(isbn);
        if (bookTitle == null) {
            logger.warn("Delete book title denied: book not found. isbn={}", isbn);
            return false;
        }

        List<Loan> loans = store.getLoansForBook(isbn);
        for (Loan loan : loans) {
            if (loan.isActive()) {
                logger.warn("Delete book title denied: active loans exist. isbn={}", isbn);
                return false;
            }
        }

        store.removeBookCopiesByIsbn(isbn);
        store.removeBookTitle(isbn);
        logger.info("Book title deleted successfully. isbn={}", isbn);
        return true;
    }

    public String registerMember(String firstName, String lastName, String personalNumber, int memberTypeId) {
        logger.info("Register member requested. personalNumber={}, memberTypeId={}", personalNumber, memberTypeId);

        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (personalNumber == null || personalNumber.isBlank()) {
            throw new IllegalArgumentException("Personal number is required.");
        }

        MemberType memberType = store.getMemberType(memberTypeId);
        if (memberType == null) {
            logger.warn("Registration denied: invalid member type. memberTypeId={}", memberTypeId);
            throw new IllegalArgumentException("Invalid member type.");
        }

        Person existingPerson = store.getPerson(personalNumber);
        if (existingPerson != null && existingPerson.blocked) {
            logger.warn("Registration denied: person is blocked. personalNumber={}", personalNumber);
            throw new IllegalArgumentException("Registration not allowed due to previous violations.");
        }

        Membership existingMembership = store.getMembershipByPersonalNumber(personalNumber);
        if (existingMembership != null) {
            logger.info("Register member skipped: existing membership found. personalNumber={}, memberId={}",
                    personalNumber, existingMembership.memberId);
            return String.valueOf(existingMembership.memberId);
        }

        if (existingPerson == null) {
            store.addPerson(new Person(personalNumber, firstName, lastName, false));
        }

        int memberId = store.generateMemberId();
        validateGeneratedMemberId(memberId);

        Membership membership = new Membership(
                memberId,
                personalNumber,
                memberType.memberTypeId,
                null,
                "ACTIVE",
                0,
                0
        );

        store.addMembership(membership);

        logger.info("Member registered successfully. personalNumber={}, memberId={}",
                personalNumber, membership.memberId);

        return String.valueOf(membership.memberId);
    }

    public boolean suspendMember(int memberId, int days) {
        logger.info("Suspend member requested. memberId={}, days={}", memberId, days);
        validateMemberId(memberId);

        Membership membership = store.getMembership(memberId);
        if (membership == null || days <= 0) {
            logger.warn("Suspend member denied. memberId={}, days={}", memberId, days);
            return false;
        }

        Date today = new Date();
        updateMembershipStatus(membership, today);

        Date endDate = addDays(today, days);
        membership.suspendedUntil = endDate;
        membership.status = "SUSPENDED";
        membership.suspensionCount++;

        store.updateMembership(membership);
        store.addSuspension(new Suspension(0, memberId, today, endDate));
        logger.info("Member suspended successfully. memberId={}, suspendedUntil={}", memberId, endDate);
        return true;
    }

    public boolean deleteMember(int memberId) {
        logger.info("Delete member requested. memberId={}", memberId);
        validateMemberId(memberId);

        Membership membership = store.getMembership(memberId);
        if (membership == null) {
            logger.warn("Delete member denied: member not found. memberId={}", memberId);
            return false;
        }

        List<Loan> loans = store.getLoansForMember(memberId);
        for (Loan loan : loans) {
            if (loan.isActive()) {
                logger.warn("Delete member denied: active loans exist. memberId={}", memberId);
                return false;
            }
        }

        store.blockPerson(membership.personalNumber);
        store.removeMembership(memberId);
        logger.info("Member deleted and person blocked successfully. memberId={}, personalNumber={}",
                memberId, membership.personalNumber);
        return true;
    }

    public boolean lendBook(int memberId, String isbn) {
        logger.info("Lend request received. memberId={}, isbn={}", memberId, isbn);
        validateMemberId(memberId);
        validateIsbn(isbn);

        Membership membership = store.getMembership(memberId);
        if (membership == null) {
            logger.warn("Lend denied: member not found. memberId={}", memberId);
            return false;
        }

        Date today = new Date();
        boolean statusChanged = updateMembershipStatus(membership, today);
        if (statusChanged) {
            store.updateMembership(membership);
        }

        BookTitle bookTitle = store.getBookTitle(isbn);
        if (bookTitle == null) {
            logger.warn("Lend denied: book not found. isbn={}", isbn);
            return false;
        }

        if (membership.isSuspended(today)) {
            logger.warn("Lend denied: member is suspended. memberId={}", memberId);
            return false;
        }

        MemberType memberType = store.getMemberType(membership.memberTypeId);
        if (memberType == null) {
            logger.error("Lend failed: member type not found. memberId={}, memberTypeId={}",
                    memberId, membership.memberTypeId);
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
            logger.warn("Lend denied: loan limit reached. memberId={}, activeLoans={}, maxLoans={}",
                    memberId, activeLoans, memberType.maxLoans);
            return false;
        }

        if (store.getActiveLoan(memberId, isbn) != null) {
            logger.warn("Lend denied: member already has this book on loan. memberId={}, isbn={}", memberId, isbn);
            return false;
        }

        BookCopy copy = store.getAvailableBookCopy(isbn);
        if (copy == null) {
            logger.warn("Lend denied: no available copy. isbn={}", isbn);
            return false;
        }

        copy.status = "LOANED";
        store.updateBookCopy(copy);

        Loan loan = new Loan(
                0,
                memberId,
                copy.copyId,
                today,
                addDays(today, 15),
                null
        );

        store.addLoan(loan);
        logger.info("Loan created successfully. memberId={}, isbn={}, copyId={}, loanId={}",
                memberId, isbn, copy.copyId, loan.loanId);
        return true;
    }

    public ReturnResult returnBook(int memberId, String isbn) {
        logger.info("Return request received. memberId={}, isbn={}", memberId, isbn);
        validateMemberId(memberId);
        validateIsbn(isbn);

        Membership membership = store.getMembership(memberId);
        if (membership == null) {
            logger.warn("Return denied: member not found. memberId={}", memberId);
            return new ReturnResult(false, false, null, false, "Member not found.");
        }

        Date today = new Date();
        boolean statusChanged = updateMembershipStatus(membership, today);
        if (statusChanged) {
            store.updateMembership(membership);
        }

        BookTitle bookTitle = store.getBookTitle(isbn);
        if (bookTitle == null) {
            logger.warn("Return denied: book not found. isbn={}", isbn);
            return new ReturnResult(false, false, null, false, "Book not found.");
        }

        Loan loan = store.getActiveLoan(memberId, isbn);
        if (loan == null) {
            logger.warn("Return denied: no active loan found. memberId={}, isbn={}", memberId, isbn);
            return new ReturnResult(false, false, null, false, "No active loan found.");
        }

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
        } else {
            logger.warn("Returned loan copy could not be found in catalog. loanId={}, copyId={}",
                    loan.loanId, loan.copyId);
        }

        boolean wasLate = today.after(loan.dueDate);
        boolean memberDeleted = false;
        Date suspendedUntil = null;

        if (wasLate) {
            membership.lateReturnCount++;

            logger.warn("Late return registered. memberId={}, isbn={}, lateReturnCount={}",
                    memberId, isbn, membership.lateReturnCount);

            if (membership.lateReturnCount > 2) {
                suspendedUntil = addDays(today, 15);
                membership.suspendedUntil = suspendedUntil;
                membership.status = "SUSPENDED";
                membership.suspensionCount++;

                store.addSuspension(new Suspension(0, memberId, today, suspendedUntil));
                logger.warn("Member suspended after repeated late returns. memberId={}, suspendedUntil={}",
                        memberId, suspendedUntil);

                if (membership.suspensionCount > 2) {
                    store.blockPerson(membership.personalNumber);
                    store.removeMembership(memberId);
                    memberDeleted = true;

                    logger.warn("Member exceeded suspension limit and was blocked. memberId={}, personalNumber={}",
                            memberId, membership.personalNumber);
                } else {
                    store.updateMembership(membership);
                }
            } else {
                store.updateMembership(membership);
                logger.info("Late return count updated in database. memberId={}, lateReturnCount={}",
                        memberId, membership.lateReturnCount);
            }
        }

        logger.info("Return completed. memberId={}, isbn={}, late={}, memberDeleted={}",
                memberId, isbn, wasLate, memberDeleted);
        return new ReturnResult(true, wasLate, suspendedUntil, memberDeleted, "Return completed.");
    }

    public List<BookTitle> searchBookTitles(String titleQuery) {
        return store.searchBookTitlesByTitle(titleQuery);
    }

    public BookTitle getBookTitle(String isbn) {
        validateIsbn(isbn);
        return store.getBookTitle(isbn);
    }

    public Membership getMembership(int memberId) {
        validateMemberId(memberId);

        Membership membership = store.getMembership(memberId);
        if (membership == null) {
            return null;
        }

        boolean statusChanged = updateMembershipStatus(membership, new Date());
        if (statusChanged) {
            store.updateMembership(membership);
        }

        return membership;
    }

    public List<Loan> getLoansForMember(int memberId) {
        validateMemberId(memberId);
        return store.getLoansForMember(memberId);
    }

    private Date addDays(Date date, int days) {
        long ms = days * 24L * 60 * 60 * 1000;
        return new Date(date.getTime() + ms);
    }

    private boolean updateMembershipStatus(Membership membership, Date today) {
        if (membership == null) {
            return false;
        }

        String oldStatus = membership.status;
        Date oldSuspendedUntil = membership.suspendedUntil;

        if (membership.suspendedUntil == null || !membership.suspendedUntil.after(today)) {
            membership.suspendedUntil = null;
            membership.status = "ACTIVE";
        } else {
            membership.status = "SUSPENDED";
        }

        boolean statusChanged =
                (oldStatus == null && membership.status != null) ||
                        (oldStatus != null && !oldStatus.equals(membership.status));

        boolean dateChanged =
                (oldSuspendedUntil == null && membership.suspendedUntil != null) ||
                        (oldSuspendedUntil != null && membership.suspendedUntil == null) ||
                        (oldSuspendedUntil != null && membership.suspendedUntil != null
                                && !oldSuspendedUntil.equals(membership.suspendedUntil));

        return statusChanged || dateChanged;
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

    private void validateIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN is required.");
        }
        if (!isbn.matches("\\d{6}")) {
            throw new IllegalArgumentException("ISBN must be exactly 6 digits.");
        }
    }

    private void validateMemberId(int memberId) {
        if (memberId < 1000 || memberId > 9999) {
            throw new IllegalArgumentException("Member ID must be a 4-digit number.");
        }
    }

    private void validateGeneratedMemberId(int memberId) {
        if (memberId < 1000 || memberId > 9999) {
            throw new IllegalStateException("Generated member ID must be a 4-digit number.");
        }
    }
}