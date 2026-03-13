package Database;

import Processing.Book;
import Processing.Loan;
import Processing.Member;

import java.util.List;

public interface ILibraryStore {
    void addBook(Book newBook);
    void updateBook(Book book);
    Book getBook(String isbn);
    void removeBook(String isbn);

    void addMember(Member newMember);
    void updateMember(Member member);
    Member getMember(String memberId);
    Member getMemberByPersonalNumber(String personalNumber);
    void removeMember(String memberId);

    void addLoan(Loan loan);
    void updateLoan(Loan loan);
    Loan getActiveLoan(String memberId, String isbn);

    List<Loan> getLoansForMember(String memberId);
    List<Loan> getLoansForBook(String isbn);
}