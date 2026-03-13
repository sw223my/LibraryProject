package Database;

import Objects.BookCopy;
import Objects.BookTitle;
import Objects.Loan;
import Objects.MemberType;
import Objects.Membership;
import Objects.Person;
import Objects.Suspension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryLibraryStore implements ILibraryStore {

    private final Map<String, BookTitle> bookTitles = new HashMap<>();
    private final Map<String, List<BookCopy>> bookCopiesByIsbn = new HashMap<>();
    private final Map<String, Person> persons = new HashMap<>();
    private final Map<Integer, Membership> memberships = new HashMap<>();
    private final Map<Integer, MemberType> memberTypes = new HashMap<>();
    private final List<Loan> loans = new ArrayList<>();
    private final List<Suspension> suspensions = new ArrayList<>();

    private int nextCopyId = 1;
    private int nextMemberId = 1000;
    private int nextLoanId = 1;
    private int nextSuspensionId = 1;

    public InMemoryLibraryStore() {
        memberTypes.put(1, new MemberType(1, "Undergraduate", 3));
        memberTypes.put(2, new MemberType(2, "Postgraduate", 5));
        memberTypes.put(3, new MemberType(3, "PhD", 7));
        memberTypes.put(4, new MemberType(4, "Teacher", 10));
    }

    @Override
    public void addBookTitle(BookTitle bookTitle) {
        if (bookTitle == null || bookTitle.isbn == null || bookTitle.isbn.isBlank()) {
            return;
        }
        bookTitles.put(bookTitle.isbn, bookTitle);
    }

    @Override
    public BookTitle getBookTitle(String isbn) {
        return bookTitles.get(isbn);
    }

    @Override
    public void removeBookTitle(String isbn) {
        bookTitles.remove(isbn);
    }

    @Override
    public void addBookCopies(String isbn, int count) {
        if (isbn == null || isbn.isBlank() || count <= 0) {
            return;
        }

        List<BookCopy> copies = bookCopiesByIsbn.computeIfAbsent(isbn, key -> new ArrayList<>());
        for (int i = 0; i < count; i++) {
            copies.add(new BookCopy(nextCopyId++, isbn, "AVAILABLE"));
        }
    }

    @Override
    public List<BookCopy> getBookCopies(String isbn) {
        return new ArrayList<>(bookCopiesByIsbn.getOrDefault(isbn, new ArrayList<>()));
    }

    @Override
    public BookCopy getBookCopy(int copyId) {
        for (List<BookCopy> copies : bookCopiesByIsbn.values()) {
            for (BookCopy copy : copies) {
                if (copy.copyId == copyId) {
                    return copy;
                }
            }
        }
        return null;
    }

    @Override
    public BookCopy getAvailableBookCopy(String isbn) {
        for (BookCopy copy : bookCopiesByIsbn.getOrDefault(isbn, new ArrayList<>())) {
            if (copy.isAvailable()) {
                return copy;
            }
        }
        return null;
    }

    @Override
    public void updateBookCopy(BookCopy copy) {
        if (copy == null) {
            return;
        }

        List<BookCopy> copies = bookCopiesByIsbn.get(copy.isbn);
        if (copies == null) {
            return;
        }

        for (int i = 0; i < copies.size(); i++) {
            if (copies.get(i).copyId == copy.copyId) {
                copies.set(i, copy);
                return;
            }
        }
    }

    @Override
    public void removeBookCopiesByIsbn(String isbn) {
        bookCopiesByIsbn.remove(isbn);
    }

    @Override
    public void addPerson(Person person) {
        if (person == null || person.personalNumber == null || person.personalNumber.isBlank()) {
            return;
        }
        persons.put(person.personalNumber, person);
    }

    @Override
    public Person getPerson(String personalNumber) {
        return persons.get(personalNumber);
    }

    @Override
    public void addMembership(Membership membership) {
        if (membership == null) {
            return;
        }

        if (membership.memberId <= 0) {
            membership.memberId = nextMemberId++;
        }

        memberships.put(membership.memberId, membership);
    }

    @Override
    public Membership getMembership(int memberId) {
        return memberships.get(memberId);
    }

    @Override
    public Membership getMembershipByPersonalNumber(String personalNumber) {
        for (Membership membership : memberships.values()) {
            if (membership.personalNumber != null && membership.personalNumber.equals(personalNumber)) {
                return membership;
            }
        }
        return null;
    }

    @Override
    public void updateMembership(Membership membership) {
        if (membership == null) {
            return;
        }
        memberships.put(membership.memberId, membership);
    }

    @Override
    public void removeMembership(int memberId) {
        memberships.remove(memberId);
    }

    @Override
    public MemberType getMemberType(int memberTypeId) {
        return memberTypes.get(memberTypeId);
    }

    @Override
    public List<MemberType> getAllMemberTypes() {
        return new ArrayList<>(memberTypes.values());
    }

    @Override
    public void addLoan(Loan loan) {
        if (loan == null) {
            return;
        }

        if (loan.loanId <= 0) {
            loan.loanId = nextLoanId++;
        }

        loans.add(loan);
    }

    @Override
    public void updateLoan(Loan loan) {
        if (loan == null) {
            return;
        }

        for (int i = 0; i < loans.size(); i++) {
            if (loans.get(i).loanId == loan.loanId) {
                loans.set(i, loan);
                return;
            }
        }
    }

    @Override
    public Loan getActiveLoan(int memberId, String isbn) {
        for (Loan loan : loans) {
            if (loan.memberId != memberId || !loan.isActive()) {
                continue;
            }

            for (BookCopy copy : bookCopiesByIsbn.getOrDefault(isbn, new ArrayList<>())) {
                if (copy.copyId == loan.copyId) {
                    return loan;
                }
            }
        }
        return null;
    }

    @Override
    public List<Loan> getLoansForMember(int memberId) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.memberId == memberId) {
                result.add(loan);
            }
        }
        return result;
    }

    @Override
    public List<Loan> getLoansForBook(String isbn) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            for (BookCopy copy : bookCopiesByIsbn.getOrDefault(isbn, new ArrayList<>())) {
                if (copy.copyId == loan.copyId) {
                    result.add(loan);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void addSuspension(Suspension suspension) {
        if (suspension == null) {
            return;
        }

        if (suspension.suspensionId <= 0) {
            suspension.suspensionId = nextSuspensionId++;
        }

        suspensions.add(suspension);
    }

    @Override
    public List<Suspension> getSuspensionsForMember(int memberId) {
        List<Suspension> result = new ArrayList<>();
        for (Suspension suspension : suspensions) {
            if (suspension.memberId == memberId) {
                result.add(suspension);
            }
        }
        return result;
    }
}