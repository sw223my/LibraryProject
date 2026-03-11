import java.util.List;

public interface ILibraryService {
    void addBookTitle(String isbn, String title, String author, int year, int copies);
    boolean deleteBook(String isbn);
    String registerMember(String firstName, String lastName, String personalNumber, int level);
    boolean suspendMember(String memberId, int days);
    boolean deleteMember(String memberId);
    boolean lendBook(String memberId, String isbn);
    LibraryService.ReturnResult returnBook(String memberId, String isbn);
    List<Loan> getLoansForMember(String memberId);
    Book getBook(String isbn);
    Member getMember(String memberId);
}