package Processing;

import Objects.BookTitle;
import Objects.Loan;
import Objects.Membership;

import java.util.List;

public interface ILibraryService {
    void addBookTitle(String isbn, String title, String author, int year, int copies);
    boolean deleteBookTitle(String isbn);

    String registerMember(String firstName, String lastName, String personalNumber, int memberTypeId);
    boolean suspendMember(int memberId, int days);
    boolean deleteMember(int memberId);

    boolean lendBook(int memberId, String isbn);
    LibraryService.ReturnResult returnBook(int memberId, String isbn);

    List<Loan> getLoansForMember(int memberId);
    BookTitle getBookTitle(String isbn);
    List<BookTitle> searchBookTitles(String titleQuery);
    Membership getMembership(int memberId);
}
