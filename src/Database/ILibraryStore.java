package Database;

import Objects.BookCopy;
import Objects.BookTitle;
import Objects.Loan;
import Objects.MemberType;
import Objects.Membership;
import Objects.Person;
import Objects.Suspension;

import java.util.List;

public interface ILibraryStore {

    void addBookTitle(BookTitle bookTitle);
    BookTitle getBookTitle(String isbn);
    void removeBookTitle(String isbn);

    void addBookCopies(String isbn, int count);
    List<BookCopy> getBookCopies(String isbn);
    BookCopy getAvailableBookCopy(String isbn);
    void updateBookCopy(BookCopy copy);
    void removeBookCopiesByIsbn(String isbn);

    void addPerson(Person person);
    Person getPerson(String personalNumber);

    void addMembership(Membership membership);
    Membership getMembership(int memberId);
    Membership getMembershipByPersonalNumber(String personalNumber);
    void updateMembership(Membership membership);
    void removeMembership(int memberId);

    MemberType getMemberType(int memberTypeId);
    List<MemberType> getAllMemberTypes();

    void addLoan(Loan loan);
    void updateLoan(Loan loan);
    Loan getActiveLoan(int memberId, String isbn);
    List<Loan> getLoansForMember(int memberId);
    List<Loan> getLoansForBook(String isbn);

    void addSuspension(Suspension suspension);
    List<Suspension> getSuspensionsForMember(int memberId);
}
